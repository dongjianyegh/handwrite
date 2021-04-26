package com.dongjianye.handwrite;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import static android.opengl.GLES10.GL_MODELVIEW;
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
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES32.GL_VERTEX_ARRAY;


public class HandWriteRender implements GLSurfaceView.Renderer {
    private float alpha;
    private double iGA;
    private int iGB;
    private double iGC;
    private double mSize;
    private double mDensity;
    private int mSkipPointRate;
    private Rect iGG;
    private jny iGH;
    private boolean iGI;
    private boolean mGlInited;
    private int mTextureId;
    private int mFrameBufferId;
    private int mUnkownTextureId;
    private int mUnkownFrameBufferId;
    private int mWith;  // 纹理的宽度和高度，应该是2的幂次方
    private int mHeight;
    private jnz iGs;
    private Mipmap mMipmap;
    private HandWriteView mHandWriteView;
    private ConcurrentLinkedQueue<HandWriteTask> mHandWriteTasks;
    private MotionEvent mCurMotionEvent;
    private jnz.a mDownPosition;
    private double iGy;
    private double iGz;

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

    public HandWriteRender(HandWriteView handWriteView) {
        mGlInited = false;
        iGs = new jnz();
        mMipmap = null;
        mHandWriteView = null;
        mHandWriteTasks = new ConcurrentLinkedQueue<>();
        mCurMotionEvent = null;
        mDownPosition = null;
        iGy = 0.0d;
        iGz = 0.0d;
        iGA = 0.0d;
        mSkipPointRate = 1;
        alpha = 0.8f;
        iGH = new jny();
        iGI = true;
        mMipmap = new Mipmap();
        mHandWriteView = handWriteView;
        reset();
    }

    public void setDensity(double d) {
        mDensity = d;
    }

    public void setSize(double d) {
        mSize = d;
    }

    public void A(Rect rect) {
        iGG = rect;
    }

    public void setSkipPointRate(int i) {
        mSkipPointRate = i;
    }

    private boolean aD(MotionEvent motionEvent) {
        return iGG == null || (motionEvent.getX() > ((float) iGG.left) && motionEvent.getX() < ((float) iGG.right) && motionEvent.getY() > ((float) iGG.top) && motionEvent.getY() < ((float) iGG.bottom));
    }

    private boolean ah(float f, float f2) {
        Rect rect = iGG;
        return rect == null || (f > ((float) rect.left) && f < ((float) iGG.right) && f2 > ((float) iGG.top) && f2 < ((float) iGG.bottom));
    }

    public void reset() {
        iGH.reset();
        mHandWriteTasks.add(HandWriteEraseTask.getEraseTask());
    }

    public void clear() {
        mHandWriteTasks.clear();
        reset();
    }

