package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public class CityHandlerUnitTest {

    @Test
    public void deleteCitySubscribesToRepositoryDeleteBeforeReturningId() {
        AtomicBoolean subscribed = new AtomicBoolean(false);
        CityRepository cityRepository = repositoryWithDelete(Mono.fromRunnable(() -> subscribed.set(true)));
        CityHandler cityHandler = new CityHandler(cityRepository);

        Long deletedId = cityHandler.deleteCity(42L).block();

        Assert.assertTrue(subscribed.get());
        Assert.assertEquals(Long.valueOf(42L), deletedId);
    }

    private CityRepository repositoryWithDelete(Mono<Void> deleteMono) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName()) && args != null && args.length == 1
                            && args[0] instanceof Long) {
                        return deleteMono;
                    }
                    if ("toString".equals(method.getName())) {
                        return "CityRepository test proxy";
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
