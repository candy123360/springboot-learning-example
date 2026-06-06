package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

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
        City city = new City();

        String viewName = cityController.postCity(city).block();

        assertEquals("redirect:/city", viewName);
        assertEquals(1, cityService.insertSubscriptions.get());
    }

    @Test
    public void putBookRunsUpdateBeforeRedirecting() {
        City city = new City();

        String viewName = cityController.putBook(city).block();

        assertEquals("redirect:/city", viewName);
        assertEquals(1, cityService.updateSubscriptions.get());
    }

    @Test
    public void deleteCityRunsDeleteBeforeRedirecting() {
        String viewName = cityController.deleteCity(1L).block();

        assertEquals("redirect:/city", viewName);
        assertEquals(1, cityService.deleteSubscriptions.get());
    }

    private static class RecordingCityService implements CityService {

        private final AtomicInteger insertSubscriptions = new AtomicInteger();
        private final AtomicInteger updateSubscriptions = new AtomicInteger();
        private final AtomicInteger deleteSubscriptions = new AtomicInteger();

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.create(sink -> {
                insertSubscriptions.incrementAndGet();
                sink.success(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.create(sink -> {
                updateSubscriptions.incrementAndGet();
                sink.success(city);
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.create(sink -> {
                deleteSubscriptions.incrementAndGet();
                sink.success();
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
