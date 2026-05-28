package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

public class CityControllerTest {

    @Test
    public void postCityReturnsSavePublisher() {
        RecordingCityService cityService = new RecordingCityService();
        CityController controller = new CityController();
        controller.cityService = cityService;

        Mono<String> viewName = controller.postCity(new City());

        Assert.assertEquals(0, cityService.inserts.get());
        Assert.assertEquals("redirect:/city", viewName.block());
        Assert.assertEquals(1, cityService.inserts.get());
    }

    @Test
    public void putBookReturnsUpdatePublisher() {
        RecordingCityService cityService = new RecordingCityService();
        CityController controller = new CityController();
        controller.cityService = cityService;

        Mono<String> viewName = controller.putBook(new City());

        Assert.assertEquals(0, cityService.updates.get());
        Assert.assertEquals("redirect:/city", viewName.block());
        Assert.assertEquals(1, cityService.updates.get());
    }

    @Test
    public void deleteCityReturnsDeletePublisher() {
        RecordingCityService cityService = new RecordingCityService();
        CityController controller = new CityController();
        controller.cityService = cityService;

        Mono<String> viewName = controller.deleteCity(1L);

        Assert.assertEquals(0, cityService.deletes.get());
        Assert.assertEquals("redirect:/city", viewName.block());
        Assert.assertEquals(1, cityService.deletes.get());
    }

    private static class RecordingCityService implements CityService {

        private final AtomicInteger inserts = new AtomicInteger();
        private final AtomicInteger updates = new AtomicInteger();
        private final AtomicInteger deletes = new AtomicInteger();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.fromRunnable(() -> inserts.incrementAndGet()).thenReturn(city);
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.fromRunnable(() -> updates.incrementAndGet()).thenReturn(city);
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(() -> deletes.incrementAndGet());
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
