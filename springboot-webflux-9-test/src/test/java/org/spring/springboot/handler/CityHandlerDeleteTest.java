package org.spring.springboot.handler;

import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCitySubscribesToRepositoryDeleteBeforeReturningId() {
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityHandler cityHandler = new CityHandler(repositoryThatRecordsDeleteSubscription(deleteSubscribed));

        Mono<Long> deleteResult = cityHandler.deleteCity(1L);

        assertFalse(deleteSubscribed.get());
        assertEquals(Long.valueOf(1L), deleteResult.block());
        assertTrue(deleteSubscribed.get());
    }

    private CityRepository repositoryThatRecordsDeleteSubscription(final AtomicBoolean deleteSubscribed) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class<?>[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        return Mono.defer(() -> {
                            deleteSubscribed.set(true);
                            return Mono.empty();
                        });
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
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
