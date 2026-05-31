package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

public class CityControllerTest {

    @Test
    public void postCityReturnsPublisherThatPersistsBeforeRedirect() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = new CityController();
        cityController.cityService = cityService;

        City city = new City();
        Mono<String> result = cityController.postCity(city);

        Assert.assertSame(city, cityService.insertedCity);
        Assert.assertFalse(cityService.insertSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putBookReturnsPublisherThatUpdatesBeforeRedirect() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = new CityController();
        cityController.cityService = cityService;

        City city = new City();
        Mono<String> result = cityController.putBook(city);

        Assert.assertSame(city, cityService.updatedCity);
        Assert.assertFalse(cityService.updateSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCityReturnsPublisherThatDeletesBeforeRedirect() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Mono<String> result = cityController.deleteCity(42L);

        Assert.assertEquals(Long.valueOf(42L), cityService.deletedId);
        Assert.assertFalse(cityService.deleteSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.deleteSubscribed.get());
    }

    private static class RecordingCityService implements CityService {

        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);

        private City insertedCity;
        private City updatedCity;
        private Long deletedId;

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            insertedCity = city;
            return Mono.fromSupplier(() -> {
                insertSubscribed.set(true);
                return city;
            });
        }

        @Override
        public Mono<City> update(City city) {
            updatedCity = city;
            return Mono.fromSupplier(() -> {
                updateSubscribed.set(true);
                return city;
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            deletedId = id;
            return Mono.fromRunnable(() -> deleteSubscribed.set(true));
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
