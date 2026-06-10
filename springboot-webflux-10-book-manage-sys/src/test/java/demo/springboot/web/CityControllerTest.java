package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CityControllerTest {

    @Test
    public void postCitySubscribesToInsertBeforeRedirect() {
        CityController controller = new CityController();
        RecordingCityService cityService = new RecordingCityService();
        controller.cityService = cityService;

        Mono<String> result = controller.postCity(new City());

        Assert.assertFalse(cityService.insertSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putBookSubscribesToUpdateBeforeRedirect() {
        CityController controller = new CityController();
        RecordingCityService cityService = new RecordingCityService();
        controller.cityService = cityService;

        Mono<String> result = controller.putBook(new City());

        Assert.assertFalse(cityService.updateSubscribed.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirect() {
        CityController controller = new CityController();
        RecordingCityService cityService = new RecordingCityService();
        controller.cityService = cityService;

        Mono<String> result = controller.deleteCity(7L);

        Assert.assertNull(cityService.deletedId.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertEquals(Long.valueOf(7L), cityService.deletedId.get());
    }

    private static final class RecordingCityService implements CityService {

        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicReference<Long> deletedId = new AtomicReference<>();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.defer(() -> {
                insertSubscribed.set(true);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.defer(() -> {
                updateSubscribed.set(true);
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
