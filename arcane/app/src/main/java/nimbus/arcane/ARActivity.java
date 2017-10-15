package nimbus.arcane;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
//import android.graphics.Matrix;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.lang.*;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Text;

/**
 * Created by ntdat on 1/13/17.
 * Github : https://github.com/dat-ng/ar-location-based-android
 */
/**
 * Last Edited by Arnold Angelo on 10/15/17
 */

//The Activity that runs the AR feature on our App
public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    final static String TAG = "ARActivity";

    private SurfaceView surfaceView; //Surface View for the AR View
    private FrameLayout cameraContainerLayout; //The Frame Layout for Camera View

    private Camera camera; //Camera to access camera view and it's features
    private ARView arOverlayView; //Class for drawing the AR object
    private CameraPreview arCamera; //Class that determine the Camera Preview(Size,Scale,etc) of the AR view

    private TextView tvCurrentLocation; //Text containing current location
    private TextView pointsLeft; //Text containing how many checkpoints left
    private ImageView compass; //Compass image showing user direction towards north,south,east,west
    private ImageView objectDir; //Compass image showing user direction towards target object

    private float azimuth = 0f;
    private float currentAzimuth = 0f;
    private float angle; //Angle between position to destination

    private float[] mGravity = new float[3]; //Accelerometer Array
    private float[] mGeomagnetic = new float[3]; //Magnetometer Array

    private SensorManager sensorManager; //Sensor manager to access the device's sensor
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;
    private float DISTANCE_THRESHOLD = 15.0f; /*Minimum distance needs to be reached between the user and target to show that the user have reach
                                                the target*/

    private LocationManager locationManager; //Location manager to get location service and current location
    public Location location; //Location of the user now

    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    private boolean isRouting = true; //Boolean that shows if the AR mode is in Routing mode or not
    private boolean destinationReached = false;

    public static LatLng destinationPoint=null;
    public static ARPoint destinationARPoint;

    private Switch mSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        destinationPoint = (LatLng)getIntent().getExtras().get("destination");
        destinationARPoint = new ARPoint(destinationPoint.latitude,destinationPoint.longitude);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_preview);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.current_location);

        compass = (ImageView) findViewById(R.id.arrow);
        objectDir = (ImageView) findViewById(R.id.arrow2);

        pointsLeft = (TextView) findViewById(R.id.points_left);
        arOverlayView = new ARView(this);

        mSwitch = (Switch) findViewById(R.id.switch_ar);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    isRouting = false;
                    arOverlayView.setType(isRouting);
                    mSwitch.setChecked(true);

                } else {
                    isRouting = true;
                    arOverlayView.setType(isRouting);
                    mSwitch.setChecked(false);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();

    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    //Calculate angle between to points by their latitudes and longitudes
    private double angleFromCoordinate(double lat1, double long1, double lat2, double long2) {
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    //Request to use Camera in the device
    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    //Request to access Location/GPS in the device
    public void requestLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(permissions, REQUEST_LOCATION_PERMISSIONS_CODE);
            } else {
                initLocationService();
            }
        }
    }

    //Updating the AR View for each resume
    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    //Creating a new CameraPreview Object and keep updating the Camera Preview for each resume
    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new CameraPreview(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    //Opening Camera and Attaching the camera object to CameraPreview object to be used
    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Updating the Surfaceview
    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            //Change rotation vector to rotation matrix
            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }

        /* Line 296 - 332
          Code Explanation : Getting data from the device's sensors and calculate angle for the compass
        * Code Cited from : https://www.youtube.com/watch?v=RcqXFxqIAW4 */
        final float alpha = 0.97f;

        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
            }

            float R[] = new float[9];
            float T[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, T, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                Animation anim = new RotateAnimation(currentAzimuth, azimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                currentAzimuth = azimuth;

                float dAngle = angle - azimuth; //Delta angle between the Device Angle in NSWE(Nort,South,West,East) with the Angle between User and Destination in NSWE
                if (dAngle < 0) {
                    dAngle = 360 - Math.abs(dAngle);
                }

                anim.setDuration(500);
                anim.setFillAfter(true);

                compass.startAnimation(anim); //Your Phone orientation based on the NSEW
                objectDir.setRotation(dAngle); //Where you should turn your phone

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }

    //Initiate the location service and get our position
    private void initLocationService() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String permission : permissions) {
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

        }

        try   {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            //Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled)    {
                //Cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;


            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null)   {
                    //Get latest location
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    //Updates the TextView with latest location
                    updateLatestLocation(location);
                }
            }

            if (isGPSEnabled)  {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null)  {
                    //Get latest location
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //Updates the TextView with latest location
                    updateLatestLocation(location);
                }
            }
        } catch (Exception ex)  {
            Log.e(TAG, ex.getMessage());

        }
    }

    //
    private void updateLatestLocation(Location location) {
        if (arOverlayView !=null) {

            this.location = location;
            ARPoint nextpoint;
            float[] curLoc;
            float[] targetLoc;
            float distance;

            //When in Routing Mode
            if (isRouting==true) {

                if (arOverlayView.getIndex() < arOverlayView.getARPointsSize()) {
                    nextpoint = arOverlayView.getARPoint();

                    curLoc = LocationHelper.WSG84toECEF(location);
                    targetLoc = LocationHelper.WSG84toECEF(nextpoint.getLocation());
                    distance = LocationHelper.distanceFromECEF(curLoc, targetLoc);

                    if (distance < DISTANCE_THRESHOLD) {
                        arOverlayView.incrementIndex();
                    }

                    if (arOverlayView.getIndex() < arOverlayView.getARPointsSize()) { //When haven't reached all checkpoints after increment
                        angle = (float) angleFromCoordinate(location.getLatitude(), location.getLongitude(), arOverlayView.getARPoint().getLocation().getLatitude(), arOverlayView.getARPoint().getLocation().getLongitude());

                    } else { //When have reached all checkpoints
                        angle = currentAzimuth;

                    }

                } else {
                    angle =currentAzimuth;
                }

                //Send the current location to AROverlayView Class, which will render the target
                arOverlayView.updateCurrentLocation(location);
                //Update the current location to TextView
                tvCurrentLocation.setText(String.format("My Position \nLatitude: %.10s \nLongitude: %.10s \n",
                        location.getLatitude(), location.getLongitude()));
                pointsLeft.setText(String.format("Checkpoint(s) Left : %d",(arOverlayView.getARPointsSize()-arOverlayView.getIndex())));

            }
            else { //in Destination Target Mode
                nextpoint = destinationARPoint;

                curLoc = LocationHelper.WSG84toECEF(location);
                targetLoc = LocationHelper.WSG84toECEF(nextpoint.getLocation());

                distance = LocationHelper.distanceFromECEF(curLoc,targetLoc);

                if (distance < 5.0f) {
                    destinationReached = true;
                    arOverlayView.setIndex(arOverlayView.getARPointsSize());
                }

                angle = (float) angleFromCoordinate(location.getLatitude(),location.getLongitude(),nextpoint.getLocation().getLatitude(),nextpoint.getLocation().getLongitude());

                //Send the current location to AROverlayView Class, which will render the target
                arOverlayView.updateCurrentLocation(location);
                //Update the current location to TextView
                tvCurrentLocation.setText(String.format("My Position \nLatitude: %.10s \nLongitude: %.10s \n",
                        location.getLatitude(), location.getLongitude()));
                pointsLeft.setText(String.format("DestinationTarget Mode"));

            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLatestLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}