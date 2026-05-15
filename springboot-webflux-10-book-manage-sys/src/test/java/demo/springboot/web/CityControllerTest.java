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
    public void postCitySubscribesToInsertBeforeRedirect() {
        CityController controller = new CityController();
        TrackingCityService cityService = new TrackingCityService();
        controller.cityService = cityService;

        String viewName = controller.postCity(new City()).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putBookSubscribesToUpdateBeforeRedirect() {
        CityController controller = new CityController();
        TrackingCityService cityService = new TrackingCityService();
        controller.cityService = cityService;

        String viewName = controller.putBook(new City()).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirect() {
        CityController controller = new CityController();
        TrackingCityService cityService = new TrackingCityService();
        controller.cityService = cityService;

        String viewName = controller.deleteCity(1L).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertTrue(cityService.deleteSubscribed.get());
    }

    private static class TrackingCityService implements CityService {

        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);

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
