package nimbus.arcane;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * @author Nico Dinata
 * Unit testing for LocationHelper class
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationHelperTesting {

    /**
     * Queensberry tram stop to Arrow on Swanston
     * Expected     : 24.83 m
     * Actual       : 24.55 m
     * Inaccuracy   :  0.28 m
     */
    @Test
    public void test_superClose() throws Exception {
        LocationHelper locationHelper = new LocationHelper();

        Location source = Mockito.mock(Location.class);
        Location destination = Mockito.mock(Location.class);

        Mockito.when(source.getLatitude()).thenReturn(-37.805628);
        Mockito.when(source.getLongitude()).thenReturn(144.963110);

        Mockito.when(destination.getLatitude()).thenReturn(-37.805561);
        Mockito.when(destination.getLongitude()).thenReturn(144.963379);

        assertEquals(24.83, locationHelper.distanceFromECEF(locationHelper.WSG84toECEF(source), locationHelper.WSG84toECEF(destination)), 3.0);
    }

    /**
     * College Square Lygon to Carlton Pizzeria
     * Expected     : 319.38 m
     * Actual       : 318.07 m
     * Inaccuracy   :   1.31 m
     */
    @Test
    public void test_close() throws Exception {
        LocationHelper locationHelper = new LocationHelper();

        Location source = Mockito.mock(Location.class);
        Location destination = Mockito.mock(Location.class);

        Mockito.when(source.getLatitude()).thenReturn(-37.793675);
        Mockito.when(source.getLongitude()).thenReturn(144.968166);

        Mockito.when(destination.getLatitude()).thenReturn(-37.795524);
        Mockito.when(destination.getLongitude()).thenReturn(144.970928);

        assertEquals(319.38, locationHelper.distanceFromECEF(locationHelper.WSG84toECEF(source), locationHelper.WSG84toECEF(destination)), 3.0);
    }

    /**
     * State Library to Flinders St Station
     * Expected     : 966.20 m
     * Actual       : 964.92 m
     * Inaccuracy   :   1.28 m
     */
    @Test
    public void test_medium() throws Exception {
        LocationHelper locationHelper = new LocationHelper();

        Location source = Mockito.mock(Location.class);
        Location destination = Mockito.mock(Location.class);

        Mockito.when(source.getLatitude()).thenReturn(-37.810068);
        Mockito.when(source.getLongitude()).thenReturn(144.964106);

        Mockito.when(destination.getLatitude()).thenReturn(-37.818214);
        Mockito.when(destination.getLongitude()).thenReturn(144.967936);

        assertEquals(966.2, locationHelper.distanceFromECEF(locationHelper.WSG84toECEF(source), locationHelper.WSG84toECEF(destination)), 3.0);
    }

    /**
     * Alice Hoy building to QV
     * Expected     : 1447.44 m
     * Actual       : 1444.91 m
     * Inaccuracy   :    2.53 m
     */
    @Test
    public void test_far() throws Exception {
        LocationHelper locationHelper = new LocationHelper();

        Location source = Mockito.mock(Location.class);
        Location destination = Mockito.mock(Location.class);

        Mockito.when(source.getLatitude()).thenReturn(-37.798632);
        Mockito.when(source.getLongitude()).thenReturn(144.963431);

        Mockito.when(destination.getLatitude()).thenReturn(-37.811609);
        Mockito.when(destination.getLongitude()).thenReturn(144.964763);
        
        assertEquals(1447.44, locationHelper.distanceFromECEF(locationHelper.WSG84toECEF(source), locationHelper.WSG84toECEF(destination)), 5.0);
    }
}