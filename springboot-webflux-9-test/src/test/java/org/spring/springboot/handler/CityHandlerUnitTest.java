package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class CityHandlerUnitTest {

    @Test
    public void deleteCitySubscribesRepositoryDelete() {
        final AtomicInteger deleteSubscriptions = new AtomicInteger();
        final CityRepository cityRepository = (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        return Mono.fromRunnable(deleteSubscriptions::incrementAndGet);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        final CityHandler cityHandler = new CityHandler(cityRepository);

        final Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertEquals(0, deleteSubscriptions.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertEquals(1, deleteSubscriptions.get());
    }
}
