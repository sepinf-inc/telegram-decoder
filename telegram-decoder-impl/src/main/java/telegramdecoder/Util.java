package telegramdecoder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Util {
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

    public static void fieldsToMap(Map<String, Object> other, Object o) {
        if (o == null) {
            return;
        }
        List<Field> fields = getAllFields(o.getClass());

        for (Field f : fields) {
            try {
                other.put(f.getName(), f.get(o));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}