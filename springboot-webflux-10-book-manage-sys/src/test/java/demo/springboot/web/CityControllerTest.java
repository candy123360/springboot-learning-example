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
    public void postCityWaitsForInsertBeforeRedirect() {
        AtomicBoolean inserted = new AtomicBoolean(false);
        CityController cityController = cityControllerWith(new FakeCityService(inserted, null, null));

        Mono<String> result = cityController.postCity(new City());

        Assert.assertFalse(inserted.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(inserted.get());
    }

    @Test
    public void putBookWaitsForUpdateBeforeRedirect() {
        AtomicBoolean updated = new AtomicBoolean(false);
        CityController cityController = cityControllerWith(new FakeCityService(null, updated, null));

        Mono<String> result = cityController.putBook(new City());

        Assert.assertFalse(updated.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(updated.get());
    }

    @Test
    public void deleteCityWaitsForDeleteBeforeRedirect() {
        AtomicBoolean deleted = new AtomicBoolean(false);
        CityController cityController = cityControllerWith(new FakeCityService(null, null, deleted));

        Mono<String> result = cityController.deleteCity(1L);

        Assert.assertFalse(deleted.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertTrue(deleted.get());
    }

    private CityController cityControllerWith(CityService cityService) {
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        return cityController;
    }

    private static class FakeCityService implements CityService {

        private final AtomicBoolean inserted;
        private final AtomicBoolean updated;
        private final AtomicBoolean deleted;

        FakeCityService(AtomicBoolean inserted, AtomicBoolean updated, AtomicBoolean deleted) {
            this.inserted = inserted;
            this.updated = updated;
            this.deleted = deleted;
        }

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.fromSupplier(() -> {
                inserted.set(true);
                return city;
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.fromSupplier(() -> {
                updated.set(true);
                return city;
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(() -> deleted.set(true));
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
