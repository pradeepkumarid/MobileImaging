/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobile.imaging.myfloorplan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Context _context;

    private static final String TAG = "GP";
    private Triangle mTriangle;
    private Square mSquare1;
    private Square mSquare2;
    private Square mSquare3;
    private Square mSquare4;
    private Square mSquare5;
    private Square mSquare6;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    public MyGLRenderer(Context context) {
        _context = context;
    }

    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;

    public static volatile float xAngle;
    public static volatile float yAngle;
    public static volatile float zAngle;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
       // GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        //Dont enable this- to make openGL transparant

      //  mTriangle = new Triangle(_context);
        mSquare1 = new Square(_context,1);
        mSquare2 = new Square(_context,2);
        mSquare3 = new Square(_context,3);
        mSquare4 = new Square(_context,4);
        mSquare5 = new Square(_context,5);
        mSquare6 = new Square(_context,6);
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
        mSquare1.draw(mMVPMatrix); //  front red
        mSquare2.draw(mMVPMatrix); //top green
        mSquare3.draw(mMVPMatrix);  //left blue
        mSquare4.draw(mMVPMatrix);  //right yellow
        mSquare5.draw(mMVPMatrix); //down cyan
        mSquare6.draw(mMVPMatrix); //back pink
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



