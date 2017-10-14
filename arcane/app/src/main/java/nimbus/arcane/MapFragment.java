package nimbus.arcane;


import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.ParallelExecutorCompat;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static View mMainView;

    public static List<List<HashMap<String, String>>> routePoints = null;
    private ArrayList<String> availableGroupIDList=new ArrayList<String>();

    private GoogleMap mMap;
    private Boolean firstTime = true;

    private static LocationManager locationManager;
    private static LocationListener locationListener;

    private GPSTracker gpsTracker;
    private Location mLocation;
    private Marker mUser;
    private Polyline mLine;

    private LatLng mUserLocation;
    private LatLng mDestination;

    private double latitude, longitude;

    private Switch mSwitch;
    private ImageButton mMyLocation;

    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;

    private HashMap<String, String> mFriendsLocation = new HashMap<String, String>();
    private HashMap mMarkerMap = new HashMap();

    private ProgressDialog mProgress;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mMainView != null) {

            ViewGroup parent = (ViewGroup) mMainView.getParent();
            if (parent != null) {

                parent.removeView(mMainView);

            }
        }

        try {

            // Inflate the layout for this fragment
            mMainView = inflater.inflate(R.layout.fragment_map, container, false);

        } catch (InflateException e) {

            // Map is already there, return main view as it is

        }

        mSwitch = (Switch) mMainView.findViewById(R.id.map_ar);
        mSwitch.setVisibility(View.INVISIBLE);

        mMyLocation = (ImageButton) mMainView.findViewById(R.id.map_location);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = mRootRef.child("Users").child(mCurrentUser.getUid());

        mProgress = new ProgressDialog(getContext());

        return mMainView;
    }

    public void getGroups(){
        mUserRef.child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                   for(DataSnapshot postSnapShot:dataSnapshot.getChildren()) {
                       //Groups group=postSnapShot.getValue(Groups.class);
                       availableGroupIDList.add(postSnapShot.getKey());
                       // adapter.notifyDataSetChanged();
                       Log.d("TESTGROUP",postSnapShot.getKey());
                   }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gpsTracker = new GPSTracker(getContext());
        // checking whether gps is enabled or not
        gpsTracker.checkGPS();

        mLocation = gpsTracker.getLocation();

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

                    addMarker();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        mSwitch.setChecked(false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void addMarker() {

        mRootRef.child("Groups").child("-Kw2VGxV01m_oJqGa9qs").child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> members = dataSnapshot.getChildren();
                for (DataSnapshot member : members) {

                    String user_id = member.getKey();
                    if (!user_id.equals(mCurrentUser.getUid())) {

                        String user_name = member.child("name").getValue().toString();
                        if (member.hasChild("latlng")) {

                            double user_latitude = (double) member.child("latlng").child("latitude").getValue();
                            double user_longitude = (double) member.child("latlng").child("longitude").getValue();
                            LatLng user_location = new LatLng(user_latitude, user_longitude);

                            Marker marker = mMap.addMarker(new MarkerOptions().position(user_location).snippet("set as destination").title(user_name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                            verifyMarker(marker, user_name);

                            mMarkerMap.put(user_name, marker);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void verifyMarker(Marker marker, String user_name) {

        Marker tMarker = (Marker) mMarkerMap.get(user_name);
        if (tMarker != null) {

            marker.remove();
            mMarkerMap.remove(user_name);

        }

    }

    public void addUserLocation(LatLng location) {

        Map locationMap = new HashMap();
        locationMap.put("Users/" + mCurrentUser.getUid() + "/latlng", location);
        locationMap.put("Groups/" + "-Kw2VGxV01m_oJqGa9qs" + "/Members/" + mCurrentUser.getUid() + "/latlng", location);

        mRootRef.updateChildren(locationMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError != null) {

                    Toast.makeText(getContext(), "Error in putting user location to database", Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {

                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                        mMap.clear();
                        if (mUser != null) {

                            mUser.remove();

                        }
                        mUser = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                        // add user location to database
                        addUserLocation(userLocation);

                    }

//                    addMarker();

                }
            }
        }
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

        getGroups();

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                mUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.clear();
                if (mUser != null) {

                    mUser.remove();

                }
                mUser = mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));

                // add user location to database
                addUserLocation(mUserLocation);

                if (firstTime) {

                    mProgress.dismiss();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 15));
                    firstTime = false;

                }

                addMarker();

                makeRoute(mUserLocation, mDestination);

                Log.d("GROUPS",availableGroupIDList.toString());

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

                if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(), permissions, permission_all);

                } else {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {

                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                        mMap.clear();
                        if (mUser != null) {

                            mUser.remove();

                        }
                        mUser = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                        // add user location to database
                        addUserLocation(userLocation);

                    }

//                    addMarker();

                }

            }

        }

        addMarker();

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                mDestination = marker.getPosition();
//                Toast.makeText(getContext(), mDestination.toString(), Toast.LENGTH_LONG).show();
                makeRoute(mUserLocation, mDestination);

            }
        });

        mMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUserLocation != null) {

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 15));

                }

            }
        });
    }

    public static int hasPermissions(Context context, String... permissions) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && context != null && permissions != null) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);

            return 0;

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                    return 1;

                }

            }

        }

        return 0;
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

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private void makeRoute(LatLng origin, LatLng dest) {

        if (origin == null && dest == null) {

            Toast.makeText(getContext(), "user and destination location needed", Toast.LENGTH_LONG).show();

        } else if (origin == null) {

            Toast.makeText(getContext(), "user location not found", Toast.LENGTH_LONG).show();

        } else if (dest == null) {

            Toast.makeText(getContext(), "please select a marker as destination", Toast.LENGTH_LONG).show();

        } else {

//            mMap.clear();
            if (mUser != null) {

                mUser.remove();

            }
            mUser = mMap.addMarker(new MarkerOptions().position(mUserLocation).title("You are here"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mUserLocation, 15));
//            addMarker();

            //Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);
//            Toast.makeText(getContext(), url, Toast.LENGTH_SHORT).show();

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);

            mSwitch.setVisibility(View.VISIBLE);
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        Intent arIntent = new Intent(getContext(), ARActivity.class);
                        arIntent.putExtra("destination", mDestination);
//                        arIntent.putExtra("routing_points", routePoints.toString());
                        startActivity(arIntent);

                        Toast.makeText(getContext(), "Opening AR Activity", Toast.LENGTH_LONG).show();
                        mSwitch.setChecked(false);

                    } else {

                        Toast.makeText(getContext(), "Switch off", Toast.LENGTH_LONG).show();

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
