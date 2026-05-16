package org.spring.springboot.handler;

import org.junit.Assert;
import org.junit.Test;
import org.spring.springboot.dao.CityRepository;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CityHandlerUnitTest {

    @Test
    public void deleteCitySubscribesToRepositoryDeleteBeforeReturningId() {
        AtomicBoolean deleteSubscribed = new AtomicBoolean(false);
        CityRepository cityRepository = mock(CityRepository.class);
        when(cityRepository.deleteById(1L)).thenReturn(Mono.fromRunnable(() -> deleteSubscribed.set(true)));
        CityHandler cityHandler = new CityHandler(cityRepository);

        Mono<Long> deletedId = cityHandler.deleteCity(1L);

        Assert.assertFalse(deleteSubscribed.get());
        Assert.assertEquals(Long.valueOf(1L), deletedId.block());
        Assert.assertTrue(deleteSubscribed.get());
    }

    @Test(expected = IllegalStateException.class)
    public void deleteCityPropagatesRepositoryDeleteFailure() {
        CityRepository cityRepository = mock(CityRepository.class);
        when(cityRepository.deleteById(1L)).thenReturn(Mono.error(new IllegalStateException("delete failed")));
        CityHandler cityHandler = new CityHandler(cityRepository);

        cityHandler.deleteCity(1L).block();
    }
}
