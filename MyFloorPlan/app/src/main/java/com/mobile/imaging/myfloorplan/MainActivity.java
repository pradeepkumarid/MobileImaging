package com.mobile.imaging.myfloorplan;

import android.content.Context;
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



    private SeekBar XSeek;
    private SeekBar YSeek;
    private SeekBar ZSeek;
    public int freqSeekValue;
    public int alphaValue;
    public boolean isThreadRunning = false;

    private Button rollingButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        if (!checkCameraHardware(this))
            Log.e(TAG, "No Camera Support!!!!");

        mGLView = new MyGLSurfaceView(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (magnetometer == null){
            Log.e(TAG, "No Magnetometer Support!!!!");
        }


        // Create an instance of Camera
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        mCamera.setDisplayOrientation(90);

        //mGLView.setBackgroundColor(Color.TRANSPARENT);
        // preview.setBackgroundColor(Color.TRANSPARENT);
        // preview.addView(mPreview);

        FrameLayout openGLFrame = new FrameLayout(this);
        openGLFrame.setBackgroundColor(Color.TRANSPARENT);
        openGLFrame.addView(mGLView);

        /*
        FrameLayout miscLayout = new FrameLayout(this);
        miscLayout.setBackgroundColor(Color.TRANSPARENT);
        rollingButton = new Button(this);
        magDataText0 = new TextView(this);
        magDataText1 = new TextView(this);
        magDataText2 = new TextView(this);
        miscLayout.addView(rollingButton);
        miscLayout.addView(magDataText0);
        miscLayout.addView(magDataText1);
        miscLayout.addView(magDataText2);
        */

        setContentView(R.layout.activity_main);

        FrameLayout cameraLayout = (FrameLayout)findViewById(R.id.camera_Layout);
        FrameLayout miscFrameLayout = (FrameLayout)findViewById(R.id.misc_frame);

        cameraLayout.addView(mPreview);
        addContentView(openGLFrame, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        magDataText0 = (TextView)findViewById(R.id.magData);
        magDataText1 = (TextView)findViewById(R.id.textView);
        magDataText2 = (TextView)findViewById(R.id.textView2);


        rollingButton = (Button)findViewById(R.id.button);
        rollingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("GP","Rolling pressed");
                startRolling();

            }
        });


        mGLView.setZOrderMediaOverlay(true);
        miscFrameLayout.bringToFront();
        //openGLFrame.invalidate();
        rollingButton.bringToFront();
        magDataText0.bringToFront();
        magDataText1.bringToFront();
        magDataText2.bringToFront();

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
        Log.d(TAG, "Screen size : width "+screenwidth+" height = "+screenheight);

        android.hardware.Camera.Parameters parameters = mCamera.getParameters();
        android.hardware.Camera.Size size = parameters.getPictureSize();
        int height = size.height;
        int width = size.width;

        Log.e(TAG,"Height = "+height+" WEIGHT = "+width);

        parameters.setPictureSize(screenheight,screenwidth);
        mCamera.setParameters(parameters);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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

        // Create our Preview view and set it as the content of our activity.
        mGLView.onResume();


        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);


    }


    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mGLView.onPause();
    }

    private void releaseCamera() {
        if (mCamera != null) {
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


    public void startRolling()
    {
        Thread thread = new Thread() {
            @Override
            public void run() {
                isThreadRunning = true;
                try {
                    Random r = new Random();
                    int i1 = r.nextInt(6);
                    int count = 0;
                    Log.e("GP", "Random Number generated is" + i1);
                    while(count<11) {
                        MyGLRenderer.xAngle = (axes[i1][0] + 720*count/10)/3.6f;
                        MyGLRenderer.yAngle = (axes[i1][1] + 720*count/10)/3.6f;
                        MyGLRenderer.zAngle = (axes[i1][2] + 720*count/10)/3.6f;
                        mGLView.requestRender();
                        sleep(100);
                        count++;

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    isThreadRunning = false;
                }
            }
        };
        thread.start();
    }


}
