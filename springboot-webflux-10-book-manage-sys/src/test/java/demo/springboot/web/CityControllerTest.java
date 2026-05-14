package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CityControllerTest {

    private CityController cityController;
    private StubCityService cityService;

    @Before
    public void setUp() {
        cityController = new CityController();
        cityService = new StubCityService();
        cityController.cityService = cityService;
    }

    @Test
    public void postCitySubscribesToInsertBeforeRedirect() {
        City city = new City();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        cityService.insertResult = savedCity -> Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(savedCity);
        });

        String viewName = cityController.postCity(city).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(subscribed.get());
    }

    @Test
    public void putCitySubscribesToUpdateBeforeRedirect() {
        City city = new City();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        cityService.updateResult = savedCity -> Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(savedCity);
        });

        String viewName = cityController.putCity(city).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(subscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirect() {
        Long cityId = 1L;
        AtomicBoolean subscribed = new AtomicBoolean(false);
        cityService.deleteResult = id -> Mono.defer(() -> {
            subscribed.set(true);
            return Mono.empty();
        });

        String viewName = cityController.deleteCity(cityId).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(subscribed.get());
    }

    private static class StubCityService implements CityService {

        private Function<City, Mono<City>> insertResult = Mono::just;
        private Function<City, Mono<City>> updateResult = Mono::just;
        private Function<Long, Mono<Void>> deleteResult = id -> Mono.empty();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return insertResult.apply(city);
        }

        @Override
        public Mono<City> update(City city) {
            return updateResult.apply(city);
        }

        @Override
        public Mono<Void> delete(Long id) {
            return deleteResult.apply(id);
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
