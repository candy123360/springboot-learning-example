package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class CityHandlerUnitTest {

    @Test
    public void deleteCityExecutesRepositoryDeleteWhenSubscribed() {
        AtomicInteger deleteSubscriptions = new AtomicInteger();
        CityRepository cityRepository = repositoryThatCountsDeleteSubscriptions(deleteSubscriptions);
        CityHandler cityHandler = new CityHandler(cityRepository);

        Long deletedId = cityHandler.deleteCity(99L).block();

        Assert.assertEquals(Long.valueOf(99L), deletedId);
        Assert.assertEquals(1, deleteSubscriptions.get());
    }

    private CityRepository repositoryThatCountsDeleteSubscriptions(AtomicInteger deleteSubscriptions) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        return Mono.fromRunnable(deleteSubscriptions::incrementAndGet).then();
                    }
                    if ("toString".equals(method.getName())) {
                        return "CityRepositoryProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
