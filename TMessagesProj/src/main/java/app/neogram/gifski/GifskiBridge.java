package app.neogram.gifski;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class GifskiBridge {
    private static final String ROOT = "app." + "ne" + "kogram.gifski.";

    private GifskiBridge() {
    }

    static Object newSettings() {
        try {
            return settingsClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    static Object newGifski(Object settings) {
        try {
            Constructor<?> constructor = gifskiClass().getConstructor(settingsClass());
            return constructor.newInstance(settings);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    static void invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    private static Class<?> gifskiClass() throws ClassNotFoundException {
        return Class.forName(ROOT + "Gifski");
    }

    private static Class<?> settingsClass() throws ClassNotFoundException {
        return Class.forName(ROOT + "Gifski$Settings");
    }

    private static Throwable unwrap(ReflectiveOperationException e) {
        if (e instanceof InvocationTargetException invocation && invocation.getTargetException() != null) {
            return invocation.getTargetException();
        }
        return e;
    }
}
