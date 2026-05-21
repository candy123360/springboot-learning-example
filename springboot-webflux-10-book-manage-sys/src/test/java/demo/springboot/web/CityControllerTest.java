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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class CityControllerTest {

    @Test
    public void getCityListResolvesCitiesBeforeRendering() {
        City city = city(1L, "Shanghai");
        List<City> cities = Arrays.asList(city);
        AtomicBoolean subscribed = new AtomicBoolean(false);
        StubCityService cityService = new StubCityService();
        cityService.all = Flux.defer(() -> {
            subscribed.set(true);
            return Flux.fromIterable(cities);
        });
        CityController controller = controller(cityService);
        Model model = new ExtendedModelMap();

        String viewName = controller.getCityList(model).block();

        Assert.assertTrue(subscribed.get());
        Assert.assertEquals("cityList", viewName);
        Assert.assertEquals(cities, model.asMap().get("cityList"));
    }

    @Test
    public void postCitySubscribesToSaveBeforeRedirecting() {
        City city = city(1L, "Shanghai");
        AtomicBoolean subscribed = new AtomicBoolean(false);
        StubCityService cityService = new StubCityService();
        cityService.insert = savedCity -> Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(savedCity);
        });
        CityController controller = controller(cityService);

        String viewName = controller.postCity(city).block();

        Assert.assertTrue(subscribed.get());
        Assert.assertEquals("redirect:/city", viewName);
    }

    @Test
    public void getCityResolvesCityBeforeRenderingForm() {
        City city = city(1L, "Shanghai");
        AtomicBoolean subscribed = new AtomicBoolean(false);
        StubCityService cityService = new StubCityService();
        cityService.findById = id -> Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        });
        CityController controller = controller(cityService);
        Model model = new ExtendedModelMap();

        String viewName = controller.getCity(1L, model).block();

        Assert.assertTrue(subscribed.get());
        Assert.assertEquals("cityForm", viewName);
        Assert.assertSame(city, model.asMap().get("city"));
        Assert.assertEquals("update", model.asMap().get("action"));
    }

    @Test
    public void putBookSubscribesToUpdateBeforeRedirecting() {
        City city = city(1L, "Shanghai");
        AtomicBoolean subscribed = new AtomicBoolean(false);
        StubCityService cityService = new StubCityService();
        cityService.update = savedCity -> Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(savedCity);
        });
        CityController controller = controller(cityService);

        String viewName = controller.putBook(city).block();

        Assert.assertTrue(subscribed.get());
        Assert.assertEquals("redirect:/city", viewName);
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirecting() {
        AtomicBoolean subscribed = new AtomicBoolean(false);
        StubCityService cityService = new StubCityService();
        cityService.delete = id -> Mono.fromRunnable(() -> subscribed.set(true));
        CityController controller = controller(cityService);

        String viewName = controller.deleteCity(1L).block();

        Assert.assertTrue(subscribed.get());
        Assert.assertEquals("redirect:/city", viewName);
    }

    private CityController controller(CityService cityService) {
        CityController controller = new CityController();
        controller.cityService = cityService;
        return controller;
    }

    private City city(Long id, String cityName) {
        City city = new City();
        city.setId(id);
        city.setCityName(cityName);
        return city;
    }

    private static class StubCityService implements CityService {
        private Flux<City> all = Flux.empty();
        private Function<City, Mono<City>> insert = city -> Mono.error(new AssertionError("Unexpected insert"));
        private Function<City, Mono<City>> update = city -> Mono.error(new AssertionError("Unexpected update"));
        private Function<Long, Mono<Void>> delete = id -> Mono.error(new AssertionError("Unexpected delete"));
        private Function<Long, Mono<City>> findById = id -> Mono.empty();

        @Override
        public Flux<City> findAll() {
            return all;
        }

        @Override
        public Mono<City> insertByCity(City city) {
            return insert.apply(city);
        }

        @Override
        public Mono<City> update(City city) {
            return update.apply(city);
        }

        @Override
        public Mono<Void> delete(Long id) {
            return delete.apply(id);
        }

        @Override
        public Mono<City> findById(Long id) {
            return findById.apply(id);
        }
    }
}
