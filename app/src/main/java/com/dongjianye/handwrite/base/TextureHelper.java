package com.dongjianye.handwrite.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_DEPTH_ATTACHMENT;
import static android.opengl.GLES20.GL_DEPTH_COMPONENT16;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_RENDERBUFFER;
import static android.opengl.GLES20.GL_TEXTURE_2D;

/**
 * Created by Administrator on 2016/7/5.
 */
public class TextureHelper {

  private final static String TAG = "TextureHelper";

  public static int loadTextureFromBitmap(Context context, int resourceId){
    final int[] textureObjectIds = new int[1];
    GLES20.glGenTextures(1,textureObjectIds,0);
    if(textureObjectIds[0] == 0 ){
      if(LoggerConfig.ON){
        Log.w(TAG,"Could not generate a new OpenGL texture object.");
      }
      return 0;
    }
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inScaled = false;

    final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resourceId,options);

    if(bitmap==null){
      if(LoggerConfig.ON){
        Log.w(TAG,"Resource ID "+resourceId+" could not be decoded");
      }
      GLES20.glDeleteTextures(1,textureObjectIds,0);
      return 0;
    }
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureObjectIds[0]);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

    bitmap.recycle();

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

    return textureObjectIds[0];
  }

  /**
   * 生成空纹理ID，width和height必须为2的幂次方
   * @param width
   * @param height
   * @return
   */
  public static int genTexture(int width, int height) {
    final int[] textureId = new int[1];
    GLES20.glGenTextures(1, textureId, 0);
    final int id = textureId[0];
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id); // 绑定纹理id
    // 生成纹理
    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    return id;
  }

  public static int createFrameBufferForTexture(int width, int height, int textureId) {
      final int[] frameBuffersId = new int[1];
      // 创建帧缓冲区
      GLES20.glGenFramebuffers(1, frameBuffersId, 0);
  
      // 绑定帧缓冲区
      GLES20.glBindFramebuffer(GL_FRAMEBUFFER, frameBuffersId[0]);
  
      // 渲染缓冲对象
      int[] renderBuffersId = new int[1];
      GLES20.glGenRenderbuffers(1, renderBuffersId, 0);
      GLES20.glBindRenderbuffer(GL_RENDERBUFFER, renderBuffersId[0]);

      GLES20.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
      GLES20.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffersId[0]);
      GLES20.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
      
      final int status = GLES20.glCheckFramebufferStatus(GL_FRAMEBUFFER);
      if (status == GL_FRAMEBUFFER_COMPLETE) {
          GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
          return frameBuffersId[0];
      }
      throw new RuntimeException("Framebuffer is not complete: " + Integer.toHexString(status));
    }
}
