package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CityHandlerUnitTest {

    @Test
    public void deleteCityExecutesRepositoryDeleteBeforeReturningId() {
        RepositoryInvocationHandler invocationHandler = new RepositoryInvocationHandler();
        CityHandler cityHandler = new CityHandler(invocationHandler.repository());

        Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertEquals(0, invocationHandler.deleteCount);
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertEquals(1, invocationHandler.deleteCount);
        Assert.assertEquals(Long.valueOf(1L), invocationHandler.deletedId);
    }

    private static class RepositoryInvocationHandler implements InvocationHandler {

        private int deleteCount;
        private Long deletedId;

        private CityRepository repository() {
            return (CityRepository) Proxy.newProxyInstance(
                    CityRepository.class.getClassLoader(),
                    new Class<?>[]{CityRepository.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("deleteById".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof Long) {
                return Mono.<Void>fromRunnable(() -> {
                    deleteCount++;
                    deletedId = (Long) args[0];
                });
            }
            if ("toString".equals(method.getName())) {
                return "CityRepository test proxy";
            }
            throw new UnsupportedOperationException(method.getName());
        }
    }
}
