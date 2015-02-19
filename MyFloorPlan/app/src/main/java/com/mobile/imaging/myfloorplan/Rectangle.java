package com.mobile.imaging.myfloorplan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Pradeep on 2/18/2015.
 */
public class Rectangle {
        private final String vertexShaderCode =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +

                        "attribute vec4 vPosition;" +
                        "attribute vec4 a_color;" +
                        "attribute vec2 tCoordinate;" +
                        "varying vec2 v_tCoordinate;" +
                        "varying vec4 v_Color;" +
                        "void main() {" +
                        // the matrix must be included as a modifier of gl_Position
                        "  gl_Position = vPosition * uMVPMatrix;" +
                        "	v_tCoordinate = tCoordinate;" +
                        "	v_Color = a_color;" +
                        "}";


    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

        private final FloatBuffer vertexBuffer;
        private final ShortBuffer drawListBuffer;
        private final int mProgram;
        private int mPositionHandle;

        private int mColorHandle;
        private int mMVPMatrixHandle;

        private Context context;

      // number of coordinates per vertex in this array
        static final int COORDS_PER_VERTEX = 3;

        private final short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

        private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        float color[];

        public Rectangle(Context context, float[] rectCoords, float[] rgba ) {
            this.context = context;

            color = rgba;

            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    rectCoords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(rectCoords);
            vertexBuffer.position(0);

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);

            //prepare shaders and OpenGL program
            int vertexShader = FloorGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
            int fragmentShader = FloorGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);

            mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
            GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

            // Enable depth buffer  //GP
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            // Enable back face culling //GP
            //  GLES20.glEnable(GLES20.GL_CULL_FACE);
        }

        public void draw(float[] mvpMatrix) {
            // Add program to OpenGL environment
            GLES20.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

            // Set color for drawing the triangle
            GLES20.glUniform4fv(mColorHandle, 1, color, 0);



            FloorGLRenderer.checkGlError("glVertexAttribPointer...texCoord");
            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            FloorGLRenderer.checkGlError("glGetUniformLocation");

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
            FloorGLRenderer.checkGlError("glUniformMatrix4fv");

            // Draw the square
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }
