package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import org.spring.springboot.domain.City;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class CityHandlerTest {

    @Test
    public void testSave() throws Exception {
        City city = newCity();
        CityHandler cityHandler = new CityHandler(repository(new AtomicReference<Long>()));

        City expectCity = cityHandler.save(city).block();

        Assert.assertNotNull(expectCity);
        Assert.assertEquals(city.getId(), expectCity.getId());
        Assert.assertEquals(city.getCityName(), expectCity.getCityName());
    }

    @Test
    public void deleteCitySubscribesToRepositoryDeleteBeforeReturningId() {
        AtomicReference<Long> deletedId = new AtomicReference<>();
        CityHandler cityHandler = new CityHandler(repository(deletedId));

        Mono<Long> result = cityHandler.deleteCity(1L);

        Assert.assertNull(deletedId.get());
        Assert.assertEquals(Long.valueOf(1L), result.block());
        Assert.assertEquals(Long.valueOf(1L), deletedId.get());
    }

    private static City newCity() {
        City wl = new City();
        wl.setId(1L);
        wl.setProvinceId(2L);
        wl.setCityName("WL");
        wl.setDescription("WL IS GOOD");
        return wl;
    }

    private static CityRepository repository(AtomicReference<Long> deletedId) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("save".equals(methodName)) {
                        return Mono.just(args[0]);
                    }
                    if ("findById".equals(methodName)) {
                        return Mono.empty();
                    }
                    if ("findAll".equals(methodName)) {
                        return Flux.empty();
                    }
                    if ("deleteById".equals(methodName)) {
                        Long id = (Long) args[0];
                        return Mono.fromRunnable(() -> deletedId.set(id));
                    }
                    if ("toString".equals(methodName)) {
                        return "CityRepository test proxy";
                    }
                    if ("hashCode".equals(methodName)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(methodName)) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
