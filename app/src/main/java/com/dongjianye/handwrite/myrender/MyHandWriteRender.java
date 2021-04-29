package com.dongjianye.handwrite.myrender;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.dongjianye.handwrite.base.HandWriteMotionTask;
import com.dongjianye.handwrite.base.HandWriteTask;
import com.dongjianye.handwrite.base.HandWriteUpTask;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import static android.opengl.GLES10.GL_MODELVIEW;
import static android.opengl.GLES10.GL_MODULATE;
import static android.opengl.GLES10.GL_ONE;
import static android.opengl.GLES10.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES10.GL_PROJECTION;
import static android.opengl.GLES10.GL_TEXTURE_COORD_ARRAY;
import static android.opengl.GLES10.GL_TEXTURE_ENV;
import static android.opengl.GLES10.GL_TEXTURE_ENV_MODE;
import static android.opengl.GLES11Ext.GL_COLOR_ATTACHMENT0_OES;
import static android.opengl.GLES11Ext.GL_DEPTH_ATTACHMENT_OES;
import static android.opengl.GLES11Ext.GL_FRAMEBUFFER_COMPLETE_OES;
import static android.opengl.GLES11Ext.GL_FRAMEBUFFER_OES;
import static android.opengl.GLES11Ext.GL_RENDERBUFFER_OES;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DITHER;
import static android.opengl.GLES20.GL_REPLACE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES32.GL_VERTEX_ARRAY;

/**
 * @author dongjianye on 4/27/21
 */
public class MyHandWriteRender implements GLSurfaceView.Renderer {

    private Bitmap mBitmap;

    private int mWith;  // 纹理的宽度和高度，应该是2的幂次方
    private int mHeight;

    private int mTextureWidth;
    private int mTextureHeight;

    private final float[] mVertexFloats = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
    private final float[] mTexCoordFloats = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};
//    private final float[] mTexCoordFloats = {0.0f, 0.0f, 128.0f, 0.0f, 0.0f, 128.0f, 128.0f, 128.0f};
    private final float[] mAlphaVertexFloats = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};

    private final FloatBuffer mAlphaVertextPointers;
    private final FloatBuffer mVertexPointers; // 顶点坐标
    private final FloatBuffer mTexCoordPointers; // 纹理坐标

    private final float[] mTempAlphaVertexFloats = new float[mAlphaVertexFloats.length];
    private final float[] mTempTexCoordFloats = new float[mTexCoordFloats.length];

    private ConcurrentLinkedQueue<HandWriteTask> mHandWriteTasks;

    private int mFrameBufferId;
    private int mTextureId;

    public MyHandWriteRender() {
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

        mHandWriteTasks = new ConcurrentLinkedQueue<>();
    }
    /**
     * 获取比给定value大于等于的最小的2的幂次方的值
     * @param value
     * @return
     */
    private int getMinTwoPower(int value) {
        int leftShift = 1;
        while (true) {
            int result = 1 << leftShift;
            if (result >= value) {
                return result;
            }
            leftShift++;
        }
    }

    private final int[] mGenTextureIds = new int[1];

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(400, 100, width, height);

        mWith = width;
        mHeight = height;

        mTextureWidth = getMinTwoPower(width);
        mTextureHeight = getMinTwoPower(height);

        // 创建两个纹理和对应的缓冲区
