package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class CityControllerTest {

    @Test
    public void postCityChainsInsertIntoTheReturnedMono() {
        CityController controller = new CityController();
        RecordingCityService cityService = new RecordingCityService();
        controller.cityService = cityService;
        City city = new City();

        Mono<String> result = controller.postCity(city);

        Assert.assertFalse(cityService.insertSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.insertSubscribed.get());
        Assert.assertSame(city, cityService.insertedCity.get());
    }

    @Test
    public void putBookChainsUpdateIntoTheReturnedMono() {
        CityController controller = new CityController();
        RecordingCityService cityService = new RecordingCityService();
        controller.cityService = cityService;
        City city = new City();

        Mono<String> result = controller.putBook(city);

        Assert.assertFalse(cityService.updateSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.updateSubscribed.get());
        Assert.assertSame(city, cityService.updatedCity.get());
    }

    @Test
    public void deleteCityChainsDeleteIntoTheReturnedMono() {
        CityController controller = new CityController();
        RecordingCityService cityService = new RecordingCityService();
        controller.cityService = cityService;

        Mono<String> result = controller.deleteCity(42L);

        Assert.assertFalse(cityService.deleteSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.deleteSubscribed.get());
        Assert.assertEquals(42L, cityService.deletedId.get());
    }

    private static class RecordingCityService implements CityService {

        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        private final AtomicReference<City> insertedCity = new AtomicReference<City>();
        private final AtomicReference<City> updatedCity = new AtomicReference<City>();
        private final AtomicLong deletedId = new AtomicLong();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(final City city) {
            return Mono.defer(() -> {
                insertSubscribed.set(true);
                insertedCity.set(city);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(final City city) {
            return Mono.defer(() -> {
                updateSubscribed.set(true);
                updatedCity.set(city);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(final Long id) {
            return Mono.defer(() -> {
                deleteSubscribed.set(true);
                deletedId.set(id);
                return Mono.empty();
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
