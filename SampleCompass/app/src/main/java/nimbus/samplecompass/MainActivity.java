package nimbus.samplecompass;

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
import android.graphics.Matrix;
import android.widget.TextView;
import java.lang.*;

import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imageView; //Above Compass
    private ImageView imageView2; //Below Compass
    private ImageView imageView3; //Middle Compass

    private float[] mGravity = new float[3]; //Accelerometer Array
    private float[] mGeomagnetic = new float[3]; //Magnetometer Array

    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    private SensorManager msensorManager;

    //Angle between position to destination
    float angle;
    //Lat,Lang of my position (CS Lygon)
    private float myLongitude = 144.9691f;
    private float myLatitude = -37.7931f;
    //Lat,Lang of my destination (Melbourne Central)
    private float objLongitude = 144.9467f;
    private float objLatitude = -37.7992f;

    //Function to calculate angle from my position to destination
    private double angleFromCoordinate(double lat1, double long1, double lat2, double long2) {
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set the Arrow image
        imageView = (ImageView) findViewById(R.id.compass); //Top Arrow
        imageView2 = (ImageView) findViewById(R.id.compass2); //Bottom Arrow
        imageView3 = (ImageView) findViewById(R.id.compass3); //Middle Arrow

        //Determine the angle of the arrow
        angle = (float) angleFromCoordinate(myLatitude, myLongitude, objLatitude, objLongitude);

        //Rotate the arrow based on the angle above
        imageView.setRotation(angle);

        msensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        msensorManager.registerListener(this,msensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_GAME);
        msensorManager.registerListener(this,msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        msensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;

        synchronized (this){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1];
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2];
            }

            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0];
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1];
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2];
            }

            float R[] = new float[9];
            float T[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R,T,mGravity,mGeomagnetic);

            if(success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                Animation anim = new RotateAnimation(-currentAzimuth,-azimuth,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                currentAzimuth = azimuth;

                float dAngle = angle-(360-azimuth);
                if (dAngle < 0) { //If left rotation
                    dAngle = 360-Math.abs(dAngle);
                }

                anim.setDuration(500);
                anim.setFillAfter(true);

                imageView2.startAnimation(anim); //Your Phone orientation based on the NSEW
                imageView3.setRotation(dAngle); //Where you should turn your phone

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
    }

}
