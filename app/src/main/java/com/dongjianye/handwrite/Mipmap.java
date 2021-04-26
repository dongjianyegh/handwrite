package com.dongjianye.handwrite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_MODELVIEW;
import static android.opengl.GLES10.GL_MODULATE;
import static android.opengl.GLES10.GL_TEXTURE_ENV;
import static android.opengl.GLES10.GL_TEXTURE_ENV_MODE;
import static android.opengl.GLES20.GL_REPLACE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;


public class Mipmap {
    private final float[] mVertexFloats = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private final float[] mTexCoordFloats = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private final float[] mAlphaVertexFloats = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    private final FloatBuffer mAlphaVertextPointers;
    private final FloatBuffer mVertexPointers; // 顶点坐标
    private final FloatBuffer mTexCoordPointers; // 纹理坐标

    private final int[] mGenTextureIds = new int[3];
    private final float[] mTempAlphaVertexFloats = new float[mAlphaVertexFloats.length];
    private final float[] mTempTexCoordFloats = new float[mTexCoordFloats.length];
    private Bitmap mBitmap;
    private int resourceId = 1;

    public Mipmap() {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(mAlphaVertexFloats.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        mAlphaVertextPointers = allocateDirect.asFloatBuffer();
        mAlphaVertextPointers.put(mAlphaVertexFloats);
        mAlphaVertextPointers.position(0);

        ByteBuffer allocateDirect2 = ByteBuffer.allocateDirect(mVertexFloats.length * 4);
        allocateDirect2.order(ByteOrder.nativeOrder());
        mVertexPointers = allocateDirect2.asFloatBuffer();
        mVertexPointers.put(mVertexFloats);
        mVertexPointers.position(0);

        ByteBuffer allocateDirect3 = ByteBuffer.allocateDirect(mTexCoordFloats.length * 4);
        allocateDirect3.order(ByteOrder.nativeOrder());
        mTexCoordPointers = allocateDirect3.asFloatBuffer();
        mTexCoordPointers.put(mTexCoordFloats);
        mTexCoordPointers.position(0);
    }

    public void setResource(int i) {
        resourceId = i;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    /* access modifiers changed from: package-private */
    public void checkGlError(GL gl) {
        int glGetError = ((GL10) gl).glGetError();
        if (glGetError != 0) {
            String name = Mipmap.class.getName();
            Log.e(name, "GLError 0x" + Integer.toHexString(glGetError));
            throw new RuntimeException("GLError 0x" + Integer.toHexString(glGetError));
        }
    }

    /**
     * 在指定的texture上绘制三角形
     * @param gl10
     * @param x
     * @param y
     * @param size
     * @param index
     */
    public void drawTriangleAtSpecialTexture(GL10 gl10, float x, float y, float size, int index) {
        for (int i = 0; i < mVertexFloats.length; ++i) {
            mTempAlphaVertexFloats[i] = mVertexFloats[i] * size;
            if (i % 2 == 0) {
                mTempAlphaVertexFloats[i] = mTempAlphaVertexFloats[i] + (x - (size / 2.0f));
            } else {
                mTempAlphaVertexFloats[i] = mTempAlphaVertexFloats[i] + (y - (size / 2.0f));
            }
        }

        Log.d("HandWriteRender", String.format("drawTriangleAtSpecialTexture: %s", getFloatArrayString(mTempAlphaVertexFloats)));

        mVertexPointers.put(mTempAlphaVertexFloats);
        mVertexPointers.position(0);
        mTexCoordPointers.put(mTexCoordFloats);
        mTexCoordPointers.position(0);
        gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexPointers);
        gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordPointers);
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mGenTextureIds[index]);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, 9987.0f);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, 9729.0f);
        gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    private static String getFloatArrayString(float[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; ++i) {
            if (i % 2 == 0) {
                builder.append("(x=").append(array[i]);
            } else {
                builder.append(",y=").append(array[i]).append(")");
            }
        }

        return builder.toString();
    }

    public void a(GL10 gl10, int textureId2, int textureId, int viewWidth, int viewHeight, int glWidth, int glHeight, float alpha) {
        for (int i = 0; i < mAlphaVertexFloats.length; ++i) {
            mTempAlphaVertexFloats[i] = mAlphaVertexFloats[i];
            if (i % 2 == 0) {
                mTempAlphaVertexFloats[i] = mTempAlphaVertexFloats[i] * ((float) viewWidth);
            } else {
                mTempAlphaVertexFloats[i] = mTempAlphaVertexFloats[i] * ((float) viewHeight);
            }
        }

        Log.d("HandWriteRender", String.format("mTempAlphaVertexFloats: %s", getFloatArrayString(mTempAlphaVertexFloats)));

        mAlphaVertextPointers.put(mTempAlphaVertexFloats);
        mAlphaVertextPointers.position(0);
        
        final float ratioX = (float) ((viewWidth * 1.0d) / glWidth);
        final float ratioY = (float) ((viewHeight * 1.0d) / glHeight);
        
        for (int i = 0; i < mTexCoordFloats.length; ++i) {
            mTempTexCoordFloats[i] = mTexCoordFloats[i];
            if (i % 2 == 0) {
                mTempTexCoordFloats[i] = mTempTexCoordFloats[i] * ratioX;
            } else {
                mTempTexCoordFloats[i] = mTempTexCoordFloats[i] * ratioY;
            }
        }

        Log.d("HandWriteRender", String.format("mTempTexCoordFloats: %s", getFloatArrayString(mTempTexCoordFloats)));

        mTexCoordPointers.put(mTempTexCoordFloats);
        mTexCoordPointers.position(0);

        gl10.glMatrixMode(GL_MODELVIEW);
        gl10.glLoadIdentity();

        if (textureId2 == GL_TEXTURE0) {
            gl10.glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        } else {
            gl10.glColor4f(alpha, alpha, alpha, alpha);
            gl10.glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        }
        gl10.glBindTexture(GL_TEXTURE_2D, textureId);
        gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, mAlphaVertextPointers);
        gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordPointers);
        gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    /**
     * 生成3个纹理id，给后两个设置多级渐远纹理
     * @param gl10
     * @param context
     */
    public void init(GL10 gl10, Context context) {
        gl10.glGenTextures(3, mGenTextureIds, 0);
        Bitmap bitmap = mBitmap;
        if (bitmap != null) {
            buildMipmapFromBitmap(gl10, bitmap);
        } else {
            buildMipmapFromResource(gl10, context);
        }
    }

    private void buildMipmapFromResource(GL10 gl10, Context context) {
        Bitmap decodeStream = BitmapFactory.decodeStream(context.getResources().openRawResource(resourceId));
        buildMipmap(1, decodeStream, gl10);
        buildMipmap(2, decodeStream, gl10);
        decodeStream.recycle();
    }

    private void buildMipmapFromBitmap(GL10 gl10, Bitmap bitmap) {
        buildMipmap(1, bitmap, gl10);
        buildMipmap(2, bitmap, gl10);
    }

    private void buildMipmap(int index, Bitmap bitmap, GL10 gl10) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(createBitmap).drawBitmap(bitmap, 0.0f, 0.0f, new Paint());
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mGenTextureIds[index]);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        buildMipmap(index, gl10, createBitmap);
        checkGlError(gl10);
        createBitmap.recycle();
    }

    /**
     * 生成多级渐远纹理
     * @param index
     * @param gl10
     * @param bitmap
     */
    private void buildMipmap(int index, GL10 gl10, Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mGenTextureIds[index]);
        int level = 0;
        while (height >= 1 && width >= 1) {
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);

            if (height == 1 || width == 1) {
                break;
            }

            level++;
            height /= 2;
            width /= 2;

            Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            bitmap.recycle();
            bitmap = createScaledBitmap;
        }
    }
}
