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
    public void postCitySubscribesToSaveBeforeRedirecting() {
        City city = new City();
        TrackingCityService cityService = new TrackingCityService(city);
        CityController controller = newControllerWithService(cityService);

        Mono<String> redirect = controller.postCity(city);

        assertRedirectSubscribesToOperation(redirect, cityService.saveSubscribed);
    }

    @Test
    public void putBookSubscribesToUpdateBeforeRedirecting() {
        City city = new City();
        TrackingCityService cityService = new TrackingCityService(city);
        CityController controller = newControllerWithService(cityService);

        Mono<String> redirect = controller.putBook(city);

        assertRedirectSubscribesToOperation(redirect, cityService.updateSubscribed);
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirecting() {
        CityService cityService = new TrackingCityService(new City());
        CityController controller = newControllerWithService(cityService);

        Mono<String> redirect = controller.deleteCity(1L);

        assertRedirectSubscribesToOperation(redirect, ((TrackingCityService) cityService).deleteSubscribed);
    }

    private CityController newControllerWithService(CityService cityService) {
        CityController controller = new CityController();
        controller.cityService = cityService;
        return controller;
    }

    private void assertRedirectSubscribesToOperation(Mono<String> redirect, AtomicBoolean subscribed) {
        Assert.assertFalse(subscribed.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(subscribed.get());
    }

    private static class TrackingCityService implements CityService {
        private final City city;
        private final AtomicBoolean saveSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);

        private TrackingCityService(City city) {
            this.city = city;
        }

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.fromRunnable(() -> saveSubscribed.set(true)).thenReturn(city);
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.fromRunnable(() -> updateSubscribed.set(true)).thenReturn(city);
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(() -> deleteSubscribed.set(true));
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.just(city);
        }
    }
}