    public void appendMotionEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            iGH.reset();
        }
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mHandWriteTasks.add(new HandWriteUpTask(obtain));
        } else {
            mHandWriteTasks.add(new HandWriteMotionTask(obtain));
        }
    }

    public void clearTasks() {
        mHandWriteTasks.clear();
    }

    public void onDrawFrame(GL10 gl10) {
        int i;

        iGH.start();
        float f = 0.0f;
        // 清除屏幕颜色
        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // 将从窗口中清除最后一次所绘制的图形
        gl10.glClear(GL_COLOR_BUFFER_BIT);
        GL11ExtensionPack gl11 = (GL11ExtensionPack) gl10;

        gl11.glBindFramebufferOES(GL_FRAMEBUFFER, mFrameBufferId);

        gl10.glMatrixMode(GL_PROJECTION);
        gl10.glLoadIdentity();
        createOrthof(gl10);

        gl10.glMatrixMode(GL_MODELVIEW);

        final long currentTimeMillis = System.currentTimeMillis();
        boolean z = false;
        while (System.currentTimeMillis() - currentTimeMillis <= 10) {
            HandWriteTask peek = mHandWriteTasks.peek();
            if (peek == null) {
                continue;
            }
            switch (peek.getTaskType()) {
                case DRAW_MOTION_EVENT_TASK: {
                    HandWriteTask poll = mHandWriteTasks.poll();
                    while (poll != null) {
                        MotionEvent motionEvent = ((HandWriteMotionTask) poll).getMotionEvent();
                        if (motionEvent != null) {
                            drawMotionEvent(gl10, motionEvent);
                        }
                        HandWriteTask top = mHandWriteTasks.peek();
                        poll = (top == null || top.getTaskType() != HandWriteTaskType.DRAW_MOTION_EVENT_TASK) ? null : mHandWriteTasks.poll();
                    }
                    break;
                }
                case ERASE_TASK: {
                    if (mHandWriteTasks.poll() != null) {
                        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                        gl10.glClear(GL_COLOR_BUFFER_BIT);
                        mMipmap.drawTriangle(gl10, 0.0f, 0.0f, 1.0f, 2);
                    }
                    break;
                }
                case UP_MOTION_EVENT_TASK: {
                    HandWriteTask poll2 = mHandWriteTasks.poll();
                    while (poll2 != null) {
                        MotionEvent dUk2 = ((HandWriteUpTask) poll2).getMotionEvent();
                        if (dUk2 != null) {
                            drawMotionEvent(gl10, dUk2);
                        }
                        HandWriteTask peek3 = mHandWriteTasks.peek();
                        poll2 = (peek3 == null || peek3.getTaskType() != HandWriteTaskType.UP_MOTION_EVENT_TASK) ? null : mHandWriteTasks.poll();
                    }
                    if (((double) alpha) > 0.999d) {
                        z = false;
                        continue;
                    } else {
                        c(gl10);
                        z = true;
                    }
                    break;
                }
            }
        }
        gl11.glBindFramebufferOES(36160, 0);
        gl10.glMatrixMode(GL_PROJECTION);
        gl10.glLoadIdentity();
        createOrthof(gl10);
        gl10.glMatrixMode(GL_MODELVIEW);
        gl10.glLoadIdentity();
        if (!z) {
            mMipmap.a(gl10, 33984, mTextureId, mHandWriteView.getWidth(), mHandWriteView.getHeight(), mWith, mHeight, 1.0f);
        } else {
            mMipmap.a(gl10, 33984, mUnkownTextureId, mHandWriteView.getWidth(), mHandWriteView.getHeight(), mWith, mHeight, 1.0f);
            int i2 = mFrameBufferId;
            mFrameBufferId = mUnkownFrameBufferId;
            mUnkownFrameBufferId = i2;
            int i3 = mTextureId;
            mTextureId = mUnkownTextureId;
            mUnkownTextureId = i3;
        }
        iGH.end();
    }

    private void createOrthof(GL10 gl10) {
        // TODO 创建平行投影
        gl10.glOrthof(0.0f, (float) mHandWriteView.getWidth(), (float) mHandWriteView.getHeight(), 0.0f, -1.0f, 1.0f);
    }

    private void c(GL10 gl10) {
        GL11ExtensionPack gL11ExtensionPack = (GL11ExtensionPack) gl10;
        gL11ExtensionPack.glBindFramebufferOES(36160, mUnkownFrameBufferId);
        if (iGI) {
            gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl10.glClear(GL_COLOR_BUFFER_BIT);
            iGI = false;
        }
        mMipmap.a(gl10, mUnkownTextureId, mTextureId, mHandWriteView.getWidth(), mHandWriteView.getHeight(), mWith, mHeight, alpha);
        gL11ExtensionPack.glBindFramebufferOES(36160, mFrameBufferId);
        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl10.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void drawMotionEvent(GL10 gl10, MotionEvent motionEvent) {
        MotionEvent motionEvent2;
        double d;
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN || mDownPosition == null) {
            iGC = mSize;
            iGz = 0.0d;
            iGA = 0.0d;
            mDownPosition = new jnz.a();
            mDownPosition.init((double) motionEvent.getX(), (double) motionEvent.getY(), mSize);
            iGB = 0;
            motionEvent2 = motionEvent;
            if (aD(motionEvent2)) {
                drawTriangle(gl10, (double) motionEvent.getX(), (double) motionEvent.getY(), mSize);
            }
        } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE) {
            double downX = mDownPosition.x;
            double downY = mDownPosition.y;
            if (mDensity <= 0.0d) {
                Log.e(HandWriteView.class.getName(), "density=" + mDensity + ", you should call IHandWriter.init(density,width,height) first!");
            }
            final double moveX = motionEvent.getX();
            final double moveY = motionEvent.getY();
            final double hypot = Math.hypot((moveX - downX) / mDensity, (moveY - downY) / mDensity);

            int i = (((int) hypot) / 10) + 1;
            final double eventTime = (double) (motionEvent.getEventTime() - mCurMotionEvent.getEventTime());

            double d5 = hypot / eventTime;
            double a = jnx.a(mDownPosition.x, mDownPosition.y, moveX, moveY, mSize, iGC, iGz);
            if (iGB < 2) {
                d = d5;
                iGy = d;
                iGs.a(downX, downY, iGC, motionEvent.getX(),motionEvent.getY(), a);
            } else {
                d = d5;
                iGs.b((double) motionEvent.getX(), (double) motionEvent.getY(), a);
            }
            iGy = (iGy + d) / 2.0d;
            iGC = a;
            iGz = hypot;
            iGA += iGz;
            a(gl10, i);
        }

        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
            double d6 = mDownPosition.x;
            double d7 = mDownPosition.y;
            if (iGA < 0.1d) {
                drawTriangle(gl10, d6, d7, mSize);
            } else {
                double x2 = (double) motionEvent.getX();
                double y2 = (double) motionEvent.getY();
                Double.isNaN(x2);
                double d8 = x2 - d6;
                double d9 = mDensity;
                Double.isNaN(y2);
                int hypot2 = (((int) (Math.hypot(d8 / d9, (y2 - d7) / d9) * mDensity)) / 10) + 1;
                iGs.b(x2, y2, 0.0d);
                a(gl10, hypot2);
                iGs.end();
                a(gl10, hypot2);
            }
            mDownPosition = null;
            iGy = 0.0d;
        }
        mCurMotionEvent = motionEvent;
        iGB++;
    }

    private void a(GL10 gl10, int i) {
        int min = Math.min(10, i);
        iGs.a(mDownPosition, 0.0d);
        double d = mDownPosition.x;
        double d2 = mDownPosition.y;
        double d3 = mDownPosition.gi;
        double d4 = (double) min;
        Double.isNaN(d4);
        double d5 = 1.0d / d4;
        double d6 = d3;
        double d7 = d5;
        double d8 = d2;
        double d9 = d;
        for (double d10 = 1.0d; d7 < d10; d10 = 1.0d) {
            iGs.a(mDownPosition, d7);
            a(gl10, d9, d8, d6, mDownPosition.x, mDownPosition.y, mDownPosition.gi);
            d9 = mDownPosition.x;
            d8 = mDownPosition.y;
            d6 = mDownPosition.gi;
            d7 += d5;
        }
        iGs.a(mDownPosition, 1.0d);
        a(gl10, d9, d8, d6, mDownPosition.x, mDownPosition.y, mDownPosition.gi);
    }

    private void a(GL10 gl10, double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = d4 - d;
        double d8 = d5 - d2;
        double hypot = Math.hypot(d7, d8);
        float f = (float) (d7 / hypot);
        float f2 = (float) (d8 / hypot);
        float f3 = (float) ((d6 - d3) / hypot);
        float f4 = (float) d;
        float f5 = (float) d2;
        float f6 = (float) d3;
        for (int i = 0; ((double) i) < hypot; i++) {
            if (ah(f4, f5) && i % (mSkipPointRate + 1) == 0) {
                mMipmap.drawTriangle(gl10, f4, f5, f6, 1);
            }
            f4 += f;
            f5 += f2;
            f6 += f3;
        }
    }

    private void drawTriangle(GL10 gl10, double x, double y, double size) {
        gl10.glMatrixMode(GL_MODELVIEW);
        gl10.glLoadIdentity();
        mMipmap.drawTriangle(gl10, (float) x, (float) y, (float) size, 2);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        initGl(gl10, width, height);
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

    private void initGl(GL10 gl10, int viewWidth, int viewHeight) {
        if (mGlInited) {
            destroy(gl10);
        }
        mGlInited = true;
        mWith = getMinTwoPower(viewWidth);
        mHeight = getMinTwoPower(viewHeight);
        
        // 创建两个纹理和对应的缓冲区
        mTextureId = genTexture(gl10, mWith, mHeight);
        mFrameBufferId = createBufferForTexture(gl10, mWith, mHeight, mTextureId);

        // 创建两个纹理和对应的缓冲区
        mUnkownTextureId = genTexture(gl10, mWith, mHeight);
        mUnkownFrameBufferId = createBufferForTexture(gl10, mWith, mHeight, mUnkownTextureId);

        // 设置渲染窗口大小
        gl10.glViewport(0, 0, viewWidth, viewHeight);

        // 对投影相关工作
        gl10.glMatrixMode(GL_PROJECTION);
        // 重置当前指定的矩阵为单位矩阵
        gl10.glLoadIdentity();
        // TODO 创建平行投影
        gl10.glOrthof(0.0f, (float) viewWidth, (float) viewHeight, 0.0f, -1.0f, 1.0f);

        // 对模型视图的相关工作
        gl10.glMatrixMode(GL_MODELVIEW);
        gl10.glDisable(GL_DITHER);
        gl10.glEnable(GL_TEXTURE_2D);
        gl10.glEnable(GL_BLEND);
        gl10.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
        gl10.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        gl10.glEnableClientState(GL_VERTEX_ARRAY);
        gl10.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        reset();
    }

    private void destroy(GL10 gl10) {
        deleteFrame(gl10, mFrameBufferId);
        deleteTexture(gl10, mTextureId);
        mGlInited = false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        if (!gl10.glGetString(7939).contains("GL_OES_framebuffer_object")) {
            Log.e(HandWriteView.class.getName(), "Framebuffer is not supported!", null);
            return;
        }
        iGI = true;
        mMipmap.init(gl10, mHandWriteView.getContext());
        initGl(gl10, mHandWriteView.getWidth(), mHandWriteView.getHeight());
    }

    public void setResource(int i) {
        mMipmap.setResource(i);
    }

    public void setBitmap(Bitmap bitmap) {
        mMipmap.setBitmap(bitmap);
    }

    public void setStrokeAlpha(float f) {
        alpha = f;
    }
}
