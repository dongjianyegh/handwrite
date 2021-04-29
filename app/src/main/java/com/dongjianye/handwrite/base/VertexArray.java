package com.dongjianye.handwrite.base;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2016/7/5.
 */
public class VertexArray {
    private final FloatBuffer mVertexFloatBuffer;
    private final FloatBuffer mTextureFloatBuffer;

    // 采用同样的高度
    public VertexArray(float[] vertexData) {
        mVertexFloatBuffer = ByteBuffer.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

        mTextureFloatBuffer = ByteBuffer.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)
              .order(ByteOrder.nativeOrder())
              .asFloatBuffer();
    }

    public void updateVertexAttributePointer(float[] vertexData, int attr, int componentCount) {
        mVertexFloatBuffer.put(vertexData);
        mVertexFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(attr, componentCount, GLES20.GL_FLOAT,false, 0, mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(attr);
    }

    public void setTextureAttributePointer(float[] vertexData, int attr, int componentCount) {
        mTextureFloatBuffer.put(vertexData);
        mTextureFloatBuffer.position(0);
        GLES20.glVertexAttribPointer(attr, componentCount, GLES20.GL_FLOAT,false, 0, mTextureFloatBuffer);
        GLES20.glEnableVertexAttribArray(attr);
    }
}
