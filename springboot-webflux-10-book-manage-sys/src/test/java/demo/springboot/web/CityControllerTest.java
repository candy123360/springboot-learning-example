package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CityControllerTest {

    private CityController cityController;
    private FakeCityService cityService;

    @Before
    public void setUp() {
        cityController = new CityController();
        cityService = new FakeCityService();
        cityController.cityService = cityService;
    }

    @Test
    public void postCityExecutesInsertBeforeRedirecting() {
        City city = new City();

        Mono<String> view = cityController.postCity(city);

        Assert.assertEquals(0, cityService.insertCount);
        Assert.assertEquals("redirect:/city", view.block());
        Assert.assertEquals(1, cityService.insertCount);
        Assert.assertSame(city, cityService.insertedCity);
    }

    @Test
    public void putBookExecutesUpdateBeforeRedirecting() {
        City city = new City();

        Mono<String> view = cityController.putBook(city);

        Assert.assertEquals(0, cityService.updateCount);
        Assert.assertEquals("redirect:/city", view.block());
        Assert.assertEquals(1, cityService.updateCount);
        Assert.assertSame(city, cityService.updatedCity);
    }

    @Test
    public void deleteCityExecutesDeleteBeforeRedirecting() {
        Mono<String> view = cityController.deleteCity(1L);

        Assert.assertEquals(0, cityService.deleteCount);
        Assert.assertEquals("redirect:/city", view.block());
        Assert.assertEquals(1, cityService.deleteCount);
        Assert.assertEquals(Long.valueOf(1L), cityService.deletedId);
    }

    private static class FakeCityService implements CityService {

        private int insertCount;
        private int updateCount;
        private int deleteCount;
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
                insertCount++;
                insertedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.defer(() -> {
                updateCount++;
                updatedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.<Void>fromRunnable(() -> {
                deleteCount++;
                deletedId = id;
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.empty();
        }
    }
}
