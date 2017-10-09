package nimbus.arcane;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ntdat on 1/13/17.
 */

/**
<<<<<<< HEAD
 * Last Edited by Arnold on 10/7/17.
=======
 * Edited by Arnold on 9/26/2017.
>>>>>>> parent of 75cb8ed... Connect AR with Map Fragment Data
 */

public class ARView extends View{

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;
    private int i = 0;

    //Initializing the AR Points
    public ARView(Context context) {
        super(context);
        this.context = context;

<<<<<<< HEAD
        routePoints = MapFragment.routePoints;
        final List<HashMap<String,String>> pointList = routePoints.get(0);
        final int pointLength = pointList.size();

        //Log.d("Test","Route = "+pointList);

=======
>>>>>>> parent of 75cb8ed... Connect AR with Map Fragment Data
        //Pass the Array of Locations into here to be rendered later
        arPoints = new ArrayList<ARPoint>() {{
            add(new ARPoint("First Point", -37.8, 144.959, 0));
        }};
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    //Increment Index used for showing AR Points
    public void incrementIndex() {
        this.i = this.i + 1;
    }

    //Get the current Index
    public int getIndex() {
        return this.i;
    }

    public int getSize() {
        return arPoints.size();
    }

    public ARPoint getARPoint() {
        return arPoints.get(i);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        if (i < arPoints.size()) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                canvas.drawCircle(x,y,radius+20,paint);
                paint.setColor(Color.RED);
                canvas.drawCircle(x, y, radius, paint);
                canvas.drawText("Checkpoint "+(i+1), x - (30*6), y-160, paint);
                canvas.drawText("Distance : " + LocationHelper.distanceFromECEF(currentLocationInECEF,pointInECEF) + " m", x - (30 * 11), y - 80, paint);
            }
        }
        else {
            //Generate Text when all points have been reached
            canvas.drawText("Arrived at Destination", canvas.getWidth()/4, canvas.getHeight()/2, paint);
        }
    }
}
