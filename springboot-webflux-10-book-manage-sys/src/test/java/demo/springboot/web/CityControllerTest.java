package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

public class CityControllerTest {

    private CityController cityController;
    private RecordingCityService cityService;

    @Before
    public void setUp() {
        cityController = new CityController();
        cityService = new RecordingCityService();
        cityController.cityService = cityService;
    }

    @Test
    public void postCityRunsSaveBeforeRedirecting() {
        final Mono<String> result = cityController.postCity(new City());

        Assert.assertEquals(0, cityService.saves.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertEquals(1, cityService.saves.get());
    }

    @Test
    public void putBookRunsUpdateBeforeRedirecting() {
        final Mono<String> result = cityController.putBook(new City());

        Assert.assertEquals(0, cityService.updates.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertEquals(1, cityService.updates.get());
    }

    @Test
    public void deleteCityRunsDeleteBeforeRedirecting() {
        final Mono<String> result = cityController.deleteCity(1L);

        Assert.assertEquals(0, cityService.deletes.get());
        Assert.assertEquals("redirect:/city", result.block());
        Assert.assertEquals(1, cityService.deletes.get());
    }

    private static class RecordingCityService implements CityService {

        private final AtomicInteger saves = new AtomicInteger();
        private final AtomicInteger updates = new AtomicInteger();
        private final AtomicInteger deletes = new AtomicInteger();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.fromCallable(() -> {
                saves.incrementAndGet();
                return city;
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.fromCallable(() -> {
                updates.incrementAndGet();
                return city;
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(deletes::incrementAndGet);
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
