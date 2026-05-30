package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CityControllerTest {

    @Test
    public void postCityRunsSaveBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);

        String viewName = cityController.postCity(city()).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertTrue(cityService.insertSubscribed.get());
    }

    @Test
    public void deleteCityRunsDeleteBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);

        String viewName = cityController.deleteCity(42L).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(42L, cityService.deletedId.get());
        Assert.assertTrue(cityService.deleteSubscribed.get());
    }

    @Test
    public void putBookRunsUpdateBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);

        String viewName = cityController.putBook(city()).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertTrue(cityService.updateSubscribed.get());
    }

    @Test
    public void getCityListResolvesCitiesIntoModel() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);
        Model model = new ExtendedModelMap();

        String viewName = cityController.getCityList(model).block();

        Assert.assertEquals("cityList", viewName);
        Assert.assertTrue(cityService.findAllSubscribed.get());
        Object cityList = model.asMap().get("cityList");
        Assert.assertTrue(cityList instanceof List);
        Assert.assertEquals(1, ((List) cityList).size());
    }

    @Test
    public void getCityResolvesCityIntoModel() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);
        Model model = new ExtendedModelMap();

        String viewName = cityController.getCity(42L, model).block();

        Assert.assertEquals("cityForm", viewName);
        Assert.assertTrue(cityService.findByIdSubscribed.get());
        Assert.assertSame(cityService.city, model.asMap().get("city"));
        Assert.assertEquals("update", model.asMap().get("action"));
    }

    private CityController cityController(RecordingCityService cityService) {
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        return cityController;
    }

    private City city() {
        City city = new City();
        city.setId(42L);
        city.setProvinceId(1L);
        city.setCityName("WL");
        city.setDescription("WL IS GOOD");
        return city;
    }

    private static class RecordingCityService implements CityService {
        private final City city = new City();
        private final AtomicBoolean findAllSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean insertSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean updateSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        private final AtomicBoolean findByIdSubscribed = new AtomicBoolean(false);
        private final AtomicLong deletedId = new AtomicLong();

        private RecordingCityService() {
            city.setId(42L);
            city.setProvinceId(1L);
            city.setCityName("WL");
            city.setDescription("WL IS GOOD");
        }

        @Override
        public Flux<City> findAll() {
            return Flux.defer(() -> {
                findAllSubscribed.set(true);
                return Flux.just(city);
            });
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.defer(() -> {
                insertSubscribed.set(true);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.defer(() -> {
                updateSubscribed.set(true);
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.fromRunnable(() -> {
                deletedId.set(id);
                deleteSubscribed.set(true);
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.defer(() -> {
                findByIdSubscribed.set(true);
                return Mono.just(city);
            });
        }
    }
}
