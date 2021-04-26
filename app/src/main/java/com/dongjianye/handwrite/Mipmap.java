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


public class Mipmap {
    private float[] mVertexFloats = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private float[] mTexCoordFloats = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private float[] mUnkownVertexFloats = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    private FloatBuffer mUnkownVertextPointers;
    private FloatBuffer mVertexPointers; // 顶点坐标
    private FloatBuffer mTexCoordPointers; // 纹理坐标

    private int[] mGenTextureIds = new int[3];
    private float[] mTempUnkownVertexFloats = new float[this.mUnkownVertexFloats.length];
    private float[] mTempTexCoordFloats = new float[this.mTexCoordFloats.length];
    private Bitmap mBitmap;
    private int resourceId = 1;

    public Mipmap() {
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(this.mUnkownVertexFloats.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.mUnkownVertextPointers = allocateDirect.asFloatBuffer();
        this.mUnkownVertextPointers.put(this.mUnkownVertexFloats);
        this.mUnkownVertextPointers.position(0);

        ByteBuffer allocateDirect2 = ByteBuffer.allocateDirect(this.mVertexFloats.length * 4);
        allocateDirect2.order(ByteOrder.nativeOrder());
        this.mVertexPointers = allocateDirect2.asFloatBuffer();
        this.mVertexPointers.put(this.mVertexFloats);
        this.mVertexPointers.position(0);

        ByteBuffer allocateDirect3 = ByteBuffer.allocateDirect(this.mTexCoordFloats.length * 4);
        allocateDirect3.order(ByteOrder.nativeOrder());
        this.mTexCoordPointers = allocateDirect3.asFloatBuffer();
        this.mTexCoordPointers.put(this.mTexCoordFloats);
        this.mTexCoordPointers.position(0);
    }

    public void setResource(int i) {
        this.resourceId = i;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
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
            mTempUnkownVertexFloats[i] = mVertexFloats[i] * size;
            if (i % 2 == 0) {
                mTempUnkownVertexFloats[i] = mTempUnkownVertexFloats[i] + (x - (size / 2.0f));
            } else {
                mTempUnkownVertexFloats[i] = mTempUnkownVertexFloats[i] + (y - (size / 2.0f));
            }
        }

        this.mVertexPointers.put(this.mTempUnkownVertexFloats);
        this.mVertexPointers.position(0);
        this.mTexCoordPointers.put(this.mTexCoordFloats);
        this.mTexCoordPointers.position(0);
        gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, this.mVertexPointers);
        gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, this.mTexCoordPointers);
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, this.mGenTextureIds[index]);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, 9987.0f);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, 9729.0f);
        gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void a(GL10 gl10, int i, int i2, int i3, int i4, int i5, int i6, float f) {
        int i7 = 0;
        while (true) {
            float[] fArr = this.mUnkownVertexFloats;
            if (i7 >= fArr.length) {
                break;
            }
            float[] fArr2 = this.mTempUnkownVertexFloats;
            fArr2[i7] = fArr[i7];
            if (i7 % 2 == 0) {
                fArr2[i7] = fArr2[i7] * ((float) i3);
            } else {
                fArr2[i7] = fArr2[i7] * ((float) i4);
            }
            i7++;
        }
        this.mUnkownVertextPointers.put(this.mTempUnkownVertexFloats);
        this.mUnkownVertextPointers.position(0);
        double d = (double) i3;
        Double.isNaN(d);
        double d2 = (double) i5;
        Double.isNaN(d2);
        float f2 = (float) ((d * 1.0d) / d2);
        double d3 = (double) i4;
        Double.isNaN(d3);
        double d4 = (double) i6;
        Double.isNaN(d4);
        float f3 = (float) ((d3 * 1.0d) / d4);
        int i8 = 0;
        while (true) {
            float[] fArr3 = this.mTexCoordFloats;
            if (i8 >= fArr3.length) {
                break;
            }
            float[] fArr4 = this.mTempTexCoordFloats;
            fArr4[i8] = fArr3[i8];
            if (i8 % 2 == 0) {
                fArr4[i8] = fArr4[i8] * f2;
            } else {
                fArr4[i8] = fArr4[i8] * f3;
            }
            i8++;
        }
        this.mTexCoordPointers.put(this.mTempTexCoordFloats);
        this.mTexCoordPointers.position(0);
        gl10.glMatrixMode(5888);
        gl10.glLoadIdentity();
        if (i == 33984) {
            gl10.glTexEnvx(8960, 8704, 7681);
        } else {
            gl10.glColor4f(f, f, f, f);
            gl10.glTexEnvx(8960, 8704, 8448);
        }
        gl10.glBindTexture(3553, i2);
        gl10.glVertexPointer(2, 5126, 0, this.mUnkownVertextPointers);
        gl10.glTexCoordPointer(2, 5126, 0, this.mTexCoordPointers);
        gl10.glDrawArrays(5, 0, 4);
    }

    /**
     * 生成3个纹理id，给后两个设置多级渐远纹理
     * @param gl10
     * @param context
     */
    public void init(GL10 gl10, Context context) {
        gl10.glGenTextures(3, this.mGenTextureIds, 0);
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            buildMipmapFromBitmap(gl10, bitmap);
        } else {
            buildMipmapFromResource(gl10, context);
        }
    }

    private void buildMipmapFromResource(GL10 gl10, Context context) {
        Bitmap decodeStream = BitmapFactory.decodeStream(context.getResources().openRawResource(this.resourceId));
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
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, this.mGenTextureIds[index]);
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
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, this.mGenTextureIds[index]);
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
