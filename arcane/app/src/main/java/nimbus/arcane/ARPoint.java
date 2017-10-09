package nimbus.arcane;

import android.location.Location;

/**
 * Created by ntdat on 1/16/17.
 */
<<<<<<< HEAD

/**
 * Last Edited by Arnold 10/7/17
 */
=======
>>>>>>> parent of 75cb8ed... Connect AR with Map Fragment Data

public class ARPoint {
    Location location;

    String name;

<<<<<<< HEAD
    public ARPoint(double lat, double lon) {
=======
    public ARPoint(String name, double lat, double lon, double altitude) {
        this.name = name;
>>>>>>> parent of 75cb8ed... Connect AR with Map Fragment Data
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
