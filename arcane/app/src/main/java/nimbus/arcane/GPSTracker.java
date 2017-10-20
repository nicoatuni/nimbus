package nimbus.arcane;

import android.*;
import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by Richard Aldrich on 3/10/2017.
 * Last edited by Leonardus Elbert Putra 20/10/2017
 */

public class GPSTracker extends Service implements LocationListener {

    public final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location;
    protected LocationManager locationManager;

    public GPSTracker(Context context) {
        this.context = context;
    }

    // Check whether GPS/Location is enabled or not
    public void checkGPS() {

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // Getting GPS Status
        isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
        // Getting Network Status
        isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

        String gps_message = "Please Enable your GPS/Location Service";
        String net_message = "Please Enable your Network Service";

        if (!isGPSEnabled) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(gps_message);

            dialog.setPositiveButton("GPS Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(gpsIntent);

                }
            });

            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    /*
                        take the last known location from database
                     */

                }
            });

            dialog.show();

        }

    }

    // return last known location if user has used the app otherwise request location update from
    //location manager
    public Location getLocation() {

        try {

            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled) {

                    if (location == null) {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                        if (locationManager != null) {

                            location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

                        }

                    }
                }

                if (location == null) {

                    if (isNetworkEnabled) {

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

                        if (locationManager != null) {

                            location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

                        }
                    }

                }

            }

        } catch (Exception e) {

            Toast.makeText(context, "Location not detected", Toast.LENGTH_LONG).show();

        }

        return location;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
