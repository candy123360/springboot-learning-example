package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCityWaitsForRepositoryDelete() {
        AtomicInteger deletes = new AtomicInteger();
        CityRepository cityRepository = (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())
                            && args != null
                            && args.length == 1
                            && Long.valueOf(1L).equals(args[0])) {
                        return Mono.fromRunnable(() -> deletes.incrementAndGet());
                    }
                    if ("toString".equals(method.getName())) {
                        return "cityRepository";
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
        CityHandler cityHandler = new CityHandler(cityRepository);

        Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertEquals(0, deletes.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertEquals(1, deletes.get());
    }
}
