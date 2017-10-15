package nimbus.arcane;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * @author Nico Dinata
 * FINISH ME PLEASE
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationHelperTesting {

    @Test
    public void test_shouldReturn() throws Exception {
        LocationHelper locationHelper = new LocationHelper();

        Location source = Mockito.mock(Location.class);
        Location destination = Mockito.mock(Location.class);

        Mockito.when(locationHelper.WSG84toECEF(source)).thenReturn(new double[] {-37.810068, 144.964106, 0});
        Mockito.when(locationHelper.WSG84toECEF(destination)).thenReturn(new double[] {-37.818214, 144.967936, 0});

        assertEquals(locationHelper.distanceFromECEF(locationHelper.WSG84toECEF(source), locationHelper.WSG84toECEF(destination)), 966.23, 5.0);
    }
}