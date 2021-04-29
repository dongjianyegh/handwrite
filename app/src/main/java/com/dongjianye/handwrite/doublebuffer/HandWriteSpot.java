package com.dongjianye.handwrite.doublebuffer;

import android.opengl.GLES20;

import com.dongjianye.handwrite.base.Constants;
import com.dongjianye.handwrite.base.VertexArray;

/**
 * @author dongjianye on 4/28/21
 */
class HandWriteSpot {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            -1.0f, 1.0f, //左上
            1.0f, 1.0f, // 右上
            -1.0f, -1.0f, // 左下
            1.0f, -1.0f, // 右下
    };

    private static final float[] TEXTURE_DATA = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    private final VertexArray vertexArray;

    private final float[] mTempVertexData = new float[VERTEX_DATA.length];

    private int mWidth;
    private int mHeight;

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     *
     * @param x 相对于窗口的位置X
     * @param y 相对于窗口的位置Y
     */
    public void updateVertexPosition(final float x, final float y, final float size) {
        final float centerH = mWidth / 2.0f;
        final float centerV = mHeight / 2.0f;
        final float halfSize = size / 2;
        // 分别计算出 左上， 右上， 左下， 右下的位置，一定是可以用个简单的函数映射的
        final float left = (x - halfSize) / centerH - 1.0f;
        final float right = (x + halfSize) / centerH - 1.0f;
        final float top = (mHeight - y + halfSize) / centerV - 1.0f;
        final float bottom = (mHeight - y - halfSize) / centerV - 1.0f;

        mTempVertexData[0] = left;
        mTempVertexData[1] = top;
        mTempVertexData[2] = right;
        mTempVertexData[3] = top;
        mTempVertexData[4] = left;
        mTempVertexData[5] = bottom;
        mTempVertexData[6] = right;
        mTempVertexData[7] = bottom;
    }

    public HandWriteSpot() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }


    public void bindData(HandWriteSpotShaderProgram textureProgram) {

        // 设置顶点数据
        vertexArray.updateVertexAttributePointer(mTempVertexData,
                textureProgram.getPositionAttributeLocation(),POSITION_COMPONENT_COUNT);

        vertexArray.setTextureAttributePointer(TEXTURE_DATA,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }


}