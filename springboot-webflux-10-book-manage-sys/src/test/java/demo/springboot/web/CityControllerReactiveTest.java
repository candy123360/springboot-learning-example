package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class CityControllerReactiveTest {

    @Test
    public void postCityRunsInsertBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);
        City city = city(1L);

        Assert.assertEquals("redirect:/city", cityController.postCity(city).block());

        Assert.assertSame(city, cityService.insertedCity.get());
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void putBookRunsUpdateBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);
        City city = city(2L);

        Assert.assertEquals("redirect:/city", cityController.putBook(city).block());

        Assert.assertSame(city, cityService.updatedCity.get());
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void deleteCityRunsDeleteBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);

        Assert.assertEquals("redirect:/city", cityController.deleteCity(3L).block());

        Assert.assertEquals(3L, cityService.deletedId.get());
        Assert.assertTrue(cityService.deleteSubscribed.get());
    }

    @Test
    public void getCityListAddsCollectedCitiesToModel() {
        RecordingCityService cityService = new RecordingCityService();
        City city = city(4L);
        cityService.city = city;
        CityController cityController = cityController(cityService);
        ExtendedModelMap model = new ExtendedModelMap();

        Assert.assertEquals("cityList", cityController.getCityList(model).block());

        List cityList = (List) model.asMap().get("cityList");
        Assert.assertEquals(1, cityList.size());
        Assert.assertSame(city, cityList.get(0));
    }

    private CityController cityController(RecordingCityService cityService) {
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        return cityController;
    }

    private City city(Long id) {
        City city = new City();
        city.setId(id);
        return city;
    }

    private static class RecordingCityService implements CityService {

        private City city;
        private AtomicReference<City> insertedCity = new AtomicReference<>();
        private AtomicReference<City> updatedCity = new AtomicReference<>();
        private AtomicLong deletedId = new AtomicLong();
        private AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private AtomicBoolean deleteSubscribed = new AtomicBoolean(false);

        @Override
        public Flux<City> findAll() {
            return Flux.just(city);
        }

        @Override
        public Mono<City> insertByCity(City city) {
            insertedCity.set(city);
            return Mono.fromSupplier(() -> {
                insertSubscribed.set(true);
                return city;
            });
        }

        @Override
        public Mono<City> update(City city) {
            updatedCity.set(city);
            return Mono.fromSupplier(() -> {
                updateSubscribed.set(true);
                return city;
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            deletedId.set(id);
            return Mono.fromRunnable(() -> deleteSubscribed.set(true));
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.just(city);
        }
    }
}
