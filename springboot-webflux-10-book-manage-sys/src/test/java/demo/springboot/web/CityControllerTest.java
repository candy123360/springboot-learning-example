package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CityControllerTest {

    private static final String REDIRECT_TO_CITY_URL = "redirect:/city";

    @Test
    public void postCitySavesBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);
        City city = new City();

        Mono<String> result = cityController.postCity(city);

        assertNull(cityService.insertedCity);
        assertEquals(REDIRECT_TO_CITY_URL, result.block(Duration.ofSeconds(1)));
        assertSame(city, cityService.insertedCity);
    }

    @Test
    public void putCityUpdatesBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);
        City city = new City();

        Mono<String> result = cityController.putCity(city);

        assertNull(cityService.updatedCity);
        assertEquals(REDIRECT_TO_CITY_URL, result.block(Duration.ofSeconds(1)));
        assertSame(city, cityService.updatedCity);
    }

    @Test
    public void deleteCityDeletesBeforeRedirecting() {
        RecordingCityService cityService = new RecordingCityService();
        CityController cityController = cityController(cityService);

        Mono<String> result = cityController.deleteCity(7L);

        assertNull(cityService.deletedId);
        assertEquals(REDIRECT_TO_CITY_URL, result.block(Duration.ofSeconds(1)));
        assertEquals(Long.valueOf(7L), cityService.deletedId);
    }

    private CityController cityController(RecordingCityService cityService) {
        CityController cityController = new CityController();
        cityController.cityService = cityService;
        return cityController;
    }

    private static class RecordingCityService implements CityService {

        private City insertedCity;
        private City updatedCity;
        private Long deletedId;

        @Override
        public Flux<City> findAll() {
            return Flux.empty();
        }

        @Override
        public Mono<City> insertByCity(final City city) {
            return Mono.defer(() -> {
                insertedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<City> update(final City city) {
            return Mono.defer(() -> {
                updatedCity = city;
                return Mono.just(city);
            });
        }

        @Override
        public Mono<Void> delete(final Long id) {
            return Mono.defer(() -> {
                deletedId = id;
                return Mono.empty();
            });
        }

        @Override
        public Mono<City> findById(final Long id) {
            return Mono.empty();
        }
    }
}
