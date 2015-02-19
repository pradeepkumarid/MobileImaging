package com.mobile.imaging.myfloorplan;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Random;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private static String TAG = "GP";

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    float[] mGeomagnetic;
    float[] mGravity;
    float azimuth; // yaw
    float pitch;
    float roll;

    TextView magDataText0;
    TextView magDataText1;
    TextView magDataText2;

    private GLSurfaceView mGLView;

    private float[][] axes = new float[][]{
            {45,180,45},
            {135,45,180},
            {135,45,90},
            {135,45,270},
            {135,45,0},
            {45,0,45}
    };
    public boolean isThreadRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate MainActivity");
        if (!checkCameraHardware(this))
            Log.e(TAG, "No Camera Support!!!!");



        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (magnetometer == null){
            Log.e(TAG, "No Magnetometer Support!!!!");
        }


    }

    public void initViews()
    {

        mGLView = new MyGLSurfaceView(this);
        initCamera();

        FrameLayout openGLFrame = new FrameLayout(this);
        openGLFrame.setBackgroundColor(Color.TRANSPARENT);
        openGLFrame.addView(mGLView);

        setContentView(R.layout.activity_main);

        FrameLayout cameraLayout = (FrameLayout)findViewById(R.id.camera_Layout);
        cameraLayout.addView(mPreview);
        addContentView(openGLFrame, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        mGLView.setZOrderMediaOverlay(true);

        //For displaying all textviews
        LinearLayout miscLayout = new LinearLayout(this);
        magDataText0 = new TextView(this);
        magDataText1 = new TextView(this);
        magDataText2 = new TextView(this);
        magDataText0.setTextColor(Color.RED);
        magDataText1.setTextColor(Color.GREEN);
        magDataText2.setTextColor(Color.BLUE);

        miscLayout.addView(magDataText0);
        miscLayout.addView(magDataText1);
        miscLayout.addView(magDataText2);
        addContentView(miscLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));



    }
    public void initCamera()
    {
        // Create an instance of Camera
        Log.d(TAG,"initCamera MainActivity");
        if(mCamera==null) {
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera);
            mCamera.setDisplayOrientation(90);
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Camera.Size mSize;
            for (Camera.Size size : sizes) {
                Log.i(TAG, "Available resolution: " + size.width + " " + size.height);
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point sizeDisplay = new Point();
            display.getSize(sizeDisplay);

            int screenwidth = sizeDisplay.x;
            int screenheight = sizeDisplay.y;
            Log.d(TAG, "Screen size : width " + screenwidth + " height = " + screenheight);

            android.hardware.Camera.Parameters parameters = mCamera.getParameters();
            android.hardware.Camera.Size size = parameters.getPictureSize();
            int height = size.height;
            int width = size.width;

            Log.e(TAG, "Height = " + height + " WEIGHT = " + width);

            parameters.setPictureSize(screenheight, screenwidth);
            mCamera.setParameters(parameters);
        }
        else
            Log.e(TAG,"mCamera is already initialized");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_render) {

            setWallCoords();
            //show a loading sign untill Wall Cords are set. Then proceed to FloorPlanActivity

            Log.d(TAG,"menu item action_render clicked");
            Intent intent = new Intent(this, FloorPlanActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(TAG, "Exception in getCameraInstance "+e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }





    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume MainActivity");
        initViews();

        // Create our Preview view and set it as the content of our activity.
        mGLView.onResume();


        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);


    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause MainActivity");
        releaseCamera();
        mGLView.onPause();
    }

    private void releaseCamera() {
        Log.d(TAG,"releaseCamera MainActivity");
        if (mCamera != null) {
            Log.d(TAG,"mCamera is released");
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        if(mPreview!=null)
        {
            mPreview = null;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration myConfig) {
        super.onConfigurationChanged(myConfig);
        int orient = getResources().getConfiguration().orientation;
        switch(orient) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.d(TAG,"ORIENTATION_LANDSCAPE");
                // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Log.d(TAG,"ORIENTATION_LANDSCAPE");
                break;
            default:
                Log.d(TAG,"SCREEN_ORIENTATION_UNSPECIFIED");
                //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0]; // orientation contains: azimut, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
            }
        }
        redrawData();
     //   mCustomDrawableView.invalidate();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void redrawData()
    {
        magDataText0.setText("A:"+azimuth);
        magDataText1.setText("P:"+pitch);
        magDataText2.setText("R:"+roll);
    }

    public void setWallCoords()
    {
        float[][] tempCords = { {0.7071f, 0.7071f} ,
                                { -0.7071f,0.7071f},
                                {-0.7071f,-0.7071f},
                                {0.7071f,-0.7071f }
                    };   //Get this from ray tracing algo implementation

        int noOfWalls = 4;
        float zHeight = 0.25f;

        FloorCoordsGlobal.numOfWalls = noOfWalls;

        //Conversion of corner points to wall coordinates.

        for(int i=0;i<noOfWalls;i++)
        {
            int n = i+1;
            if(n==noOfWalls)
                n=0; // To make n as 0 when i is last point

            FloorCoordsGlobal.wallCoords[i][0] = tempCords[i][0];   // 'i' th Wall's 1st x cord
            FloorCoordsGlobal.wallCoords[i][1] = tempCords[i][1];   // 'i' th Wall's 1st y cord
            FloorCoordsGlobal.wallCoords[i][2] = -zHeight;

            FloorCoordsGlobal.wallCoords[i][3] = tempCords[n][0];   // 'i' th Wall's 2nd x cord
            FloorCoordsGlobal.wallCoords[i][4] = tempCords[n][1];   // 'i' th Wall's 2nd y cord
            FloorCoordsGlobal.wallCoords[i][5] = -zHeight;

            FloorCoordsGlobal.wallCoords[i][6] = tempCords[n][0];   // 'i' th Wall's 3rd x cord
            FloorCoordsGlobal.wallCoords[i][7] = tempCords[n][1];   // 'i' th Wall's 3rd y cord
            FloorCoordsGlobal.wallCoords[i][8] = zHeight;

            FloorCoordsGlobal.wallCoords[i][9] = tempCords[i][0];   // 'i' th Wall's 3rd x cord
            FloorCoordsGlobal.wallCoords[i][10] = tempCords[i][1];   // 'i' th Wall's 3rd y cord
            FloorCoordsGlobal.wallCoords[i][11] = zHeight;

        }




    }


}
