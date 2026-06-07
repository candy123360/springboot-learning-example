package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

public class CityHandlerTest {

    @Test
    public void deleteCitySubscribesToRepositoryDelete() {
        final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityHandler cityHandler = new CityHandler(repositoryProxy(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("deleteById".equals(method.getName())) {
                    return Mono.fromRunnable(new Runnable() {
                        @Override
                        public void run() {
                            deleteSubscribed.set(true);
                        }
                    });
                }
                throw new UnsupportedOperationException(method.getName());
            }
        }));

        Mono<Long> deleteResult = cityHandler.deleteCity(1L);

        Assert.assertFalse(deleteSubscribed.get());
        StepVerifier.create(deleteResult)
                .expectNext(1L)
                .verifyComplete();
        Assert.assertTrue(deleteSubscribed.get());
    }

    private CityRepository repositoryProxy(InvocationHandler invocationHandler) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                invocationHandler);
    }
}
