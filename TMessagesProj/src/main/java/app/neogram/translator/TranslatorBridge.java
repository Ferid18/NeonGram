package app.neogram.translator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class TranslatorBridge {
    private static final String ROOT = "app." + "ne" + "kogram.translator.";

    private TranslatorBridge() {
    }

    static Object singleton(String simpleName) {
        try {
            Class<?> clazz = Class.forName(ROOT + simpleName);
            return clazz.getMethod("getInstance").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static void invokeStaticUnchecked(String simpleName, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> clazz = Class.forName(ROOT + simpleName);
            Method method = clazz.getMethod(methodName, parameterTypes);
            method.invoke(null, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(unwrap(e));
        }
    }

    static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw translateException(e.getTargetException());
        } catch (ReflectiveOperationException e) {
            throw e;
        }
    }

    static String readStringField(Object target, String name) throws ReflectiveOperationException {
        Field field;
        try {
            field = target.getClass().getField(name);
        } catch (NoSuchFieldException ignored) {
            field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
        }
        Object value = field.get(target);
        return value != null ? value.toString() : null;
    }

    private static Throwable unwrap(ReflectiveOperationException e) {
        if (e instanceof InvocationTargetException invocation && invocation.getTargetException() != null) {
            return invocation.getTargetException();
        }
        return e;
    }

    private static Exception translateException(Throwable throwable) {
        if ("Http429Exception".equals(throwable.getClass().getSimpleName())) {
            return new Http429Exception(throwable.getMessage(), throwable);
        }
        if (throwable instanceof Exception exception) {
            return exception;
        }
        return new RuntimeException(throwable);
    }
}
