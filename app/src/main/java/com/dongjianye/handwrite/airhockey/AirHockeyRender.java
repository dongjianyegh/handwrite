package com.dongjianye.handwrite.airhockey;

import android.content.Context;
import android.graphics.Matrix;
import android.opengl.GLSurfaceView;

import com.dongjianye.handwrite.R;
import com.dongjianye.handwrite.base.LoggerConfig;
import com.dongjianye.handwrite.base.MatrixHelper;
import com.dongjianye.handwrite.base.ShaderHelper;
import com.dongjianye.handwrite.base.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_LINES;
import static android.opengl.GLES10.GL_TRIANGLES;
import static android.opengl.GLES10.GL_TRIANGLE_STRIP;
import static android.opengl.GLES10.glClear;
import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES10.glViewport;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * @author dongjianye on 4/27/21
 */
public class AirHockeyRender implements GLSurfaceView.Renderer {

    private static final String U_COLOR = "u_color";
    private static final String A_COLOR = "a_Color";
    private static final String A_POSITION = "a_Position";
    private static final String U_MATRIX = "u_Matrix";

    private static final int BYTES_PER_FLOAT = 4;

    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int COLOR_COMPONENT_COUNT = 3;

    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private final Context mContext;

    private int mProgram;
    private int mColorLocation;
    private int mPositionLocation;


    private int mMatrixLocation;

    private final FloatBuffer vertexData;

    private final float[] mMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

//    float[] tableVerticesWithTriangles = {
//            // Order of coordinates: X, Y, R, G, B
//
//            // Triangle 1
//            -0.5f, -0.5f,
//            0.5f, 0.5f,
//            -0.5f, 0.5f,
//
//            // Triangle 2
//            -0.5f, -0.5f,
//            0.5f, -0.5f,
//            0.5f, 0.5f,
//
//            // line 1
//            -0.5f, 0f,
//            0.5f, 0f,
//
//            0f, -0.25f,
//            0f, 0.25f
//    };

//    float[] tableVerticesWithTriangles = {
//            // Order of coordinates: X, Y, R, G, B
//            0, 0,          0f,   1.5f,          1f, 1f, 1f,
//            // Triangle 1
//            -0.5f, -0.8f,  0f,   1f,    0.7f, 0.7f, 0.7f,
//            0.5f, -0.8f,   0f,   1f,     0.7f, 0.7f, 0.7f,
//            0.5f, 0.8f,    0f,   2f,     0.7f, 0.7f, 0.7f,
//            -0.5f, 0.8f,   0f,   2f,     0.7f, 0.7f, 0.7f,
//            -0.5f, -0.8f,  0f,   1f,     0.7f, 0.7f, 0.7f,
//
//            // line 1
//            -0.5f, 0f,   0f,   1.5f,       1f, 0f, 0f,
//            0.5f, 0f,    0f,   1.5f,   1f, 0f, 0f,
//
//            0f, -0.25f,  0f,   1.25f,       0.7f, 0.7f, 0.7f,
//            0f, 0.25f,   0f,   1.75f,       0.7f, 0.7f, 0.7f,
//    };

    float[] tableVerticesWithTriangles = {
            // Order of coordinates: X, Y, R, G, B
            0, 0,                       1f, 1f, 1f,
            // Triangle 1
            -0.5f, -0.8f,       0.7f, 0.7f, 0.7f,
            0.5f, -0.8f,         0.7f, 0.7f, 0.7f,
            0.5f, 0.8f,          0.7f, 0.7f, 0.7f,
            -0.5f, 0.8f,         0.7f, 0.7f, 0.7f,
            -0.5f, -0.8f,        0.7f, 0.7f, 0.7f,

            // line 1
            -0.5f, 0f,            1f, 0f, 0f,
            0.5f, 0f,         1f, 0f, 0f,

            0f, -0.25f,            0.7f, 0.7f, 0.7f,
            0f, 0.25f,             0.7f, 0.7f, 0.7f,
    };

    public AirHockeyRender(Context context) {
        mContext = context;

        vertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        glUseProgram(mProgram);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(mProgram);
        }

//        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
//        mColorLocation = glGetUniformLocation(mProgram, A_COLOR);
//
//        // Bind our data, specified by the variable vertexData, to the vertex
//        // attribute at location A_POSITION_LOCATION.
//        vertexData.position(0);
//        glVertexAttribPointer(mPositionLocation,
//                POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
//
//        glEnableVertexAttribArray(mPositionLocation);


        mPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
        mColorLocation = glGetAttribLocation(mProgram, A_COLOR);
        mMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);

        vertexData.position(0);

        glVertexAttribPointer(mPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        glEnableVertexAttribArray(mPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);

        glVertexAttribPointer(mColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        glEnableVertexAttribArray(mColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

//        final float aspectRatio = width > height ?
//                (float) width / (float) height :
//                (float) height / (float) width;
//        if (width > height) {
//            // Landscape
//            orthoM(mMatrix, 0,
//                    -aspectRatio, aspectRatio,
//                    -1f, 1f,
//                    -1f, 1f);
//        } else {
//            // Portrait or square
//            orthoM(mMatrix, 0,
//                    -1f, 1f,
//                    -aspectRatio, aspectRatio,
//                    -1f, 1f);
//        }
        MatrixHelper.perspectiveM(mMatrix, 45, (float) width
                / (float) height, 1f, 10f);

        setIdentityM(modelMatrix, 0);

        translateM(modelMatrix, 0, 0f, 0f, -2.5f);
//        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, mMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, mMatrix, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
//        gl.glEnable(0x8642);
        glUniformMatrix4fv(mMatrixLocation, 1, false, mMatrix, 0);

//        glUniform4f(mColorLocation, 1.0f, 1.0f, 1.0f, 0.0f);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

//        glUniform4f(mColorLocation, 1.0f, 0.0f, 0.0f, 0.0f);
        glDrawArrays(GL_LINES, 6, 2);

//        glUniform4f(mColorLocation, 1.0f, 0.0f, 0.0f, 0.0f);
        glDrawArrays(GL_POINTS, 8, 1);

//        glUniform4f(mColorLocation, 0.0f, 0.0f, 1.0f, 0.0f);
        glDrawArrays(GL_POINTS, 9, 1);
    }
}