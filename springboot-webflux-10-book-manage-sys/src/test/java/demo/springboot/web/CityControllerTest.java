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
    public void postCityReturnsSaveMonoBeforeRedirecting() {
        City city = new City();
        AtomicBoolean saved = new AtomicBoolean(false);
        CityController controller = new CityController();
        controller.cityService = new RecordingCityService(
                Mono.defer(() -> {
                    saved.set(true);
                    return Mono.just(city);
                }),
                Mono.empty(),
                Mono.empty());

        Mono<String> redirect = controller.postCity(city);

        Assert.assertFalse(saved.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(saved.get());
    }

    @Test
    public void putBookReturnsUpdateMonoBeforeRedirecting() {
        City city = new City();
        AtomicBoolean updated = new AtomicBoolean(false);
        CityController controller = new CityController();
        controller.cityService = new RecordingCityService(
                Mono.empty(),
                Mono.defer(() -> {
                    updated.set(true);
                    return Mono.just(city);
                }),
                Mono.empty());

        Mono<String> redirect = controller.putBook(city);

        Assert.assertFalse(updated.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(updated.get());
    }

    @Test
    public void deleteCityReturnsDeleteMonoBeforeRedirecting() {
        AtomicBoolean deleted = new AtomicBoolean(false);
        CityController controller = new CityController();
        controller.cityService = new RecordingCityService(
                Mono.empty(),
                Mono.empty(),
                Mono.<Void>fromRunnable(() -> deleted.set(true)));

        Mono<String> redirect = controller.deleteCity(1L);

        Assert.assertFalse(deleted.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(deleted.get());
    }

    private static class RecordingCityService implements CityService {

        private final Mono<City> insertResult;
        private final Mono<City> updateResult;
        private final Mono<Void> deleteResult;

        RecordingCityService(Mono<City> insertResult, Mono<City> updateResult, Mono<Void> deleteResult) {
            this.insertResult = insertResult;
            this.updateResult = updateResult;
            this.deleteResult = deleteResult;
        }

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return insertResult;
        }

        @Override
        public Mono<City> update(City city) {
            return updateResult;
        }

        @Override
        public Mono<Void> delete(Long id) {
            return deleteResult;
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
