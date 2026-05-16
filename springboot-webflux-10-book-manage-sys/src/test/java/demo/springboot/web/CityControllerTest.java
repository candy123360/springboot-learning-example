package demo.springboot.web;

import demo.springboot.domain.City;
import demo.springboot.service.CityService;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CityControllerTest {

    @Test
    public void postCitySubscribesToSaveBeforeRedirecting() {
        City city = new City();
        CityController controller = newControllerWithService(cityServiceSaving(city, "create"));

        Mono<String> redirect = controller.postCity(city);

        assertRedirectSubscribesToOperation(redirect, "create");
    }

    @Test
    public void putBookSubscribesToUpdateBeforeRedirecting() {
        City city = new City();
        CityController controller = newControllerWithService(cityServiceSaving(city, "update"));

        Mono<String> redirect = controller.putBook(city);

        assertRedirectSubscribesToOperation(redirect, "update");
    }

    @Test
    public void deleteCitySubscribesToDeleteBeforeRedirecting() {
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityService cityService = mock(CityService.class);
        when(cityService.delete(1L)).thenReturn(Mono.fromRunnable(() -> deleteSubscribed.set(true)));
        CityController controller = newControllerWithService(cityService);

        Mono<String> redirect = controller.deleteCity(1L);

        Assert.assertFalse(deleteSubscribed.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(deleteSubscribed.get());
    }

    private CityController newControllerWithService(CityService cityService) {
        CityController controller = new CityController();
        controller.cityService = cityService;
        return controller;
    }

    private CityService cityServiceSaving(City city, String operationName) {
        AtomicBoolean subscribed = operationSubscription(operationName);
        CityService cityService = mock(CityService.class);
        when(cityService.insertByCity(city)).thenReturn(Mono.fromRunnable(() -> subscribed.set(true)).thenReturn(city));
        when(cityService.update(city)).thenReturn(Mono.fromRunnable(() -> subscribed.set(true)).thenReturn(city));
        return cityService;
    }

    private AtomicBoolean operationSubscription(String operationName) {
        AtomicBoolean subscribed = new AtomicBoolean(false);
        operationSubscriptions.put(operationName, subscribed);
        return subscribed;
    }

    private void assertRedirectSubscribesToOperation(Mono<String> redirect, String operationName) {
        AtomicBoolean subscribed = operationSubscriptions.get(operationName);
        Assert.assertFalse(subscribed.get());
        Assert.assertEquals("redirect:/city", redirect.block());
        Assert.assertTrue(subscribed.get());
    }

    private final java.util.Map<String, AtomicBoolean> operationSubscriptions = new java.util.HashMap<>();
}
