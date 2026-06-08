package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

public class CityControllerReactiveChainTest {

    private CityController controller;
    private RecordingCityService cityService;

    @Before
    public void setUp() {
        controller = new CityController();
        cityService = new RecordingCityService();
        controller.cityService = cityService;
    }

    @Test
    public void postCityRunsInsertBeforeRedirect() {
        City city = new City();

        Mono<String> result = controller.postCity(city);

        Assert.assertNull(cityService.insertedCity.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertSame(city, cityService.insertedCity.get());
    }

    @Test
    public void putBookRunsUpdateBeforeRedirect() {
        City city = new City();

        Mono<String> result = controller.putBook(city);

        Assert.assertNull(cityService.updatedCity.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertSame(city, cityService.updatedCity.get());
    }

    @Test
    public void deleteCityRunsDeleteBeforeRedirect() {
        Mono<String> result = controller.deleteCity(1L);

        Assert.assertNull(cityService.deletedId.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertEquals(Long.valueOf(1L), cityService.deletedId.get());
    }

    private static class RecordingCityService implements CityService {
        private final AtomicReference<City> insertedCity = new AtomicReference<>();
        private final AtomicReference<City> updatedCity = new AtomicReference<>();
        private final AtomicReference<Long> deletedId = new AtomicReference<>();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.defer(() -> {
                insertedCity.set(city);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.defer(() -> {
                updatedCity.set(city);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(() -> deletedId.set(id));
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
