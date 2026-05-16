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
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityRepository cityRepository = repositoryDeletingWith(Mono.fromRunnable(() -> deleteSubscribed.set(true)));
        CityHandler cityHandler = new CityHandler(cityRepository);

        Mono<Long> deletedId = cityHandler.deleteCity(1L);

        Assert.assertFalse(deleteSubscribed.get());
        Assert.assertEquals(Long.valueOf(1L), deletedId.block());
        Assert.assertTrue(deleteSubscribed.get());
    }

    @Test(expected = IllegalStateException.class)
    public void deleteCityPropagatesRepositoryDeleteFailure() {
        CityRepository cityRepository = repositoryDeletingWith(Mono.error(new IllegalStateException("delete failed")));
        CityHandler cityHandler = new CityHandler(cityRepository);

        cityHandler.deleteCity(1L).block();
    }

    private CityRepository repositoryDeletingWith(Mono<Void> deleteResult) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class<?>[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())
                            && args != null
                            && args.length == 1
                            && Long.valueOf(1L).equals(args[0])) {
                        return deleteResult;
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
