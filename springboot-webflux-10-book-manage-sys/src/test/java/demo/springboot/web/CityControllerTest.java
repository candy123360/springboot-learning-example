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
    public void postCityExecutesSaveBeforeRedirect() {
        AtomicInteger saves = new AtomicInteger();
        CityController cityController = controllerWithService(countingService(saves, new AtomicInteger(), new AtomicInteger()));

        String viewName = cityController.postCity(new City()).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(1, saves.get());
    }

    @Test
    public void putBookExecutesUpdateBeforeRedirect() {
        AtomicInteger updates = new AtomicInteger();
        CityController cityController = controllerWithService(countingService(new AtomicInteger(), updates, new AtomicInteger()));

        String viewName = cityController.putBook(new City()).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(1, updates.get());
    }

    @Test
    public void deleteCityExecutesDeleteBeforeRedirect() {
        AtomicInteger deletes = new AtomicInteger();
        CityController cityController = controllerWithService(countingService(new AtomicInteger(), new AtomicInteger(), deletes));

        String viewName = cityController.deleteCity(99L).block();

        Assert.assertEquals("redirect:/city", viewName);
        Assert.assertEquals(1, deletes.get());
    }

    private CityController controllerWithService(CityService cityService) {
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        return cityController;
    }

    private CityService countingService(AtomicInteger saves, AtomicInteger updates, AtomicInteger deletes) {
        return new CityService() {
            @Override
            public Flux<City> findAll() {
                return Flux.empty();
            }

            @Override
            public Mono<City> insertByCity(City city) {
                return Mono.fromRunnable(saves::incrementAndGet).thenReturn(city);
            }

            @Override
            public Mono<City> update(City city) {
                return Mono.fromRunnable(updates::incrementAndGet).thenReturn(city);
            }

            @Override
            public Mono<Void> delete(Long id) {
                return Mono.fromRunnable(deletes::incrementAndGet).then();
            }

            @Override
            public Mono<City> findById(Long id) {
                return Mono.empty();
            }
        };
    }
}
