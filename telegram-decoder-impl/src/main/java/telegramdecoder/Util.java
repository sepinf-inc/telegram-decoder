package telegramdecoder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Puts the non-null public/protected instance fields of o into the map (value.toString()).
     * Static fields (constants such as FLAG_* and constructor) are not included.
     */
    public static void fieldsToMap(Map<String, Object> other, Object o) {
        if (o == null) {
            return;
        }
        for (Field f : INSTANCE_FIELDS.get(o.getClass())) {
            try {
                final Object val = f.get(o);
                if (val != null) {
                    other.put(f.getName(), val.toString());
                }
            } catch (IllegalAccessException e) {
                // not expected: fields are public or were made accessible when cached
            }
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