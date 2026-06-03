package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
    public void getCityListCollectsCitiesBeforeRendering() {
        Model model = new ExtendedModelMap();

        String viewName = cityController.getCityList(model).block();

        Assert.assertEquals("cityList", viewName);
        Assert.assertEquals(1, cityService.findAllSubscriptions);
        Assert.assertTrue(model.asMap().get("cityList") instanceof List);
        Assert.assertEquals("WL", ((City) ((List) model.asMap().get("cityList")).get(0)).getCityName());
    }

    @Test
    public void postCitySubscribesInsertBeforeRedirecting() {
        City city = city("create", 1L);

        String viewName = cityController.postCity(city).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(city, cityService.insertedCity);
        Assert.assertEquals(1, cityService.insertSubscriptions);
    }

    @Test
    public void getCityResolvesCityBeforeRenderingForm() {
        Model model = new ExtendedModelMap();

        String viewName = cityController.getCity(1L, model).block();

        Assert.assertEquals("cityForm", viewName);
        Assert.assertEquals("update", model.asMap().get("action"));
        Assert.assertEquals(cityService.cityToFind, model.asMap().get("city"));
        Assert.assertEquals(1L, cityService.findById.get());
        Assert.assertEquals(1, cityService.findByIdSubscriptions);
    }

    @Test
    public void putCitySubscribesUpdateBeforeRedirecting() {
        City city = city("update", 2L);

        String viewName = cityController.putCity(city).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(city, cityService.updatedCity);
        Assert.assertEquals(1, cityService.updateSubscriptions);
    }

    @Test
    public void deleteCitySubscribesDeleteBeforeRedirecting() {
        String viewName = cityController.deleteCity(3L).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(3L, cityService.deletedId.get());
        Assert.assertEquals(1, cityService.deleteSubscriptions);
    }

    private static City city(String name, Long id) {
        City city = new City();
        city.setId(id);
        city.setProvinceId(2L);
        city.setCityName(name);
        city.setDescription(name + " desc");
        return city;
    }

    private static class RecordingCityService implements CityService {

        private final City cityToFind = city("WL", 1L);
        private final AtomicLong findById = new AtomicLong();
        private final AtomicLong deletedId = new AtomicLong();

        private int findAllSubscriptions;
        private int insertSubscriptions;
        private int updateSubscriptions;
        private int deleteSubscriptions;
        private int findByIdSubscriptions;

        private City insertedCity;
        private City updatedCity;

        @Override
        public Flux<City> findAll() {
            return Flux.defer(() -> {
                findAllSubscriptions++;
                return Flux.just(cityToFind);
            });
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return Mono.defer(() -> {
                insertSubscriptions++;
                insertedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(City city) {
            return Mono.defer(() -> {
                updateSubscriptions++;
                updatedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(Long id) {
            return Mono.defer(() -> {
                deleteSubscriptions++;
                deletedId.set(id);
                return Mono.empty();
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.defer(() -> {
                findByIdSubscriptions++;
                findById.set(id);
                return Mono.just(cityToFind);
            });
        }
    }
}
