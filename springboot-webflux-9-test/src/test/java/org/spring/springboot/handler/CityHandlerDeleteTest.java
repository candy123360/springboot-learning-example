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
        CityHandler cityHandler = new CityHandler(repositoryWithDeleteFlag(deleted));

        Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertFalse(deleted.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertTrue(deleted.get());
    }

    private CityRepository repositoryWithDeleteFlag(AtomicBoolean deleted) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        Assert.assertEquals(Long.valueOf(1L), args[0]);
                        return Mono.fromRunnable(() -> deleted.set(true));
                    }
                    if ("toString".equals(method.getName())) {
                        return "CityRepository proxy";
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
