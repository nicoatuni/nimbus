package nimbus.arcane;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Elbert on 11/10/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MapFragmentTesting {

    @Test
    public void getDirectionsURLShouldReturnTrueIfCorrectURL() throws Exception {
        String correctURL = "https://maps.googleapis.com/maps/api/directions/json?origin=-37.8,144.959&destination=-37.81,144.9593&sensor=false&mode=walking";
        MapFragment mapFragment=new MapFragment();

        Location location1 = Mockito.mock(Location.class);
        Mockito.when(location1.getLatitude()).thenReturn(-37.8);
        Mockito.when(location1.getLongitude()).thenReturn(144.959);
        LatLng origin=new LatLng(location1.getLatitude(),location1.getLongitude());

        Location location2 = Mockito.mock(Location.class);
        Mockito.when(location2.getLatitude()).thenReturn(-37.81);
        Mockito.when(location2.getLongitude()).thenReturn(144.9593);
        LatLng destination=new LatLng(location2.getLatitude(),location2.getLongitude());

        String testURL=mapFragment.getDirectionsUrl(origin,destination);

        assertEquals(correctURL,testURL);

    }
}
