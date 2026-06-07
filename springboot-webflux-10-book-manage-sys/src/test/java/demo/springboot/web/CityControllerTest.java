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
    public void postCitySubscribesToInsertBeforeRedirecting() {
        FakeCityService cityService = new FakeCityService();
        CityController cityController = cityController(cityService);

        Mono<String> viewName = cityController.postCity(new City());

        Assert.assertFalse(cityService.insertSubscribed.get());
        Assert.assertEquals("redirect:/city", viewName.block());
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putCitySubscribesToUpdateBeforeRedirecting() {
        FakeCityService cityService = new FakeCityService();
        CityController cityController = cityController(cityService);

        Mono<String> viewName = cityController.putCity(new City());

        Assert.assertFalse(cityService.updateSubscribed.get());
        Assert.assertEquals("redirect:/city", viewName.block());
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirecting() {
        FakeCityService cityService = new FakeCityService();
        CityController cityController = cityController(cityService);

        Mono<String> viewName = cityController.deleteCity(1L);

        Assert.assertFalse(cityService.deleteSubscribed.get());
        Assert.assertEquals("redirect:/city", viewName.block());
        Assert.assertTrue(cityService.deleteSubscribed.get());
    }

    private CityController cityController(CityService cityService) {
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        return cityController;
    }

    private static class FakeCityService implements CityService {

        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(final City city) {
            return Mono.fromSupplier(() -> {
                insertSubscribed.set(true);
                return city;
            });
        }

        @Override
        public Mono<City> update(final City city) {
            return Mono.fromSupplier(() -> {
                updateSubscribed.set(true);
                return city;
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(() -> deleteSubscribed.set(true)).then();
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
