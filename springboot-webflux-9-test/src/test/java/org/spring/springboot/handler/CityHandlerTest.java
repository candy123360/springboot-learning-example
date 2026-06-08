package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import org.spring.springboot.domain.City;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public class CityHandlerTest {

    @Test
    public void testSave() {
        City wl = new City();
        wl.setId(1L);
        wl.setProvinceId(2L);
        wl.setCityName("WL");
        wl.setDescription("WL IS GOOD");

        AtomicReference<City> savedCity = new AtomicReference<>();
        CityHandler handler = new CityHandler(repositorySavingInto(savedCity));

        Mono<City> result = handler.save(wl);

        Assert.assertNull(savedCity.get());
        City expectCity = result.block();

        Assert.assertNotNull(expectCity);
        Assert.assertSame(wl, expectCity);
        Assert.assertSame(wl, savedCity.get());
    }

    private CityRepository repositorySavingInto(AtomicReference<City> savedCity) {
        return (CityRepository) Proxy.newProxyInstance(
                CityRepository.class.getClassLoader(),
                new Class[]{CityRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        City city = (City) args[0];
                        return Mono.defer(() -> {
                            savedCity.set(city);
                            return Mono.just(city);
                        });
                    }
                    if ("toString".equals(method.getName())) {
                        return "CityRepository save test proxy";
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
