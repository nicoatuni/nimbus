package nimbus.arcane;

import android.location.Location;

/**
 * Created by ntdat on 1/16/17.
 */
/**
 * Last Edited by Arnold 10/7/17
 */

public class ARPoint {
    Location location;

    public ARPoint(double lat, double lon) {
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
    }

    public Location getLocation() {
        return location;
    }
}
