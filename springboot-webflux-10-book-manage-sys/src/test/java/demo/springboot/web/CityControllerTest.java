package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class CityControllerTest {

    @Test
    public void getCityListResolvesCitiesBeforeRendering() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        Model model = new ExtendedModelMap();

        String viewName = cityController.getCityList(model).block();

        Assert.assertEquals("cityList", viewName);
        Assert.assertEquals(cityService.cities, model.asMap().get("cityList"));
        Assert.assertEquals(1, cityService.findAllSubscriptions);
    }

    @Test
    public void writeActionsSubscribeBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        City city = city(3L, "Hangzhou");

        Assert.assertEquals("redirect:/city", cityController.postCity(city).block());
        Assert.assertEquals("redirect:/city", cityController.putBook(city).block());
        Assert.assertEquals("redirect:/city", cityController.deleteCity(city.getId()).block());

        Assert.assertEquals(1, cityService.insertSubscriptions);
        Assert.assertEquals(1, cityService.updateSubscriptions);
        Assert.assertEquals(1, cityService.deleteSubscriptions);
        Assert.assertSame(city, cityService.insertedCity);
        Assert.assertSame(city, cityService.updatedCity);
        Assert.assertEquals(city.getId(), cityService.deletedId);
    }

    private static City city(Long id, String cityName) {
        City city = new City();
        city.setId(id);
        city.setProvinceId(1L);
        city.setCityName(cityName);
        city.setDescription(cityName + " description");
        return city;
    }

    private static class RecordingCityService implements CityService {

        private final List<City> cities = Arrays.asList(city(1L, "Shanghai"), city(2L, "Beijing"));

        private int findAllSubscriptions;
        private int insertSubscriptions;
        private int updateSubscriptions;
        private int deleteSubscriptions;

        private City insertedCity;
        private City updatedCity;
        private Long deletedId;

        @Override
        public Flux<City> findAll() {
            return Flux.defer(() -> {
                findAllSubscriptions++;
                return Flux.fromIterable(cities);
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
                deletedId = id;
                return Mono.<Void>empty();
            });
        }

        @Override
        public Mono<City> findById(Long id) {
            return Mono.defer(() -> Mono.just(cities.get(0)));
        }
    }
}
