package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CityControllerTest {

    @Mock
    private CityService cityService;

    @InjectMocks
    private CityController cityController;

    @Test
    public void postCitySubscribesToInsertBeforeRedirect() {
        City city = new City();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        when(cityService.insertByCity(city)).thenReturn(Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        }));

        String viewName = cityController.postCity(city).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(subscribed.get());
    }

    @Test
    public void putCitySubscribesToUpdateBeforeRedirect() {
        City city = new City();
        AtomicBoolean subscribed = new AtomicBoolean(false);
        when(cityService.update(city)).thenReturn(Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        }));

        String viewName = cityController.putCity(city).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(subscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirect() {
        Long cityId = 1L;
        AtomicBoolean subscribed = new AtomicBoolean(false);
        when(cityService.delete(cityId)).thenReturn(Mono.defer(() -> {
            subscribed.set(true);
            return Mono.empty();
        }));

        String viewName = cityController.deleteCity(cityId).block();

        assertEquals("redirect:/city", viewName);
        assertTrue(subscribed.get());
    }
}