//        mTextureId = genTexture(gl, mTextureWidth, mTextureHeight);
//        mFrameBufferId = createBufferForTexture(gl, mTextureWidth, mTextureHeight, mTextureId);


        // 设置渲染窗口大小
        gl.glViewport(0, 0, width, height);

        // 对投影相关工作
        gl.glMatrixMode(GL_PROJECTION);
        // 重置当前指定的矩阵为单位矩阵
        gl.glLoadIdentity();
        // TODO 创建平行投影
        gl.glOrthof(0.0f, (float) width, (float) height, 0.0f, -1.0f, 1.0f);

        // 对模型视图的相关工作
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glDisable(GL_DITHER);
        gl.glEnable(GL_TEXTURE_2D);
        gl.glEnable(GL_BLEND);
        gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
        gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnableClientState(GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        // 生成纹理id
        gl.glGenTextures(1, mGenTextureIds, 0);

        Bitmap createBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(createBitmap).drawBitmap(mBitmap, 0.0f, 0.0f, new Paint());

        // 绑定纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mGenTextureIds[0]);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        buildMipmap(gl, createBitmap);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕颜色
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        // 将从窗口中清除最后一次所绘制的图形
        gl.glClear(GL_COLOR_BUFFER_BIT);

        GL11ExtensionPack gl11 = (GL11ExtensionPack) gl;
//        gl11.glBindFramebufferOES(GL_FRAMEBUFFER, mFrameBufferId);

        drawTriangleAtSpecialTexture(gl, 200, 200, 40);
        drawTriangleAtSpecialTexture(gl, 400, 200, 40);
        drawTriangleAtSpecialTexture(gl, 600, 200, 40);
        drawTriangleAtSpecialTexture(gl, 100, 200, 40);
        drawTriangleAtSpecialTexture(gl, 300, 200, 200);
        drawTriangleAtSpecialTexture(gl, 0, 200, 40);

//        final long currentTimeMillis = System.currentTimeMillis();
//        boolean hide = false;
//        while (System.currentTimeMillis() - currentTimeMillis <= 10) {
//            HandWriteTask peek = mHandWriteTasks.peek();
//            if (peek == null) {
//                continue;
//            }
//            switch (peek.getTaskType()) {
//                case DRAW_MOTION_EVENT_TASK: {
//                    HandWriteTask poll = mHandWriteTasks.poll();
//                    while (poll != null) {
//                        Log.d("HandWriteRender", "task size is " + mHandWriteTasks.size());
//                        MotionEvent motionEvent = ((HandWriteMotionTask) poll).getMotionEvent();
//                        if (motionEvent != null) {
//                            drawTriangleAtSpecialTexture(gl, motionEvent.getX(), motionEvent.getY(), 40);
//                        }
//                        HandWriteTask top = mHandWriteTasks.peek();
//                        poll = (top == null || top.getTaskType() != HandWriteTaskType.DRAW_MOTION_EVENT_TASK) ? null : mHandWriteTasks.poll();
//                    }
//                    break;
//                }
//                case ERASE_TASK: {
//                    if (mHandWriteTasks.poll() != null) {
////                        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
////                        gl.glClear(GL_COLOR_BUFFER_BIT);
////                        mMipmap.drawTriangleAtSpecialTexture(gl10, 0.0f, 0.0f, 1.0f);
//                    }
//                    break;
//                }
//                case UP_MOTION_EVENT_TASK: {
//                    HandWriteTask poll2 = mHandWriteTasks.poll();
//                    while (poll2 != null) {
//                        MotionEvent motionEvent = ((HandWriteUpTask) poll2).getMotionEvent();
//                        if (motionEvent != null) {
////                            drawMotionEvent(gl10, motionEvent);
//                        }
//                        HandWriteTask peek3 = mHandWriteTasks.peek();
//                        poll2 = (peek3 == null || peek3.getTaskType() != HandWriteTaskType.UP_MOTION_EVENT_TASK) ? null : mHandWriteTasks.poll();
//                    }
////                    if (((double) mStrokeAlpha) > 0.999d) {
////                        hide = false;
////                        continue;
////                    } else {
////                        c(gl10);
////                        hide = true;
////                    }
//                    break;
//                }
//            }
//        }

//        gl11.glBindFramebufferOES(GL_FRAMEBUFFER, 0);
//        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, (float) mWith, (float) mHeight, 0.0f, -1.0f, 1.0f);
//        gl.glMatrixMode(GL_MODELVIEW);
//        gl.glLoadIdentity();

        a(gl, GL_TEXTURE0, mGenTextureIds[0], mWith, mHeight, mTextureWidth, mTextureHeight, 1.0f);
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

//        mAlphaVertextPointers.put(mTempAlphaVertexFloats);
//        mAlphaVertextPointers.position(0);
//
//        final float ratioX = (float) ((viewWidth * 1.0d) / glWidth);
//        final float ratioY = (float) ((viewHeight * 1.0d) / glHeight);
//
//        for (int i = 0; i < mTexCoordFloats.length; ++i) {
//            mTempTexCoordFloats[i] = mTexCoordFloats[i];
//            if (i % 2 == 0) {
//                mTempTexCoordFloats[i] = mTempTexCoordFloats[i] * ratioX;
//            } else {
//                mTempTexCoordFloats[i] = mTempTexCoordFloats[i] * ratioY;
//            }
//        }
//
//        Log.d("HandWriteRender", String.format("mTempTexCoordFloats: %s", getFloatArrayString(mTempTexCoordFloats)));
//
//        mTexCoordPointers.put(mTempTexCoordFloats);
//        mTexCoordPointers.position(0);

//        gl10.glMatrixMode(GL_MODELVIEW);
//        gl10.glLoadIdentity();

        if (textureId2 == GL_TEXTURE0) {
            gl10.glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        } else {
            gl10.glColor4f(alpha, alpha, alpha, alpha);
            gl10.glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        }
//        gl10.glBindTexture(GL_TEXTURE_2D, textureId);
//        gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, mAlphaVertextPointers);
//        gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordPointers);
//        gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void drawTriangleAtSpecialTexture(GL10 gl10, float x, float y, float size) {


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
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mGenTextureIds[0]);
        gl10.glActiveTexture(GL_TEXTURE0);
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

    public void appendMotionEvent(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mHandWriteTasks.add(new HandWriteUpTask(obtain));
        } else {
            mHandWriteTasks.add(new HandWriteMotionTask(obtain));
        }
    }

    /**
     * 为纹理创建缓冲区
     * @param gl10
     * @param width
     * @param height
     * @param textureId
     * @return
     */
    private int createBufferForTexture(GL10 gl10, int width, int height, int textureId) {
        GL11ExtensionPack gl11 = (GL11ExtensionPack) gl10;
        final int[] frameBuffersId = new int[1];
        // 创建帧缓冲区
        gl11.glGenFramebuffersOES(1, frameBuffersId, 0);

        // 绑定帧缓冲区
        gl11.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffersId[0]);

        // 渲染缓冲对象
        int[] renderBuffersId = new int[1];
        gl11.glGenRenderbuffersOES(1, renderBuffersId, 0);

        gl11.glBindRenderbufferOES(GL_RENDERBUFFER_OES, renderBuffersId[0]);
        gl11.glRenderbufferStorageOES(GL_RENDERBUFFER_OES, GL11ExtensionPack.GL_DEPTH_COMPONENT16, width, height);

        gl11.glFramebufferRenderbufferOES(GL_FRAMEBUFFER_OES, GL_DEPTH_ATTACHMENT_OES, GL_RENDERBUFFER_OES, renderBuffersId[0]);
        gl11.glFramebufferTexture2DOES(GL_FRAMEBUFFER_OES, GL_COLOR_ATTACHMENT0_OES, GL_TEXTURE_2D, textureId, 0);
        final int status = gl11.glCheckFramebufferStatusOES(GL_FRAMEBUFFER_OES);
        if (status == GL_FRAMEBUFFER_COMPLETE_OES) {
            gl11.glBindFramebufferOES(GL_FRAMEBUFFER_OES, 0);
            return frameBuffersId[0];
        }
        throw new RuntimeException("Framebuffer is not complete: " + Integer.toHexString(status));
    }

    private void deleteFrame(GL10 gl10, int i) {
        int[] iArr = {i};
        ((GL11ExtensionPack) gl10).glDeleteFramebuffersOES(iArr.length, iArr, 0);
    }

    /**
     * 生成纹理，并且返回纹理id
     * @param gl10
     * @param width
     * @param height
     * @return
     */
    private int genTexture(GL10 gl10, int width, int height) {
        final int[] textureId = new int[1];
        gl10.glGenTextures(1, textureId, 0);
        final int id = textureId[0];
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, id); // 绑定纹理id
        // 生成纹理
        gl10.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, width, height, 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return id;
    }

    private void deleteTexture(GL10 gl10, int i) {
        int[] iArr = {i};
        gl10.glDeleteTextures(iArr.length, iArr, 0);
    }

    private void buildMipmap(GL10 gl10, Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mGenTextureIds[0]);
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