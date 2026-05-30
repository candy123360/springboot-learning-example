package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import org.spring.springboot.domain.City;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CityHandlerDeleteTest {

    @Test
    public void deleteCityRunsRepositoryDeleteWhenResultIsConsumed() throws Exception {
        AtomicLong deletedId = new AtomicLong();
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityHandler cityHandler = new CityHandler(deleteRepository(deletedId, deleteSubscribed));
        setRedisTemplate(cityHandler, new RedisTemplateStub(false));

        Long result = cityHandler.deleteCity(42L).block();

        Assert.assertEquals(Long.valueOf(42L), result);
        Assert.assertEquals(42L, deletedId.get());
        Assert.assertTrue(deleteSubscribed.get());
    }

    private CityRepository deleteRepository(AtomicLong deletedId, AtomicBoolean deleteSubscribed) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("deleteById".equals(method.getName()) && args != null && args.length == 1 && args[0] instanceof Long) {
                    deletedId.set((Long) args[0]);
                    return Mono.fromRunnable(() -> deleteSubscribed.set(true));
                }
                if ("toString".equals(method.getName())) {
                    return "CityRepositoryProxy";
                }
                throw new UnsupportedOperationException(method.toString());
            }
        };
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                handler);
    }

    private void setRedisTemplate(CityHandler cityHandler, RedisTemplateStub redisTemplate) throws Exception {
        Field field = CityHandler.class.getDeclaredField("redisTemplate");
        field.setAccessible(true);
        field.set(cityHandler, redisTemplate);
    }

    private static class RedisTemplateStub extends RedisTemplate<String, City> {
        private final boolean hasKey;

        private RedisTemplateStub(boolean hasKey) {
            this.hasKey = hasKey;
        }

        @Override
        public Boolean hasKey(String key) {
            return hasKey;
        }

        @Override
        public Boolean delete(String key) {
            return true;
        }
    }
}
