package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public class CityHandlerUnitTest {

    @Test
    public void deleteCityRunsRepositoryDeleteBeforeReturningId() {
        AtomicBoolean deleted = new AtomicBoolean(false);
        CityRepository cityRepository = repositoryDeletingWith(deleted);
        CityHandler cityHandler = new CityHandler(cityRepository);

        Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertFalse(deleted.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertTrue(deleted.get());
    }

    private CityRepository repositoryDeletingWith(AtomicBoolean deleted) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        return Mono.<Void>fromRunnable(() -> deleted.set(true));
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
