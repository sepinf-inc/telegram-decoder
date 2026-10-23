package telegramdecoder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.telegram.tgnet.TLObject;

public class Util {
    private static final byte header[] = Base64.getDecoder().decode(
            "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDACgcHiMeGSgjISMtKygwPGRBPDc3PHtYXUlkkYCZlo+AjIqgtObDoKrarYqMyP/L2u71////m8H////6/+b9//j/2wBDASstLTw1PHZBQXb4pYyl+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj4+Pj/wAARCAAAAAADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwA=");

    public static List<Field> getAllFields(Class clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }

        List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
        List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()) || Modifier.isProtected(f.getModifiers()))
                .collect(Collectors.toList());
        result.addAll(filteredFields);
        return result;
    }

    private static final Field[] NO_FIELDS = new Field[0];

    /**
     * Public/protected instance fields of each class, superclass fields first (same order as
     * {@link #getAllFields(Class)}). Computed once per class: ClassValue is thread-safe and does not
     * prevent the class from being unloaded.
     */
    private static final ClassValue<Field[]> INSTANCE_FIELDS = new ClassValue<Field[]>() {
        @Override
        protected Field[] computeValue(Class<?> type) {
            final Class<?> superclass = type.getSuperclass();
            final Field[] inherited = superclass != null ? get(superclass) : NO_FIELDS;
            final ArrayList<Field> fields = new ArrayList<>(inherited.length + 16);
            Collections.addAll(fields, inherited);
            final boolean publicType = Modifier.isPublic(type.getModifiers());
            for (Field f : type.getDeclaredFields()) {
                final int mod = f.getModifiers();
                if (Modifier.isStatic(mod) || f.isSynthetic() || !(Modifier.isPublic(mod) || Modifier.isProtected(mod))) {
                    continue;
                }
                if (!publicType || !Modifier.isPublic(mod)) {
                    try {
                        f.setAccessible(true);
                    } catch (RuntimeException e) {
                        continue;
                    }
                }
                fields.add(f);
            }
            return fields.toArray(NO_FIELDS);
        }
    };

    /** Nesting levels of TL objects rendered inside a single map value. */
    private static final int MAX_DEPTH = 3;
    /** Items rendered per collection/array; the rest is summarized as "...(+n)". */
    private static final int MAX_ITEMS = 100;
    /** Max chars of a single map value; longer values are truncated with "...". */
    private static final int MAX_VALUE_LENGTH = 8192;

    private static final ClassValue<Boolean> HAS_OWN_TO_STRING = new ClassValue<Boolean>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            try {
                return type.getMethod("toString").getDeclaringClass() != Object.class;
            } catch (NoSuchMethodException e) {
                return Boolean.FALSE;
            }
        }
    };

    /**
     * Puts the non-null public/protected instance fields of o into the map, as strings. Static fields
     * (constants such as FLAG_* and constructor) are not included.
     * <p>
     * Values without a meaningful toString() are rendered compactly instead of "Class@hash":
     * <ul>
     * <li>TL objects: {@code TL_peerUser{user_id=123}}, only non-default fields, up to {@link #MAX_DEPTH} levels;</li>
     * <li>collections/arrays: {@code [a, b]}, byte arrays as {@code byte[n]};</li>
     * <li>other objects without toString() (client-side Bitmap, NativeByteBuffer...): skipped.</li>
     * </ul>
     */
    public static void fieldsToMap(Map<String, Object> other, Object o) {
        if (o == null) {
            return;
        }
        StringBuilder sb = null;
        for (Field f : INSTANCE_FIELDS.get(o.getClass())) {
            final Object val;
            try {
                val = f.get(o);
            } catch (IllegalAccessException e) {
                continue; // not expected: fields are public or were made accessible when cached
            }
            if (val == null) {
                continue;
            }
            final String str;
            if (val instanceof String) {
                str = (String) val;
            } else if (isScalar(val)) {
                str = val.toString();
            } else if (!isRenderable(val)) {
                continue;
            } else {
                if (sb == null) {
                    sb = new StringBuilder(128);
                } else {
                    sb.setLength(0);
                }
                appendValue(sb, val, 0);
                if (sb.length() > MAX_VALUE_LENGTH) {
                    sb.setLength(MAX_VALUE_LENGTH);
                    sb.append("...");
                }
                str = sb.toString();
            }
            other.put(f.getName(), str);
        }
    }

    private static boolean isScalar(Object v) {
        return v instanceof Number || v instanceof Boolean || v instanceof Character || v instanceof Enum;
    }

    private static boolean isRenderable(Object v) {
        return v instanceof TLObject || v.getClass().isArray() || HAS_OWN_TO_STRING.get(v.getClass());
    }

    /** Values omitted inside a rendered TL object (absent optional fields, unset flags, client-side objects). */
    private static boolean isDefault(Object v) {
        if (v == null) {
            return true;
        }
        if (v instanceof Boolean) {
            return !(Boolean) v;
        }
        if (v instanceof Number) {
            return (v instanceof Double || v instanceof Float) ? ((Number) v).doubleValue() == 0 : ((Number) v).longValue() == 0;
        }
        if (v instanceof CharSequence) {
            return ((CharSequence) v).length() == 0;
        }
        if (v instanceof Collection) {
            return ((Collection<?>) v).isEmpty();
        }
        return !isScalar(v) && !isRenderable(v);
    }

    private static void appendValue(StringBuilder sb, Object v, int depth) {
        if (sb.length() >= MAX_VALUE_LENGTH) {
            return;
        }
        if (v == null) {
            sb.append("null");
            return;
        }
        if (v instanceof CharSequence || isScalar(v)) {
            sb.append(v);
            return;
        }
        if (v instanceof byte[]) {
            sb.append("byte[").append(((byte[]) v).length).append(']');
            return;
        }
        final Class<?> c = v.getClass();
        if (c.isArray()) {
            final int n = Array.getLength(v);
            sb.append('[');
            for (int i = 0; i < n && i < MAX_ITEMS && sb.length() < MAX_VALUE_LENGTH; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                appendValue(sb, Array.get(v, i), depth);
            }
            appendRemaining(sb, n);
            sb.append(']');
            return;
        }
        if (v instanceof Collection) {
            final Collection<?> col = (Collection<?>) v;
            sb.append('[');
            int i = 0;
            for (Object item : col) {
                if (i == MAX_ITEMS || sb.length() >= MAX_VALUE_LENGTH) {
                    break;
                }
                if (i++ > 0) {
                    sb.append(", ");
                }
                appendValue(sb, item, depth);
            }
            appendRemaining(sb, col.size());
            sb.append(']');
            return;
        }
        if (HAS_OWN_TO_STRING.get(c)) {
            sb.append(v.toString());
            return;
        }
        sb.append(c.getSimpleName());
        if (!(v instanceof TLObject) || depth >= MAX_DEPTH) {
            return;
        }
        boolean first = true;
        for (Field f : INSTANCE_FIELDS.get(c)) {
            final Object fv;
            try {
                fv = f.get(v);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (isDefault(fv)) {
                continue;
            }
            sb.append(first ? "{" : ", ").append(f.getName()).append('=');
            first = false;
            appendValue(sb, fv, depth + 1);
            if (sb.length() >= MAX_VALUE_LENGTH) {
                break;
            }
        }
        if (!first) {
            sb.append('}');
        }
    }

    private static void appendRemaining(StringBuilder sb, int size) {
        if (size > MAX_ITEMS) {
            sb.append(", ...(+").append(size - MAX_ITEMS).append(')');
        }
    }

    public static byte[] decodeThumb(byte[] thumb) {
        // See
        // https://github.com/TelegramMessenger/Telegram-iOS/blob/master/submodules/TinyThumbnail/Sources/TinyThumbnail.swift
        byte thumbImg[] = new byte[header.length + thumb.length - 1];
        System.arraycopy(header, 0, thumbImg, 0, header.length);

        byte width = thumb[1];
        byte height = thumb[2];
        for (int i = 3; i < thumb.length; i++) {
            thumbImg[header.length + i - 3] = thumb[i];
        }
        thumbImg[164] = width;
        thumbImg[166] = height;
        thumbImg[thumbImg.length - 2] = (byte) 0xFF;
        thumbImg[thumbImg.length - 1] = (byte) 0xD9;
        return thumbImg;
    }
}