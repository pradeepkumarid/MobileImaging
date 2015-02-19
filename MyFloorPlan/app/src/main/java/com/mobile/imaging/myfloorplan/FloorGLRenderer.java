package com.mobile.imaging.myfloorplan;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Pradeep on 2/18/2015.
 */
public class FloorGLRenderer implements GLSurfaceView.Renderer {

        private Context _context;

        private static final String TAG = "GP";
//        private Rectangle mRectangle1;
//        private Rectangle mRectangle2;
//        private Rectangle mRectangle3;
//        private Rectangle mRectangle4;
//        private Rectangle mRectangle5;

        private ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();


        private final float[] mMVPMatrix = new float[16];
        private final float[] mProjMatrix = new float[16];
        private final float[] mVMatrix = new float[16];
        private final float[] mRotationMatrix = new float[16];

        public FloorGLRenderer(Context context) {
            _context = context;
        }

        // Declare as volatile because we are updating it from another thread
        public volatile float mAngle;

        public static volatile float xAngle;
        public static volatile float yAngle;
        public static volatile float zAngle;

        public void onSurfaceCreated(GL10 unused, EGLConfig config) {

            float rectCoords1[] =  {
                    -1.0f, 0.5f, -0.25f,   // 0 top left
                    -1.0f, -0.5f, -0.25f,   // 1 bottom left
                    1.0f, -0.5f, -0.25f,   // 2  bottom right
                    1.0f, 0.5f, -0.25f}; // 3  top right



            float rectCoords2[] = {
                    -1.0f, 0.5f, -0.25f,   // 0
                    1.0f, 0.5f, -0.25f,   // 3
                    1.0f, 0.5f, 0.25f,   // 4
                    -1.0f, 0.5f, 0.25f}; // 5


            float rectCoords3[] = {
                    1.0f, -0.5f, -0.25f,     // 2
                    1.0f, 0.5f, -0.25f,    // 3
                    1.0f, 0.5f, 0.25f,   // 4
                    1.0f, -0.5f, 0.25f}; // 7

            float rectCoords4[] ={
                    -1.0f, 0.5f, -0.25f,   // 0
                    -1.0f, -0.5f, -0.25f,   // 1
                    -1.0f, -0.5f, 0.25f,   // 6
                    -1.0f, 0.5f, 0.25f}; // 5


            float rectCoords5[] = {
                    -1.0f, -0.5f, -0.25f,   // 1
                    1.0f, -0.5f, -0.25f,     // 2
                    1.0f, -0.5f, 0.25f, // 7
                    -1.0f, -0.5f, 0.25f};   // 6



            float color1[] = {0.5f, 0.1f, 0.1f, 1.0f};  //floor red
//            float color2[] = {0.3f, 0.5f, 0.7f, 1.0f};
//            float color3[] = {0.4f, 0.5f, 0.7f, 1.0f};
//            float color4[] = {0.3f, 0.6f, 0.7f, 1.0f};
//            float color5[] = {0.3f, 0.5f, 0.6f, 1.0f};
            float[][] color =  { {0.3f, 0.5f, 0.7f, 1.0f},
                                 {0.4f, 0.5f, 0.7f, 1.0f},
                                 {0.3f, 0.6f, 0.7f, 1.0f},
                                 {0.3f, 0.5f, 0.6f, 1.0f} };



//            mRectangle1 = new Rectangle(_context,rectCoords1,color1);
//            mRectangle2 = new Rectangle(_context,rectCoords2,color2);
//            mRectangle3 = new Rectangle(_context,rectCoords3,color3);
//            mRectangle4 = new Rectangle(_context,rectCoords4,color4);
//            mRectangle5 = new Rectangle(_context,rectCoords5,color5);

            for(int i=0;i<FloorCoordsGlobal.numOfWalls;i++)
            {
                rectangles.add(new Rectangle(_context,FloorCoordsGlobal.wallCoords[i],color[i%4]));
            }

        }

        public void onDrawFrame(GL10 unused) {

            // Draw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            // Set the camera position (View matrix)
            Matrix.setLookAtM(mVMatrix, 0, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);



            // Create a rotation for the triangle
            long time2 = SystemClock.uptimeMillis() % 4000L;
            float angle2 = 0.090f * ((int) time2);
            //   Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
            //   Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

            Matrix.setRotateM(mRotationMatrix, 0, xAngle * 3.6f, 1.0f, 0, 0);

            // Combine the rotation matrix with the projection and camera view
            Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);


            Matrix.setRotateM(mRotationMatrix, 0, yAngle* 3.6f, 0, 1.0f, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

            Matrix.setRotateM(mRotationMatrix, 0, zAngle * 3.6f, 0, 0, 1.0f);
            Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

            // Draw triangle
            //     mTriangle.draw(mMVPMatrix);

            // Draw square
//            mRectangle1.draw(mMVPMatrix); //  front red
//            mRectangle2.draw(mMVPMatrix); //top green
//            mRectangle3.draw(mMVPMatrix);  //left blue
//            mRectangle4.draw(mMVPMatrix);  //right yellow
//            mRectangle5.draw(mMVPMatrix);  //right yellows

            for(Rectangle mRectangle : rectangles)
            {
                mRectangle.draw(mMVPMatrix);
            }

        }

        public void onSurfaceChanged(GL10 unused, int width, int height) {
            // Adjust the viewport based on geometry changes,
            // such as screen rotation
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 100);

        }

        public static int loadShader(int type, String shaderCode) {

            // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
            // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES20.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }

        /**
         * Utility method for debugging OpenGL calls. Provide the name of the call
         * just after making it:
         * <p/>
         * <pre>
         * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
         * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
         *
         * If the operation is not successful, the check throws an error.
         *
         * @param glOperation - Name of the OpenGL call to check.
         */
        public static void checkGlError(String glOperation) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, glOperation + ": glError " + error);
                throw new RuntimeException(glOperation + ": glError " + error);
            }
        }
    }
