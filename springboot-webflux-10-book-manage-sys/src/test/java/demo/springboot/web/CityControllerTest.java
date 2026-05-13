package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

public class CityControllerTest {

    private static final String REDIRECT_TO_CITY_URL = "redirect:/city";

    @Test
    public void postCitySubscribesToSaveBeforeRedirect() {
        City city = new City();
        StubCityService cityService = new StubCityService();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        cityService.insertResult = Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        });

        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Assert.assertEquals(REDIRECT_TO_CITY_URL, cityController.postCity(city).block());
        Assert.assertTrue(subscribed.get());
    }

    @Test
    public void putCitySubscribesToUpdateBeforeRedirect() {
        City city = new City();
        StubCityService cityService = new StubCityService();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        cityService.updateResult = Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        });

        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Assert.assertEquals(REDIRECT_TO_CITY_URL, cityController.putCity(city).block());
        Assert.assertTrue(subscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirect() {
        Long cityId = 1L;
        StubCityService cityService = new StubCityService();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        cityService.deleteResult = Mono.fromRunnable(() -> subscribed.set(true)).then();

        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Assert.assertEquals(REDIRECT_TO_CITY_URL, cityController.deleteCity(cityId).block());
        Assert.assertTrue(subscribed.get());
    }

    @Test
    public void deleteCityDoesNotExposeDestructiveGetRoute() throws NoSuchMethodException {
        RequestMapping mapping = CityController.class
                .getMethod("deleteCity", Long.class)
                .getAnnotation(RequestMapping.class);

        Assert.assertArrayEquals(new RequestMethod[] {RequestMethod.POST}, mapping.method());
    }

    private static class StubCityService implements CityService {

        private Mono<City> insertResult = Mono.empty();
        private Mono<City> updateResult = Mono.empty();
        private Mono<Void> deleteResult = Mono.empty();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return insertResult;
        }

        @Override
        public Mono<City> update(City city) {
            return updateResult;
        }

        @Override
        public Mono<Void> delete(Long id) {
            return deleteResult;
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
