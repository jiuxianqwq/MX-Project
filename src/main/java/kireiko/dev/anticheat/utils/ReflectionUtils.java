package kireiko.dev.anticheat.utils;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    public static Class<?> getClass(String s) {
        try {
            return Class.forName(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean classExists(String s) {
        try {
            Class.forName(s);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static Object getObject(String packages, Class<?> clazz, String path) {
        if (clazz != null && path != null) {
            try {
                clazz = Class.forName(packages + clazz.getName());
                final Field field = clazz.getDeclaredField(path);

                field.setAccessible(true);
                return field.get(clazz);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
