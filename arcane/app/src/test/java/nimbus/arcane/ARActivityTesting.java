package nimbus.arcane;

import android.location.Location;
import android.provider.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.*;

/**
 * Created by Arnold on 10/12/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class ARActivityTesting {
    //Expected Results Obtained from : http://instantglobe.com/tools/calc-dist-to-Lat-Long.html

    @Test
    public void coordinateToAngleTest1() throws Exception {
        ARActivity sampleActivity = new ARActivity();

        Location location1 = Mockito.mock(Location.class);
        Location location2 = Mockito.mock(Location.class);

        Mockito.when(location1.getLatitude()).thenReturn(-37.0);
        Mockito.when(location1.getLongitude()).thenReturn(144.0);
        Mockito.when(location2.getLatitude()).thenReturn(-37.5);
        Mockito.when(location2.getLongitude()).thenReturn(144.5);

        assertEquals(141.631, sampleActivity.angleFromCoordinate(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude()), 3);
    }

    @Test
    public void coordinateToAngleTest2() throws Exception {
        ARActivity sampleActivity = new ARActivity();

        Location location1 = Mockito.mock(Location.class);
        Location location2 = Mockito.mock(Location.class);

        Mockito.when(location1.getLatitude()).thenReturn(-37.7964);
        Mockito.when(location1.getLongitude()).thenReturn(144.9612);
        Mockito.when(location2.getLatitude()).thenReturn(-37.8108);
        Mockito.when(location2.getLongitude()).thenReturn(144.9631);

        assertEquals(174.04891184209873, sampleActivity.angleFromCoordinate(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude()), 3);

    }

    @Test
    public void coordinateToAngleTest3() throws Exception {
        ARActivity sampleActivity = new ARActivity();

        Location location1 = Mockito.mock(Location.class);
        Location location2 = Mockito.mock(Location.class);

        Mockito.when(location1.getLatitude()).thenReturn(-37.810068);
        Mockito.when(location1.getLongitude()).thenReturn(144.964106);
        Mockito.when(location2.getLatitude()).thenReturn(-37.818214);
        Mockito.when(location2.getLongitude()).thenReturn(144.967936);

        assertEquals(159.624, sampleActivity.angleFromCoordinate(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude()), 3);
    }

    @Test
    public void coordinateToAngleTest4() throws Exception {
        ARActivity sampleActivity = new ARActivity();

        Location location1 = Mockito.mock(Location.class);
        Location location2 = Mockito.mock(Location.class);

        Mockito.when(location1.getLatitude()).thenReturn(39.099912);
        Mockito.when(location1.getLongitude()).thenReturn(-94.581213);
        Mockito.when(location2.getLatitude()).thenReturn(38.627089);
        Mockito.when(location2.getLongitude()).thenReturn(-90.200203);

        assertEquals(96.51, sampleActivity.angleFromCoordinate(location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude()), 3);
    }

    @Test
    public void deltaAngleTest() throws Exception {
        ARActivity sampleActivity = new ARActivity();

        Location location1 = Mockito.mock(Location.class);
        Location location2 = Mockito.mock(Location.class);

        Mockito.when(location1.getLatitude()).thenReturn(-37.0);
        Mockito.when(location1.getLongitude()).thenReturn(144.0);
        Mockito.when(location2.getLatitude()).thenReturn(-37.5);
        Mockito.when(location2.getLongitude()).thenReturn(144.5);

        assertEquals((141.631-90),sampleActivity.calculatedAngle(90,location1.getLatitude(), location1.getLongitude(), location2.getLatitude(), location2.getLongitude()), 3);
    }

}