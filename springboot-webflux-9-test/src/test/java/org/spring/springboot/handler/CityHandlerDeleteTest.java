package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCitySubscribesRepositoryDeleteBeforeReturningId() {
        AtomicBoolean deleteSubscribed = new AtomicBoolean();
        CityRepository cityRepository = repositoryWithDeferredDelete(deleteSubscribed);
        CityHandler cityHandler = new CityHandler(cityRepository);

        Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertFalse(deleteSubscribed.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertTrue(deleteSubscribed.get());
    }

    private static CityRepository repositoryWithDeferredDelete(AtomicBoolean deleteSubscribed) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return invokeObjectMethod(proxy, method.getName(), args);
                    }
                    if ("deleteById".equals(method.getName()) && args != null && args.length == 1
                            && args[0] instanceof Long) {
                        return Mono.defer(() -> {
                            deleteSubscribed.set(true);
                            return Mono.empty();
                        });
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }

    private static Object invokeObjectMethod(Object proxy, String methodName, Object[] args) {
        if ("toString".equals(methodName)) {
            return "CityRepository delete proxy";
        }
        if ("hashCode".equals(methodName)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName)) {
            return proxy == args[0];
        }
        throw new UnsupportedOperationException(methodName);
    }
}
