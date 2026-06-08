package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class CityHandlerReactiveChainTest {

    @Test
    public void deleteCitySubscribesToRepositoryDelete() {
        AtomicReference<Long> deletedId = new AtomicReference<>();
        CityHandler handler = new CityHandler(repositoryDeletingInto(deletedId));

        Mono<Long> result = handler.deleteCity(1L);

        Assert.assertNull(deletedId.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertEquals(Long.valueOf(1L), deletedId.get());
    }

    private CityRepository repositoryDeletingInto(AtomicReference<Long> deletedId) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("deleteById".equals(method.getName())) {
                        Long id = (Long) args[0];
                        return Mono.fromRunnable(() -> deletedId.set(id));
                    }
                    if ("toString".equals(method.getName())) {
                        return "CityRepository delete test proxy";
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
