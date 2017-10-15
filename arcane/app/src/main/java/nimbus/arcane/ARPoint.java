package nimbus.arcane;

import android.location.Location;

/**
 * Created by ntdat on 1/16/17.
 * Github : https://github.com/dat-ng/ar-location-based-android
 */
/**
 * Last Edited by Arnold Angelo 10/15/17
 */

//Represent the data of the checkpoints and destination point in the AR View
public class ARPoint {
    Location location;

    public ARPoint(double lat, double lon) {
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
    }

    //Get the Location of the ARPoint
    public Location getLocation() {
        return location;
    }
}
