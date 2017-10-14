package nimbus.arcane;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


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

    private Marker mUser;
    private Marker mOther;
    private ProgressDialog mProgress;
    private Polyline mLine;

    private LatLng mUserLocation;
    private LatLng mDestination;
    private double latitude, longitude;

    private String mOtherUser;
    private LatLng mOtherUserLocation;
    private String mOtherUserName;

    private Switch mSwitch;
    private ImageButton myLocation;

    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;

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
//                        mMap.clear();
                        if (mUser != null) {

                            mUser.remove();

                        }
                        mUser = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
                        /*
                            add the other user marker here
                         */
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

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mRootRef.child("Users").child(mCurrentUser.getUid());
        mOtherUser = getIntent().getStringExtra("user");

        mSwitch = (Switch) findViewById(R.id.map_to_ar);
        myLocation = (ImageButton) findViewById(R.id.map_to_location);

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

        mProgress = new ProgressDialog(MapActivity.this);
        mProgress.setTitle("Fetching GPS location");
        mProgress.setMessage("Please wait while we fetch your location");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        if (mLocation != null) {

            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();

        } else {

            mUserRef.child("latlng").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    latitude = (double) dataSnapshot.child("latitude").getValue();
                    longitude = (double) dataSnapshot.child("longitude").getValue();

                    mUserLocation = new LatLng(latitude, longitude);
                    mMap.clear();
                    mProgress.dismiss();
                    mUser = mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 15));

                    addOtherUser();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        mSwitch.setChecked(false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void addOtherUser() {

        mRootRef.child("Users").child(mOtherUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mOtherUserName = dataSnapshot.child("name").getValue().toString();

                if (dataSnapshot.hasChild("latlng")) {

                    double other_latitude = (double) dataSnapshot.child("latlng").child("latitude").getValue();
                    double other_longitude = (double) dataSnapshot.child("latlng").child("longitude").getValue();

                    mOtherUserLocation = new LatLng(other_latitude, other_longitude);

                    if (mOther != null) {

                        mOther.remove();

                    }
                    mOther = mMap.addMarker(new MarkerOptions().position(mOtherUserLocation).snippet("set as destination").title(mOtherUserName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void addUserLocation(LatLng location) {

        Map locationMap = new HashMap();
        locationMap.put("Users/" + mCurrentUser.getUid() + "/latlng", location);

        mRootRef.updateChildren(locationMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Toast.makeText(MapActivity.this, "Error in putting user location to database", Toast.LENGTH_LONG).show();

                }
            }
        });

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

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (mUser != null) {

                    mUser.remove();

                }
                mUser = mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));

                // add user location to database
                addUserLocation(mUserLocation);

                if (firstTime) {

                    mProgress.dismiss();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 17));
                    firstTime = false;

                }

                addOtherUser();

                makeRoute(mUserLocation, mDestination);

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

        int permission_all = 1;
        int check_permission;
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);

        } else {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(MapActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, permissions, permission_all);

                } else {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {

                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        if (mUser != null) {

                            mUser.remove();

                        }
                        mUser = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));

                        // add user location to database
                        addUserLocation(userLocation);

                    }

                }

            }

        }

        /*
        add the other user location here
         */

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                mDestination = marker.getPosition();
                makeRoute(mUserLocation, mDestination);

            }
        });

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUserLocation != null) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 15));

                }

            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {

                data = downloadUrl(url[0]);

            } catch (Exception e) {

                Log.d("Background task", e.toString());

            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            MapActivity.ParserTask parserTask = new MapActivity.ParserTask();
            parserTask.execute(result);

        }
    }

    private void makeRoute(LatLng origin, LatLng dest) {

        if (origin == null && dest == null) {

            Toast.makeText(MapActivity.this, "user and destination location needed", Toast.LENGTH_LONG).show();

        } else if (origin == null) {

            Toast.makeText(MapActivity.this, "user location not found", Toast.LENGTH_LONG).show();

        } else if (dest == null) {

            Toast.makeText(MapActivity.this, "please select a marker as destination", Toast.LENGTH_LONG).show();

        } else {

            if (mUser != null) {

                mUser.remove();

            }
            mUser = mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));

            //Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            MapActivity.DownloadTask downloadTask = new MapActivity.DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

            mSwitch.setVisibility(View.VISIBLE);
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        Intent arIntent = new Intent(MapActivity.this, ARActivity.class);
                        arIntent.putExtra("destination", mDestination);
//                        arIntent.putExtra("routing_points", routePoints.toString());
                        startActivity(arIntent);

                        Toast.makeText(MapActivity.this, "Opening AR Activity", Toast.LENGTH_LONG).show();
                        mSwitch.setChecked(false);

                    } else {

                        Toast.makeText(MapActivity.this, "Switch off", Toast.LENGTH_LONG).show();

                    }
                }
            });

        }

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {


                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            MapFragment.routePoints = result;

            Log.d("ROUTEPOINTS", MapFragment.routePoints.toString());

            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            if (mLine != null) {

                mLine.remove();

            }

            if (lineOptions!=null) {
                // Drawing polyline in the Google Map for the i-th route
                mLine = mMap.addPolyline(lineOptions);
            }
        }
    }
}