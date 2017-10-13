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
import android.widget.FrameLayout;
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
import java.lang.*;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Text;

/**
 * Created by ntdat on 1/13/17.
 */
/**
 * Last Edited by Arnold on 10/12/17
 */


public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    final static String TAG = "ARActivity";
    private SurfaceView surfaceView; //Surface View for the AR View
    private FrameLayout cameraContainerLayout; //The Frame Layout for Camera View
    private ARView arOverlayView; //Class for drawing the AR object
    private Camera camera; //Camera to access camera view and it's features
    private CameraPreview arCamera; //???
    private TextView tvCurrentLocation; //Text containing current location
    private TextView pointsLeft;

    private ImageView compass;
    private ImageView objectDir;
    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    private float[] mGravity = new float[3]; //Accelerometer Array
    private float[] mGeomagnetic = new float[3]; //Magnetometer Array
    //Angle between position to destination
    float angle;

    private SensorManager sensorManager; //Sensor manager to access the device's sensor
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; //???
    private static final long MIN_TIME_BW_UPDATES = 0;//???

    private LocationManager locationManager; //Location manager to get location service and current location
    public Location location; //Location of the user now
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_preview);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.current_location);

        compass = (ImageView) findViewById(R.id.arrow);
        objectDir = (ImageView) findViewById(R.id.arrow2);

        pointsLeft = (TextView) findViewById(R.id.points_left);
        arOverlayView = new ARView(this);
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

    private double angleFromCoordinate(double lat1, double long1, double lat2, double long2) {
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    //Request to use Camera when Activity start
    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    //Request to access Location/GPS when Activity start -- Might not be necessary since Map already get locationar
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

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

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

            //Multiply the Projection Matrix of the AR Camera and the
            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }

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

                float dAngle = angle - azimuth;
                if (dAngle < 0) { //If left rotation
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

    //Initiate the location service and get our position (latitude, longitude) and pass it to variable "location"
    private void initLocationService() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String permission : permissions) {
            //Only works for SDK version below 23 --See Android Studio Documentation for more info--
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
                //??? Anyone know what this do ???
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
                //??? Anyone know what this do ???
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

    private void updateLatestLocation(Location location) {
        if (arOverlayView !=null) {

            this.location = location;

            if(arOverlayView.getIndex()<arOverlayView.getARPointsSize()) {
                ARPoint nextpoint = arOverlayView.getARPoint();
                float[] curLoc = LocationHelper.WSG84toECEF(location);
                float[] targetLoc = LocationHelper.WSG84toECEF(nextpoint.getLocation());
                float distance = LocationHelper.distanceFromECEF(curLoc,targetLoc);

                if (distance<20.0f) {
                    arOverlayView.incrementIndex();
                }

                //Log.d("INCINDEX","INDEX = "+arOverlayView.getIndex());
                angle = (float) angleFromCoordinate(location.getLatitude(),location.getLongitude(),arOverlayView.getARPoint().getLocation().getLatitude(),arOverlayView.getARPoint().getLocation().getLongitude());
            }


            //Send the current location to AROverlayView Class, which will render the target
            arOverlayView.updateCurrentLocation(location);
            //Update the current location to TextView
            tvCurrentLocation.setText(String.format("My Position \nLatitude: %.10s \nLongitude: %.10s \n",
                    location.getLatitude(), location.getLongitude()));
            pointsLeft.setText(String.format("Checkpoint(s) Left : %d",(arOverlayView.getARPointsSize()-arOverlayView.getIndex())));
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