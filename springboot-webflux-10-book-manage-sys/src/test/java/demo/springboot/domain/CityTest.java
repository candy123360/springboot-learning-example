package demo.springboot.domain;

import org.junit.Test;
import org.springframework.data.annotation.Id;

import static org.junit.Assert.assertTrue;

public class CityTest {

    @Test
    public void idFieldIsMongoIdentifier() throws Exception {
        assertTrue(City.class.getDeclaredField("id").isAnnotationPresent(Id.class));
    }
}
