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
class Square {
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
                    "varying vec4 v_Color;" +
                    "varying vec2 v_tCoordinate;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    // texture2D() is a build-in function to fetch from the texture map
                    "	vec4 texColor = texture2D(s_texture, v_tCoordinate); " +
                    "  gl_FragColor =  texColor;" +
                    "}";

/*
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}"; */

    private final FloatBuffer vertexBuffer,texCoordBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle,mTexCoordHandle;

    private int mColorHandle, mTextureUniformHandle;
    private int mMVPMatrixHandle;
    private int mTextureDataHandle;

    private Context context;


    //===================================
    static final int COORDS_PER_TEX = 2;
    static float texCoord[] = {
/*
            0.0f, 1.0f,     // top left     (V2)
            0.0f, 0.0f,     // bottom left  (V1)
            1.0f, 1.0f,     // top right    (V4)
            1.0f, 0.0f      // bottom right (V3)
*/


            0.0f, 0.0f,
            0.0f, 0.167f,
            1.0f, 0.167f,
            1.0f, 0.0f


    };
    static float texCoord1[] = {
            0.0f, 0 * 1.0f/6 ,
            0.0f, 1 * 1.0f/6 ,
            1.0f, 1 * 1.0f/6 ,
            1.0f, 0 * 1.0f/6
    };

    static float texCoord2[] = {
            0.0f, 1 * 1.0f/6 ,
            0.0f, 2 * 1.0f/6 ,
            1.0f, 2 * 1.0f/6 ,
            1.0f, 1 * 1.0f/6
    };
    static float texCoord3[] = {
            0.0f, 2 * 1.0f/6 ,
            0.0f, 3 * 1.0f/6 ,
            1.0f, 3 * 1.0f/6 ,
            1.0f, 2 * 1.0f/6
    };
    static float texCoord4[] = {
            0.0f, 3 * 1.0f/6 ,
            0.0f, 4 * 1.0f/6 ,
            1.0f, 4 * 1.0f/6 ,
            1.0f, 3 * 1.0f/6
    };
    static float texCoord5[] = {
            0.0f, 4 * 1.0f/6 ,
            0.0f, 5 * 1.0f/6 ,
            1.0f, 5 * 1.0f/6 ,
            1.0f, 4 * 1.0f/6
    };
    static float texCoord6[] = {
            0.0f, 5 * 1.0f/6 ,
            0.0f, 6 * 1.0f/6 ,
            1.0f, 6 * 1.0f/6 ,
            1.0f, 5 * 1.0f/6
    };

    private final int texCoordStride = COORDS_PER_TEX * 4; // 4 bytes per float



    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords1[] = {
            -0.25f, 0.25f, -0.25f,   // 0 top left
            -0.25f, -0.25f, -0.25f,   // 1 bottom left
            0.25f, -0.25f, -0.25f,   // 2  bottom right
            0.25f, 0.25f, -0.25f}; // 3  top right

    static float squareCoords2[] = {
            -0.25f, 0.25f, -0.25f,   // 0
            0.25f, 0.25f, -0.25f,   // 3
            0.25f, 0.25f, 0.25f,   // 4
            -0.25f, 0.25f, 0.25f}; // 5

    static float squareCoords3[] = {
            0.25f, -0.25f, -0.25f,     // 2
            0.25f, 0.25f, -0.25f,    // 3
            0.25f, 0.25f, 0.25f,   // 4
            0.25f, -0.25f, 0.25f}; // 7

    static float squareCoords4[] = {
            -0.25f, 0.25f, -0.25f,   // 0
            -0.25f, -0.25f, -0.25f,   // 1
            -0.25f, -0.25f, 0.25f,   // 6
            -0.25f, 0.25f, 0.25f}; // 5

    static float squareCoords5[] = {
            -0.25f, -0.25f, -0.25f,   // 1
            0.25f, -0.25f, -0.25f,     // 2
            0.25f, -0.25f, 0.25f, // 7
            -0.25f, -0.25f, 0.25f};   // 6


    static float squareCoords6[] = {
            -0.25f, -0.25f, 0.25f,   // 6
            0.25f, -0.25f, 0.25f, // 7
            0.25f, 0.25f, 0.25f,   // 4
            -0.25f, 0.25f, 0.25f}; // 5

    private final short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color1[] = {1.0f, 0.1f, 0.1f, 1.0f};
    float color2[] = {0.1f, 1.0f, 0.1f, 1.0f};
    float color3[] = {0.1f, 0.1f, 1.0f, 1.0f};
    float color4[] = {1.0f, 1.0f, 0.1f, 1.0f};
    float color5[] = {0.1f, 1.0f, 1.0f, 1.0f};
    float color6[] = {1.0f, 0.1f, 1.0f, 1.0f};
    float color[];

    public Square(Context context, int faceNo) {
        this.context = context;
        float squareCoords[] = squareCoords1;

        switch(faceNo)
        {
            case 1:
                squareCoords = squareCoords1;
                color = color1;
                texCoord = texCoord1;
                break;
            case 2:
                squareCoords = squareCoords2;
                color = color2;
                texCoord = texCoord2;
                break;
            case 3:
                squareCoords = squareCoords3;
                color = color3;
                texCoord = texCoord3;
                break;
            case 4:
                squareCoords = squareCoords4;
                color = color4;
                texCoord = texCoord4;
                break;
            case 5:
                squareCoords = squareCoords5;
                color = color5;
                texCoord = texCoord5;
                break;
            case 6:
                squareCoords = squareCoords6;
                color = color6;
                texCoord = texCoord6;
                break;
        }


        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


        //===================================
        // texture coordinate
        //===================================
        // initialize texture coord byte buffer for texture coordinates
        ByteBuffer texbb = ByteBuffer.allocateDirect(
                texCoord.length * 4);
        // use the device hardware's native byte order
        texbb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        texCoordBuffer = texbb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        texCoordBuffer.put(texCoord);
        // set the buffer to read the first coordinate
        texCoordBuffer.position(0);



        //===================================
        // loading an image into texture
        //===================================
        mTextureDataHandle = loadTexture(context, R.drawable.dice);



        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
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


    public static int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
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


        // setting texture coordinate to vertex shader
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "tCoordinate");
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, COORDS_PER_TEX,
                GLES20.GL_FLOAT, false,
                texCoordStride, texCoordBuffer);
        MyGLRenderer.checkGlError("glVertexAttribPointer...texCoord");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // texture
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);




        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}