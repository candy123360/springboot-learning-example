package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CityControllerTest {

    private static final String REDIRECT_TO_CITY_URL = "redirect:/city";

    @Test
    public void postCitySubscribesToSaveBeforeRedirect() {
        City city = new City();
        CityService cityService = mock(CityService.class);
        AtomicBoolean subscribed = new AtomicBoolean(false);
        when(cityService.insertByCity(city)).thenReturn(Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        }));

        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Assert.assertEquals(REDIRECT_TO_CITY_URL, cityController.postCity(city).block());
        Assert.assertTrue(subscribed.get());
    }

    @Test
    public void putCitySubscribesToUpdateBeforeRedirect() {
        City city = new City();
        CityService cityService = mock(CityService.class);
        AtomicBoolean subscribed = new AtomicBoolean(false);
        when(cityService.update(city)).thenReturn(Mono.defer(() -> {
            subscribed.set(true);
            return Mono.just(city);
        }));

        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Assert.assertEquals(REDIRECT_TO_CITY_URL, cityController.putCity(city).block());
        Assert.assertTrue(subscribed.get());
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirect() {
        Long cityId = 1L;
        CityService cityService = mock(CityService.class);
        AtomicBoolean subscribed = new AtomicBoolean(false);
        when(cityService.delete(cityId)).thenReturn(Mono.fromRunnable(() -> subscribed.set(true)).then());

        CityController cityController = new CityController();
        cityController.cityService = cityService;

        Assert.assertEquals(REDIRECT_TO_CITY_URL, cityController.deleteCity(cityId).block());
        Assert.assertTrue(subscribed.get());
    }

    @Test
    public void deleteCityDoesNotExposeDestructiveGetRoute() throws NoSuchMethodException {
        RequestMapping mapping = CityController.class
                .getMethod("deleteCity", Long.class)
                .getAnnotation(RequestMapping.class);

        Assert.assertArrayEquals(new RequestMethod[] {RequestMethod.POST}, mapping.method());
    }
}
