package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCityRunsRepositoryDeleteBeforeReturningId() {
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        AtomicReference<Long> deletedId = new AtomicReference<>();
        CityRepository repository = (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName()) && args != null && args.length == 1) {
                        deletedId.set((Long) args[0]);
                        return Mono.fromRunnable(() -> deleteSubscribed.set(true));
                    }
                    throw new UnsupportedOperationException(method.toString());
                });

        CityHandler cityHandler = new CityHandler(repository);

        StepVerifier.create(cityHandler.deleteCity(1L))
                .expectNext(1L)
                .verifyComplete();

        Assert.assertEquals(Long.valueOf(1L), deletedId.get());
        Assert.assertTrue(deleteSubscribed.get());
    }
}
