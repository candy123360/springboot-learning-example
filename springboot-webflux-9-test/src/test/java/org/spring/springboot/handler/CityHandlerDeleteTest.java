package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCityRunsRepositoryDeleteWhenSubscribed() {
        AtomicInteger deleteSubscriptions = new AtomicInteger();
        AtomicLong deletedId = new AtomicLong();
        CityRepository cityRepository = repositoryRecordingDelete(deleteSubscriptions, deletedId);
        CityHandler cityHandler = new CityHandler(cityRepository);

        Mono<Long> deleteResult = cityHandler.deleteCity(9L);

        Assert.assertEquals(0, deleteSubscriptions.get());

        StepVerifier.create(deleteResult)
                .expectNext(9L)
                .verifyComplete();

        Assert.assertEquals(1, deleteSubscriptions.get());
        Assert.assertEquals(9L, deletedId.get());
    }

    private static CityRepository repositoryRecordingDelete(AtomicInteger deleteSubscriptions,
                                                           AtomicLong deletedId) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName()) && args != null && args.length == 1
                            && args[0] instanceof Long) {
                        return Mono.defer(() -> {
                            deleteSubscriptions.incrementAndGet();
                            deletedId.set((Long) args[0]);
                            return Mono.empty();
                        });
                    }
                    if ("toString".equals(method.getName())) {
                        return "RecordingCityRepository";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
