package nimbus.arcane;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Boolean firstTime = true;

    private static LocationManager locationManager;
    private static LocationListener locationListener;

    private GPSTracker gpsTracker;
    private Location mLocation;

    private LatLng mUserLocation;
    private LatLng mDestination;
    private double latitude, longitude;

    private Toolbar mToolbar;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {

                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));

                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mToolbar = (Toolbar) findViewById(R.id.map_bar_layout);
        mToolbar.setTitle("Set Destination");

        gpsTracker = new GPSTracker(MapActivity.this);
        gpsTracker.checkGPS();

        int permission_all = 1;
        int check_permission;
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        // checking permission
        check_permission = MapFragment.hasPermissions(MapActivity.this, permissions);
        if (check_permission == 1) {

            ActivityCompat.requestPermissions(this, permissions, permission_all);

        }

        mLocation = gpsTracker.getLocation();

        if (mLocation != null) {

            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();

        } else {

            /*
                get location from database
             */

        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));

                if (firstTime) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 17));
                    firstTime = false;

                }
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
        };

        mUserLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 17));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                // Creating MarkerOptions
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position of the marker
                markerOptions.position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                // add marker to the Google Map Android API v2
                mMap.addMarker(markerOptions.title("set as destination"));
                mDestination = latLng;

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                mDestination = marker.getPosition();
                Toast.makeText(MapActivity.this, mDestination.toString(), Toast.LENGTH_LONG).show();

                /*
                    the destination position will be sent to the chat from here
                 */

            }
        });
    }
}