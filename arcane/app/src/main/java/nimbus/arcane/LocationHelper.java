package nimbus.arcane;

import android.location.Location;

/**
 * Created by ntdat on 1/13/17.
 */
<<<<<<< HEAD

/**
 * Last Edited by Arnold on 10/7/17
 */
=======
>>>>>>> parent of 75cb8ed... Connect AR with Map Fragment Data

//Class with functions to calculate coordinates in which the object will be rendered in the ARView
public class LocationHelper {
    private final static double WGS84_A = 6378137.0;                  // WGS 84 semi-major axis constant in meters
    private final static double WGS84_E2 = 0.00669437999014;          // square of WGS 84 eccentricity

    //Converting Latitude Longitude Position to Earth Centered-Earth Fixed Position (In metres)
    public static float[] WSG84toECEF(Location location) {
        double radLat = Math.toRadians(location.getLatitude());
        double radLon = Math.toRadians(location.getLongitude());

        float clat = (float) Math.cos(radLat);
        float slat = (float) Math.sin(radLat);
        float clon = (float) Math.cos(radLon);
        float slon = (float) Math.sin(radLon);

        float N = (float) (WGS84_A / Math.sqrt(1.0 - WGS84_E2 * slat * slat));

        //No Altitude
        float x = (float) ((N) * clat * clon);
        float y = (float) ((N) * clat * slon);
        float z = (float) ((N * (1.0 - WGS84_E2)) * slat);

        return new float[] {x , y, z};
    }

    //Converting Earth Centered-Earth Fixed to East North Up Position
    public static float[] ECEFtoENU(Location currentLocation, float[] ecefCurrentLocation, float[] ecefPOI) {
        double radLat = Math.toRadians(currentLocation.getLatitude());
        double radLon = Math.toRadians(currentLocation.getLongitude());

        float clat = (float)Math.cos(radLat);
        float slat = (float)Math.sin(radLat);
        float clon = (float)Math.cos(radLon);
        float slon = (float)Math.sin(radLon);

        float dx = ecefCurrentLocation[0] - ecefPOI[0];
        float dy = ecefCurrentLocation[1] - ecefPOI[1];
        float dz = ecefCurrentLocation[2] - ecefPOI[2];

        float east = -slon*dx + clon*dy;

        float north = -slat*clon*dx - slat*slon*dy + clat*dz;

        float up = clat*clon*dx + clat*slon*dy + slat*dz;

        return new float[] {east , north, up, 1};
    }

    public static float distanceFromECEF(float[] currentLocation, float[] nextPoint) {
        double distance;
        //Still without z which is the altitude
        distance = Math.sqrt(Math.pow((nextPoint[0]-currentLocation[0]),2)+Math.pow((nextPoint[1]-currentLocation[1]),2));
        return (float)distance;
    }
}
