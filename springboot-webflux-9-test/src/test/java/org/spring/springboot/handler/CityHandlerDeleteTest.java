package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCityRunsRepositoryDeleteWhenResultIsConsumed() {
        AtomicLong deletedId = new AtomicLong();
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityHandler cityHandler = new CityHandler(deleteRepository(deletedId, deleteSubscribed));

        Long result = cityHandler.deleteCity(42L).block();

        Assert.assertEquals(Long.valueOf(42L), result);
        Assert.assertEquals(42L, deletedId.get());
        Assert.assertTrue(deleteSubscribed.get());
    }

    private CityRepository deleteRepository(AtomicLong deletedId, AtomicBoolean deleteSubscribed) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("deleteById".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof Long) {
                    deletedId.set((Long) args[0]);
                    return Mono.fromRunnable(() -> deleteSubscribed.set(true));
                }
                if ("toString".equals(method.getName())) {
                    return "CityRepositoryProxy";
                }
                throw new UnsupportedOperationException(method.toString());
            }
        };
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                handler);
    }
}
