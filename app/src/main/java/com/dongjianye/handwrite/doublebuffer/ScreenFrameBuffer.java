package com.dongjianye.handwrite.doublebuffer;

import android.opengl.GLES20;

import com.dongjianye.handwrite.base.TextureHelper;
import com.dongjianye.handwrite.base.VertexArray;

/**
 * @author dongjianye on 4/29/21
 */
public class ScreenFrameBuffer {

    // 这跟那个得反过来一次
    private static final float[] VERTEX_DATA = {
            -1.0f, -1.0f, // 左下
            1.0f, -1.0f, // 右下
            -1.0f, 1.0f, //左上
            1.0f, 1.0f, // 右上
    };

    private static final float[] TEXTURE_DATA = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    private int mFrameBufferId;
    private int mBufferTextureId;

    private int mBufferTextureWidth;
    private int mBufferTextureHeight;

    private final VertexArray vertexArray;

    private final HandWriteSpotShaderProgram mScreenProgram;

    public ScreenFrameBuffer() {
        vertexArray = new VertexArray(VERTEX_DATA);
        mScreenProgram = new HandWriteSpotShaderProgram();
    }

    public void createFrameBuffer(int width, int height) {
//        mBufferTextureWidth = Utils.getNextPower2(width);
//        mBufferTextureHeight = Utils.getNextPower2(height);

        mBufferTextureWidth = width;
        mBufferTextureHeight = height;

        mBufferTextureId = TextureHelper.genTexture(mBufferTextureWidth, mBufferTextureHeight);
        mFrameBufferId = TextureHelper.createFrameBufferForTexture(mBufferTextureWidth, mBufferTextureHeight, mBufferTextureId);
    }

    public int getFrameBufferId() {
        return mFrameBufferId;
    }

    public void draw() {
        mScreenProgram.useProgram();
        mScreenProgram.setUniforms(mBufferTextureId);


        // 设置顶点数据
        vertexArray.updateVertexAttributePointer(VERTEX_DATA,
                mScreenProgram.getPositionAttributeLocation(),2);

        vertexArray.setTextureAttributePointer(TEXTURE_DATA,
                mScreenProgram.getTextureCoordinatesAttributeLocation(),
                2);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}