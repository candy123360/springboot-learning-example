package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public class CityHandlerUnitTest {

    @Test
    public void deleteCityReturnsPublisherThatSubscribesToRepositoryDelete() {
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityHandler cityHandler = new CityHandler(repositoryWithDelete(deleteSubscribed));

        Mono<Long> result = cityHandler.deleteCity(42L);

        Assert.assertFalse(deleteSubscribed.get());
        Assert.assertEquals(Long.valueOf(42L), result.block());
        Assert.assertTrue(deleteSubscribed.get());
    }

    private CityRepository repositoryWithDelete(AtomicBoolean deleteSubscribed) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class<?>[]{CityRepository.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass().equals(Object.class)) {
                        return invokeObjectMethod(proxy, method.getName(), args);
                    }
                    if ("deleteById".equals(method.getName())) {
                        return Mono.<Void>fromRunnable(() -> deleteSubscribed.set(true));
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }

    private Object invokeObjectMethod(Object proxy, String methodName, Object[] args) {
        if ("toString".equals(methodName)) {
            return "CityRepository proxy";
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
