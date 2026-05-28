package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.springboot.dao.CityRepository;
import org.spring.springboot.domain.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CityHandlerTest {

    @Autowired
    private WebTestClient webClient;

    private static Map<String, City> cityMap = new HashMap<>();

    @BeforeClass
    public static void setup() throws Exception {
        City wl = new City();
        wl.setId(1L);
        wl.setProvinceId(2L);
        wl.setCityName("WL");
        wl.setDescription("WL IS GOOD");
        cityMap.put("WL", wl);
    }

    @Test
    public void testSave() throws Exception {

        City expectCity = webClient.post().uri("/city")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(cityMap.get("WL")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(City.class).returnResult().getResponseBody();

        Assert.assertNotNull(expectCity);
        Assert.assertEquals(expectCity.getId(), cityMap.get("WL").getId());
        Assert.assertEquals(expectCity.getCityName(), cityMap.get("WL").getCityName());
    }

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
