package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CityControllerTest {

    @Test
    public void postCitySubscribesToInsertBeforeRedirecting() {
        CityController controller = new CityController();
        StubCityService cityService = new StubCityService();
        controller.cityService = cityService;

        String viewName = controller.postCity(new City()).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putCitySubscribesToUpdateBeforeRedirecting() {
        CityController controller = new CityController();
        StubCityService cityService = new StubCityService();
        controller.cityService = cityService;

        String viewName = controller.putCity(new City()).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirecting() {
        CityController controller = new CityController();
        StubCityService cityService = new StubCityService();
        controller.cityService = cityService;

        String viewName = controller.deleteCity(1L).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(cityService.deleteSubscribed.get());
    }

    private static class StubCityService implements CityService {

        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(final City city) {
            return Mono.defer(() -> {
                insertSubscribed.set(true);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(final City city) {
            return Mono.defer(() -> {
                updateSubscribed.set(true);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(final Long id) {
            return Mono.defer(() -> {
                deleteSubscribed.set(true);
                return Mono.empty();
            });
        }

        @Override
        public Mono<City> findById(final Long id) {
            return Mono.just(new City());
        }
    }
}
