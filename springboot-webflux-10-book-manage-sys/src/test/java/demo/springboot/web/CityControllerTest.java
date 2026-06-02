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
    public void postCitySubscribesToInsertBeforeRedirecting() {
        City city = createCity(1L);

        Mono<String> response = cityController.postCity(city);

        Assert.assertEquals(0, cityService.insertSubscriptions.get());
        Assert.assertEquals("redirect:/city", response.block());
        Assert.assertEquals(1, cityService.insertSubscriptions.get());
        Assert.assertSame(city, cityService.insertedCity);
    }

    @Test
    public void putBookSubscribesToUpdateBeforeRedirecting() {
        City city = createCity(2L);

        Mono<String> response = cityController.putBook(city);

        Assert.assertEquals(0, cityService.updateSubscriptions.get());
        Assert.assertEquals("redirect:/city", response.block());
        Assert.assertEquals(1, cityService.updateSubscriptions.get());
        Assert.assertSame(city, cityService.updatedCity);
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirecting() {
        Mono<String> response = cityController.deleteCity(3L);

        Assert.assertEquals(0, cityService.deleteSubscriptions.get());
        Assert.assertEquals("redirect:/city", response.block());
        Assert.assertEquals(1, cityService.deleteSubscriptions.get());
        Assert.assertEquals(Long.valueOf(3L), cityService.deletedId);
    }

    private City createCity(Long id) {
        City city = new City();
        city.setId(id);
        city.setProvinceId(10L);
        city.setCityName("Hangzhou");
        city.setDescription("A city");
        return city;
    }

    private static class RecordingCityService implements CityService {

        private final AtomicInteger insertSubscriptions = new AtomicInteger();
        private final AtomicInteger updateSubscriptions = new AtomicInteger();
        private final AtomicInteger deleteSubscriptions = new AtomicInteger();

        private City insertedCity;
        private City updatedCity;
        private Long deletedId;

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.defer(() -> {
                insertSubscriptions.incrementAndGet();
                insertedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.defer(() -> {
                updateSubscriptions.incrementAndGet();
                updatedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.defer(() -> {
                deleteSubscriptions.incrementAndGet();
                deletedId = id;
                return Mono.empty();
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.just(createCity(id));
        }

        private City createCity(Long id) {
            City city = new City();
            city.setId(id);
            return city;
        }
    }
}
