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
    public void postCitySubscribesSaveBeforeRedirect() {
        RecordingCityService cityService = new RecordingCityService();
        CityController controller = newController(cityService);

        Mono<String> redirect = controller.postCity(new City());

        Assert.assertFalse(cityService.insertSubscribed.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putBookSubscribesUpdateBeforeRedirect() {
        RecordingCityService cityService = new RecordingCityService();
        CityController controller = newController(cityService);

        Mono<String> redirect = controller.putBook(new City());

        Assert.assertFalse(cityService.updateSubscribed.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCitySubscribesDeleteBeforeRedirect() {
        RecordingCityService cityService = new RecordingCityService();
        CityController controller = newController(cityService);

        Mono<String> redirect = controller.deleteCity(1L);

        Assert.assertFalse(cityService.deleteSubscribed.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(cityService.deleteSubscribed.get());
    }

    private static CityController newController(CityService cityService) {
        CityController controller = new CityController();
        controller.cityService = cityService;
        return controller;
    }

    private static final class RecordingCityService implements CityService {
        private final AtomicBoolean insertSubscribed = new AtomicBoolean();
        private final AtomicBoolean updateSubscribed = new AtomicBoolean();
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean();

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
            return Mono.defer(() -> {
                deleteSubscribed.set(true);
                return Mono.empty();
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
