package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCitySubscribesToRepositoryDelete() {
        AtomicBoolean deleted = new AtomicBoolean(false);
        CityHandler cityHandler = new CityHandler(repository(Mono.create(sink -> {
            deleted.set(true);
            sink.success();
        })));

        Long deletedId = cityHandler.deleteCity(99L).block();

        Assert.assertEquals(Long.valueOf(99L), deletedId);
        Assert.assertTrue(deleted.get());
    }

    @Test
    public void deleteCityPropagatesRepositoryDeleteFailure() {
        CityHandler cityHandler = new CityHandler(
                repository(Mono.error(new IllegalStateException("delete failed"))));

        try {
            cityHandler.deleteCity(99L).block();
            Assert.fail("Expected delete failure to propagate");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("delete failed", ex.getMessage());
        }
    }

    private CityRepository repository(Mono<Void> deleteResult) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        return deleteResult;
                    }
                    if ("toString".equals(method.getName())) {
                        return "CityRepository delete test double";
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
