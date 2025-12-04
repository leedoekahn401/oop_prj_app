package project.app.humanelogistics.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    public static <T> void register(Class<T> serviceClass, T instance) {
        services.put(serviceClass, instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceClass) {
        T service = (T) services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException(
                    "Service not registered: " + serviceClass.getName()
            );
        }
        return service;
    }

    public static boolean isRegistered(Class<?> serviceClass) {
        return services.containsKey(serviceClass);
    }

    public static void clear() {
        services.clear();
    }
}