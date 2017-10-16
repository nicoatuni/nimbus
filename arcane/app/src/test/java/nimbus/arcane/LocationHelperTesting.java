package nimbus.arcane;


        import android.location.Location;

        import org.junit.Before;
        import org.junit.Test;
        import org.junit.Assert;
        import org.junit.runner.RunWith;

        import org.mockito.Mockito;
        import org.mockito.runners.MockitoJUnitRunner;

        import static junit.framework.TestCase.assertEquals;

/**
 * Created by Nico Dinata
 */

/**
 * FINISH ME PLEASE
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationHelperTesting {

    @Test
    public void test_shouldReturn() throws Exception {

        LocationHelper locationHelper = new LocationHelper();

        Location source = Mockito.mock(Location.class);
        Location destination = Mockito.mock(Location.class);

        // Mockito.when(locationHelper.WSG84toECEF(source)).thenReturn(new float[] {-37.810068, 144.964106});
        // Mockito.when(locationHelper.WSG84toECEF(destination)).thenReturn(new float[] {-37.818214, 144.967936});

        // IF THIS DOESN'T WORK, ACTIVATE THE DOWN BELOW
        Mockito.doReturn(new float[] {-37.810068f, 144.964106f}).when(locationHelper).WSG84toECEF(source);
        Mockito.doReturn(new float[] {-37.818214f, 144.967936f}).when(locationHelper).WSG84toECEF(destination);

//        // IF THE ONES ABOVE DO NOT WORK, ACTIVATE THIS ONE
//        Mockito.doReturn(new float[] {-37.810068, 144.964106}).when(locationHelper.WSG84toECEF(source));
//        Mockito.doReturn(new float[] {-37.818214, 144.967936}).when(locationHelper.WSG84toECEF(destination));

        assertEquals(locationHelper.distanceFromECEF(locationHelper.WSG84toECEF(source), locationHelper.WSG84toECEF(destination)), 966.23f, 5.0f);
    }
}

