package com.dongjianye.handwrite.doublebuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.dongjianye.handwrite.base.HandWriteMotionTask;
import com.dongjianye.handwrite.base.HandWriteTask;
import com.dongjianye.handwrite.R;
import com.dongjianye.handwrite.base.HandWriteTaskType;
import com.dongjianye.handwrite.base.HandWriteUpTask;
import com.dongjianye.handwrite.base.TextureHelper;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.glBlendFunc;
import static android.opengl.GLES10.glEnable;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;

/**
 * @author dongjianye on 4/28/21
 *
 * 百度用的glVertexPointer其实已经过时了，现在推荐用shader的方式，因此还是用这个吧
 *
 * 不记录顶点，但是要保留顶点滑动轨迹，只能用个双缓存了
 */
public class ModernRender implements GLSurfaceView.Renderer {
    private final Context mContext;

    private HandWriteSpot mHandWriteSpot;
    private ScreenFrameBuffer mScreenShader;

    private HandWriteSpotShaderProgram mShaderProgram;
    private int mTextureId;

    private final ConcurrentLinkedQueue<HandWriteTask> mHandWriteTasks;



    public ModernRender(Context context) {
        mContext = context;
        mHandWriteTasks = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        mShaderProgram = new HandWriteSpotShaderProgram();
        mTextureId = TextureHelper.loadTextureFromBitmap(mContext, R.raw.brush3);
        mHandWriteSpot = new HandWriteSpot();
        mScreenShader = new ScreenFrameBuffer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width,height);
        mHandWriteSpot.setSize(width, height);

        mScreenShader.createFrameBuffer(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        gl.glMatrixMode(GL_MODELVIEW);

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, mScreenShader.getFrameBufferId());

        final long currentTimeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTimeMillis <= 10) {
            HandWriteTask peek = mHandWriteTasks.peek();
            if (peek == null) {
                continue;
            }
            switch (peek.getTaskType()) {
                case DRAW_MOTION_EVENT_TASK: {
                    HandWriteTask poll = mHandWriteTasks.poll();
                    while (poll != null) {
                        drawPoint(((HandWriteMotionTask)poll).getMotionEvent().getX(), ((HandWriteMotionTask)poll).getMotionEvent().getY(), 40);
                        poll = mHandWriteTasks.poll();
                    }
                    break;
                }
                case ERASE_TASK: {
                    if (mHandWriteTasks.poll() != null) {

                    }
                    break;
                }
                case UP_MOTION_EVENT_TASK: {
                    HandWriteTask poll2 = mHandWriteTasks.poll();
                    while (poll2 != null) {
                        MotionEvent motionEvent = ((HandWriteUpTask) poll2).getMotionEvent();
                        if (motionEvent != null) {
//                            drawMotionEvent(gl10, motionEvent);
                        }
                        HandWriteTask peek3 = mHandWriteTasks.peek();
                        poll2 = (peek3 == null || peek3.getTaskType() != HandWriteTaskType.UP_MOTION_EVENT_TASK) ? null : mHandWriteTasks.poll();
                    }

                    break;
                }
            }
        }

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);

//        gl.glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
        mScreenShader.draw();
    }

    private void drawPoint(final float x, final float y, final float size) {
        mShaderProgram.useProgram();
        mShaderProgram.setUniforms(mTextureId);
        mHandWriteSpot.updateVertexPosition(x, y, size);
        mHandWriteSpot.bindData(mShaderProgram);
        mHandWriteSpot.draw();
    }

    public void appendMotionEvent(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mHandWriteTasks.add(new HandWriteUpTask(obtain));
        } else {
            mHandWriteTasks.add(new HandWriteMotionTask(obtain));
        }
    }
}