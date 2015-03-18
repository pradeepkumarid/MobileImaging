package com.mobile.imaging.myfloorplan;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;

import Jama.Matrix;

import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.cos;
import static java.util.Arrays.sort;


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
    float[][] room1_cord=new float[10][10];
    float[][] room2_cord=new float[10][10];
    float[][] room3_cord=new float[10][10];
    float[][] room4_cord=new float[10][10];
    float[][] door1_cord=new float[10][10];
    float[][] door2_cord=new float[10][10];



    TextView magDataText0;
    TextView magDataText1;
    TextView magDataText2;
    int counter=1;
    private ArrayList<Float> azimuthAngles;
    private int anglesCount;

    float[][] doors=new float[10][10];
    float[] temp_door_angle=new float[10];
    private TableLayout tableLayout;

    Button changeroom;
    Button door;
    Button corner;
    private float[] angles=new float[5];
    int doorcount=0;
    int[] temp=new  int[10];
    int prevWallCount = 0;


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

        FloorCoordsGlobal.numOfWalls = 0;

        azimuthAngles = new ArrayList<Float>();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (magnetometer == null){
            Log.e(TAG, "No Magnetometer Support!!!!");
        }

        anglesCount = 0;


    }

    private OnClickListener corn_list=new OnClickListener() {

        @Override
        public void onClick(View v) {

            float angle = ((azimuth + 3.0f)*360.0f/6.0f);

            Log.e(TAG, "Touch detected : captured Azimuth angle = " + angle);
            azimuthAngles.add( angle ); //Use these angles for ray tracing and finding floor coordinates
            //Toast.makeText(getApplicationContext(),"Added No."+anglesCount+" angle= "+angle,Toast.LENGTH_SHORT);//toast didnt work
            anglesCount++;
            addTextContentOnScreen("Corner  No."+anglesCount+" angle= "+angle);
            //coordinates();
            //counter=counter+1;
         //   i=i+1;

        }
    };

    private OnClickListener door_list=new OnClickListener() {

        @Override
        public void onClick(View v) {

            float angle = ((azimuth + 3.0f) * 360.0f / 6.0f);

            Log.e(TAG, "Touch detected : captured Azimuth angle = " + angle);
            temp_door_angle[doorcount] = angle;
            //azimuthAngles.add( angle ); //Use these angles for ray tracing and finding floor coordinates
            //Toast.makeText(getApplicationContext(), "Added No." + doorcount + " angle= " + angle, Toast.LENGTH_SHORT);//toast didnt work


            temp[doorcount] = anglesCount;
            doorcount++;
            Log.e("GP","doorcount"+doorcount);
            Log.e("GP","anglecount"+anglesCount);

            addTextContentOnScreen("Door No."+doorcount+" angle= "+angle);
        }
        };

        private OnClickListener room_list=new OnClickListener() {
            @Override
            public void onClick(View v) {
                //  counter=counter+1;
                //    azimuthAngles.clear();
                //    doorcount=1;
                float[][]temp12; //=new float[10][10];
                temp12=coordinates();
                getdoorcoord(temp12);
                if(counter>1){
                    update();
                    //call the jama function
                }

                for(int i=0;i<anglesCount;i++){
                    switch (counter) {
                        case 1:
                            Log.e("tag", "room\t" + counter + " coords" + "x " + room1_cord[i][0] + "\ty " + room1_cord[i][1]);
                            break;
                        case 2:
                            Log.e("tag", "room\t" + counter + " coords" + "x " + room2_cord[i][0] + "\ty " + room2_cord[i][1]);
                            break;

                    }
                }
                for (int i=0;i<doorcount;i++){
                    switch(counter){
                        case 1:
                            Log.e("tag","door coords"+counter+"\tx "+door1_cord[i][0]+"\ty "+door1_cord[i][1]);
                            break;
                        case 2:
                            Log.e("tag","door coords"+counter+"\tx "+door2_cord[i][0]+"\ty "+door2_cord[i][1]);
                            break;
                    }
                }
                switch (counter){
                    case 1:
                        setWallCoords(room1_cord);
                        break;
                    case 2:
                        setWallCoords(room2_cord);
                        break;
                    /*case 3:
                        setWallCoords(room3_cord);
                        break;
                    case 4:
                        setWallCoords(room4_cord);
                        break;*/


                }
                addTextContentOnScreen("Room\t"+counter+"\t completed");
                //Log.e("GP","Room\t"+counter+"\t completed");
                counter++;
                //reinistilize
                doorcount=0;
                anglesCount=0;
                azimuthAngles.clear();

            }
        };


    private void update(){
        /*float[][]temp_room1;
        float[][] temp_room2;
        switch (counter){
            case 2:
                temp_room1=room1_cord;
                temp_room2=room2_cord;
                break;

            case 3:
                temp_room1=room2_cord;
                temp_room2=room3_cord;
                break;

            case 4:
                temp_room1=room3_cord;
                temp_room2=room4_cord;
                break;

        }*/
        double[] y={room1_cord[0][0],
                room1_cord[0][1],door1_cord[0][0],door1_cord[0][1],door1_cord[1][0],door1_cord[1][1],door1_cord[2][0],door1_cord[2][1],
                door1_cord[3][0],door1_cord[3][1],door1_cord[4][0],door1_cord[4][1],room1_cord[1][0],room1_cord[1][1]};
        double[][] mat={
                        {room2_cord[0][0],-room2_cord[0][1],1,0},
                        {room2_cord[0][1],room2_cord[0][0],0,1},

                        {door2_cord[0][0],-door2_cord[0][1],1,0},
                        {door2_cord[0][1],door2_cord[0][0],0,1},

                        {door2_cord[1][0],-door2_cord[1][1],1,0},
                        {door2_cord[1][1],door2_cord[1][0],0,1},

                        {door2_cord[2][0],-door2_cord[2][1],1,0},
                        {door2_cord[2][1],door2_cord[2][0],0,1},

                        {door2_cord[3][0],-door2_cord[3][1],1,0},
                        {door2_cord[3][1],door2_cord[3][0],0,1},



                        {door2_cord[4][0],-door2_cord[4][1],1,0},
                        {door2_cord[4][1],door2_cord[4][0],0,1},

                        {room2_cord[1][0],-room2_cord[1][1],1,0},
                        {room2_cord[1][1],room2_cord[1][0],0,1},
        };

        Matrix A;
        A=new Matrix(mat);
        Matrix b=new Matrix(y,14);
        Matrix answer=A.solve(b);
        double[][] zoom=new double[2][2];
        zoom[0][0]=answer.get(0,0);
        zoom[0][1]=-answer.get(1,0);
        zoom[1][0]=answer.get(1,0);
        zoom[1][1]=answer.get(0,0);
                //{{,},{answer.get(1,0),answer.get(0,0)}};
        Matrix zoom1=new Matrix(zoom);
        double[] trans=new double[2];
        trans[0]=answer.get(2,0);
        trans[1]=answer.get(3,0);
                //{answer.get(2,0),answer.get(3,0)};
        Matrix trans1=new Matrix(trans,2);
        for(int i=0;i<anglesCount;i++){
            double[]temp_mmat=new double[2];
            temp_mmat[0]=room2_cord[i][0];
            temp_mmat[1]=room2_cord[i][1];        //{room2_cord[i][0],room2_cord[i][1]};
            Matrix temp=new Matrix(temp_mmat,2);
            Matrix temp1=zoom1.times(temp).plus(trans1);
            room2_cord[i][0]=(float)temp1.get(0,0);
            room2_cord[i][1]=(float)temp1.get(1,0);
        }
        Log.e("tag","matrix1\t"+answer.get(0,0));
        Log.e("tag","matrix2\t"+answer.get(1,0));
        Log.e("tag","matrix3\t"+answer.get(2,0));
        Log.e("tag","matrix4\t"+answer.get(3,0));
        for(int i=0;i<doorcount;i++){
            double[]temp_mmat=new double[2];
            temp_mmat[0]=door2_cord[i][0];
            temp_mmat[1]=door2_cord[i][1];                //{door2_cord[i][0],door2_cord[i][1]};
            Matrix temp=new Matrix(temp_mmat,2);
            Matrix temp1=zoom1.times(temp).plus(trans1);
            door2_cord[i][0]=(float)temp1.get(0,0);
            door2_cord[i][1]=(float)temp1.get(1,0);

        }
//        double[][] Wall1={{2,2},{2,1},{2,-1},{2,-2}};
//        double[][] Wall2={{4,0},{3,1},{1,3},{0,4}};
//        double[] y={Wall1[0][0],Wall1[0][1],Wall1[1][0],Wall1[1][1],Wall1[2][0],Wall1[2][1],Wall1[3][0],Wall1[3][1]};
//        Log.e("tag", "Wall1\t"+Wall1[1][0]+" "+Wall1[1][1]);
//        double[][] mat={{Wall2[0][0],-Wall2[0][1],1,0},
//                {Wall2[0][1],Wall2[0][0],0,1},
//                {Wall2[1][0],-Wall2[1][1],1,0},
//                {Wall2[1][1],Wall2[1][0],0,1},
//                {Wall2[2][0],-Wall2[2][1],1,0},
//                {Wall2[2][1],Wall2[2][0],0,1},
//                {Wall2[3][0],-Wall2[3][1],1,0},
//                {Wall2[3][1],Wall2[3][0],0,1},};

/*        double[] y={2,-2,2,2,2,1,2,-1};

        double[][] mat={

                        {0,-4,1,0},
                        {4,0,0,1},
                        {4,0,1,0},
                        {0,4,0,1},
                        {3,-1,1,0},
                        {1,3,0,1},
                        {1,-3,1,0},
                        {3,1,0,1},

        };*/
//        Matrix A=new Matrix(mat);
//        Matrix b=new Matrix(y,8);
//        Matrix answer=A.solve(b);
//        Log.e("tag","matrix1\t"+answer.get(0,0));
//        Log.e("tag","matrix2\t"+answer.get(1,0));
//        Log.e("tag","matrix3\t"+answer.get(2,0));
//        Log.e("tag","matrix4\t"+answer.get(3,0));
//        Matrix zoom;
//        Matrix trans;

    }
        private void getdoorcoord(float[][] temp12){
            for (int i=0;i<doorcount;i=i+5){
                int index=temp[i];
                float theta1=azimuthAngles.get(index-1);
                float theta2=azimuthAngles.get(index);
                float thetad1=temp_door_angle[i];
                float thetad2=temp_door_angle[i+1];
                float thetad3=temp_door_angle[i+2];
                float thetad4=temp_door_angle[i+3];
                float thetad5=temp_door_angle[i+4];
                thetad1=thetad1-theta1;
                thetad2=thetad2-theta1;
                thetad3=thetad3-theta1;
                thetad4=thetad4-theta1;
                thetad5=thetad5-theta1;
                theta2=theta2-theta1;
                //int j=temp[i];
                float x1=temp12[index-1][0];
                float y1=temp12[index-1][1];
                float x2=temp12[index][0];
                float y2=temp12[index][1];
                doors[i][0]=thetad1*(x2-x1)/theta2+x1;
                doors[i][1]=thetad1*(y2-y1)/theta2+y1;
                doors[i+1][0]=thetad2*(x2-x1)/theta2+x1;
                doors[i+1][1]=thetad2*(y2-y1)/theta2+y1;
                doors[i+2][0]=thetad3*(x2-x1)/theta2+x1;
                doors[i+2][1]=thetad3*(y2-y1)/theta2+y1;
                doors[i+3][0]=thetad4*(x2-x1)/theta2+x1;
                doors[i+3][1]=thetad4*(y2-y1)/theta2+y1;
                doors[i+4][0]=thetad5*(x2-x1)/theta2+x1;
                doors[i+4][1]=thetad5*(y2-y1)/theta2+y1;

            }
            switch (counter){
                case 1:
                    door1_cord=doors;
                    break;
                case 2:
                    door2_cord=doors;break;
            }
        }




    /*@Override
    public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction()==e.ACTION_DOWN) {

            float angle = ((azimuth + 3.0f)*360.0f/6.0f);
            Log.e(TAG, "Touch detected : captured Azimuth angle = " + angle);
            azimuthAngles.add( angle ); //Use these angles for ray tracing and finding floor coordinates
            Toast.makeText(getApplicationContext(),"Added No."+anglesCount+" angle= "+angle,Toast.LENGTH_SHORT);//toast didnt work
            anglesCount++;
            addTextContentOnScreen("Added No."+anglesCount+" angle= "+angle);

        }
        return true;
    }*/


    public void initViews()
    {


        initCamera();

        FrameLayout crossHairFrame = new FrameLayout(this);
        crossHairFrame.setBackgroundColor(Color.TRANSPARENT);
        ImageView crosshair = new ImageView(this);
        crosshair.setImageResource(R.drawable.target);
        crossHairFrame.addView(crosshair);

        setContentView(R.layout.activity_main);

        changeroom= (Button) findViewById(R.id.change);
        door= (Button) findViewById(R.id.doors);
        corner= (Button) findViewById(R.id.corners);

        door.setOnClickListener(door_list);
        corner.setOnClickListener(corn_list);
        changeroom.setOnClickListener(room_list);

        FrameLayout cameraLayout = (FrameLayout)findViewById(R.id.camera_Layout);
        cameraLayout.addView(mPreview);
        addContentView(crossHairFrame, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));




        //For displaying all textviews
        TableLayout miscLayout = new TableLayout(this);
        magDataText0 = new TextView(this);
        magDataText1 = new TextView(this);
        magDataText2 = new TextView(this);
        magDataText0.setTextColor(Color.RED);
        magDataText1.setTextColor(Color.GREEN);
        magDataText2.setTextColor(Color.BLUE);




        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(tableParams);

        TableRow tableRow0 = new TableRow(this);
        tableRow0.setLayoutParams(tableParams);
        magDataText0.setLayoutParams(rowParams);
        tableRow0.addView(magDataText0);
        tableLayout.addView(tableRow0, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TableRow tableRow1 = new TableRow(this);
        tableRow1.setLayoutParams(tableParams);
        magDataText1.setLayoutParams(rowParams);
        tableRow1.addView(magDataText1);
        tableLayout.addView(tableRow1, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TableRow tableRow2 = new TableRow(this);
        tableRow2.setLayoutParams(tableParams);
        magDataText2.setLayoutParams(rowParams);
        tableRow2.addView(magDataText2);
        tableLayout.addView(tableRow2, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        addContentView(tableLayout, new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


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

            //setWallCoords();
            //show a loading sign untill Wall Cords are set. Then proceed to FloorPlanActivity


            Log.d(TAG,"menu item action_render clicked");
          Log.d("GP","Floor numOfWalls "+ FloorCoordsGlobal.numOfWalls);
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

        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);


    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause MainActivity");
        releaseCamera();
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

    public void setWallCoords(float[][] temp_cords)
    {

        //For temp - 6 walls
        float[][] tempCords =  temp_cords;
           //Get this from ray tracing algo implementation
        int noOfWalls = anglesCount;

        /*
        //FOr temp - 4 walls
        float[][] tempCords = { {0.7071f, 0.7071f} ,
                                { -0.7071f,0.7071f},
                                {-0.7071f,-0.7071f},
                                {0.7071f,-0.7071f }
                    };   //Get this from ray tracing algo implementation

        int noOfWalls = 4; */
        float zHeight = 0.25f;

        FloorCoordsGlobal.numOfWalls += noOfWalls;

        //Conversion of corner points to wall coordinates.

        for(int i=0;i<noOfWalls;i++)
        {
            int n = i+1;
            if(n==noOfWalls)
                n=0; // To make n as 0 when i is last point

            FloorCoordsGlobal.wallCoords[prevWallCount+i][0] = tempCords[i][0];   // 'i' th Wall's 1st x cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][1] = tempCords[i][1];   // 'i' th Wall's 1st y cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][2] = -zHeight;
            Log.e("GP","[" +(prevWallCount+i)+" ]["+0+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][0]+" [" +(prevWallCount+i)+" ]["+1+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][1]);
            //Log.d("GP","[" +(12*(counter-1)+i)+" ]["+1+" = "+FloorCoordsGlobal.wallCoords[12*(counter-1)+i][1]);


            FloorCoordsGlobal.wallCoords[prevWallCount+i][3] = tempCords[n][0];   // 'i' th Wall's 2nd x cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][4] = tempCords[n][1];   // 'i' th Wall's 2nd y cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][5] = -zHeight;
            Log.e("GP","[" +(prevWallCount+i)+" ]["+3+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][3]+"[" +(prevWallCount+i)+" ]["+4+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][4]);
            //Log.d("GP","[" +(12*(counter-1)+i)+" ]["+4+" = "+FloorCoordsGlobal.wallCoords[12*(counter-1)+i][4]);

            FloorCoordsGlobal.wallCoords[prevWallCount+i][6] = tempCords[n][0];   // 'i' th Wall's 3rd x cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][7] = tempCords[n][1];   // 'i' th Wall's 3rd y cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][8] = zHeight;
            Log.e("GP","[" +(prevWallCount+i)+" ]["+6+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][6]+"[" +(12*(prevWallCount+i)+" ]["+7+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][7]));
            //Log.d("GP","[" +(12*(counter-1)+i)+" ]["+7+" = "+FloorCoordsGlobal.wallCoords[12*(counter-1)+i][7]);

            FloorCoordsGlobal.wallCoords[prevWallCount+i][9] = tempCords[i][0];   // 'i' th Wall's 3rd x cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][10] = tempCords[i][1];   // 'i' th Wall's 3rd y cord
            FloorCoordsGlobal.wallCoords[prevWallCount+i][11] = zHeight;
            Log.e("GP","[" +(prevWallCount+i)+" ]["+9+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][9]+"[" +(prevWallCount+i)+" ]["+10+" = "+FloorCoordsGlobal.wallCoords[prevWallCount+i][10]);
            //Log.d("GP","[" +(12*(counter-1)+i)+" ]["+10+" = "+FloorCoordsGlobal.wallCoords[12*(counter-1)+i][10]);

        }
        prevWallCount += noOfWalls;
    }

    public void addTextContentOnScreen(String text)
    {

        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow tableRow0 = new TableRow(this);
        tableRow0.setLayoutParams(tableParams);

        TextView  tv = new TextView(this);
        tv.setLayoutParams(rowParams);
        tv.setTextColor(Color.GREEN);
        tv.setText(text);
        tableRow0.addView(tv);
        tableLayout.addView(tableRow0, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private float[][] coordinates(){

        float[] angles=new float[10];
        int  length;
        float mindistance=10000,distance1=0,distance2=0,total=0,norm=0,minangle=0;
        float x1=0,y1=0,xinter=0,yinter=0,det=0;
        float lineparams[]=new float[5],line1params[]=new float[3],line2params[]=new float[3],v1[]=new float[10];
        float v2[]=new float[10],intersection[]=new float[2],test[][]=new float[10][2],finalpointset[][]=new float[10][2];

        for(int i=0;i<azimuthAngles.size();i++)
            angles[i]=azimuthAngles.get(i);
        sort(angles, 0, 4);
        angles[anglesCount]=angles[0];
        length=anglesCount+1;
        x1= (float) cos(getangleinradian(angles[0]));
        y1= (float) sin(getangleinradian(angles[0]));
        for(int alpha=0;alpha<=360;alpha+=1) {
            lineparams[0] = (float) (-1 * sin(getangleinradian(angles[0] - alpha)));
            lineparams[1] = (float) cos(getangleinradian(angles[0] - alpha));
            lineparams[2] = (float) (y1 * cos(getangleinradian(angles[0] - alpha)) - x1 * sin(getangleinradian(angles[0] - alpha)));

            line1params[0] = lineparams[0];
            line1params[1] = lineparams[1];
            line1params[2] = lineparams[2];

            v1[0] = x1;
            v2[0] = y1;

            for (int i = 1; i < length; i++) {
                line2params[0] = (float) (-1 * sin(getangleinradian(angles[i])));
                line2params[1] = (float) cos(getangleinradian(angles[i]));
                line2params[2] = 0;

                det = (line1params[0] * line2params[1]) - (line1params[1] * line2params[0]);

                xinter = ((line2params[1] * line1params[2]) - (line1params[1] * line2params[2])) / det;
                yinter = ((line1params[0] * line2params[2]) - (line2params[0] * line1params[2])) / det;

                lineparams[0] = line1params[1];
                lineparams[1] = -1 * line1params[0];
                lineparams[2] = (-1 * yinter * line1params[0]) + (xinter * line1params[1]);

                line1params[0] = lineparams[0];
                line1params[1] = lineparams[1];
                line1params[2] = lineparams[2];

                v1[i] = xinter;
                v2[i] = yinter;

            }

            for (int j = 0; j < length; j++) {
                test[j][0] = v1[j];
                test[j][1] = v2[j];
                //Log.e("Error","V1 V2"+v1[j]+" "+v2[j]);
            }

            distance1 = (float) ((test[0][0] - test[(length - 1)][0]) * (test[0][0] - test[(length - 1)][0]));
            distance2 = (float) ((test[0][1] - test[(length - 1)][1]) * (test[0][1] - test[(length - 1)][1]));
            total = distance1 + distance2;
            norm = (float) sqrt(total);
            if (mindistance > norm) {
                mindistance = norm;
                minangle = alpha;
                for (int j = 0; j < length; j++) {
                    finalpointset[j][0] = test[j][0];
                    finalpointset[j][1] = test[j][1];
                }


            }
        }
        switch (counter){
            case 1:
                room1_cord=finalpointset;
                break;
            case 2:
                room2_cord=finalpointset;
                break;
            case 3:
                room3_cord=finalpointset;
                break;
            case 4:
                room4_cord=finalpointset;
                break;
        }
    return finalpointset;
    }
    private float getangleinradian(float angle){

        float x= (float) (angle*Math.PI/180);
        return x;
    }
}
