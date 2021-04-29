package com.dongjianye.handwrite.base;

import android.opengl.GLES20;

/**
 * Created by Administrator on 2016/7/5.
 * 着色器代码封装
 */
public class ShaderProgram {
  protected final int mProgram;

  protected ShaderProgram(final String vertexShaderCode,
                          final String fragmentShaderCode) {
    mProgram = ShaderHelper.buildProgram(vertexShaderCode, fragmentShaderCode);
  }

  public void useProgram() {
    GLES20.glUseProgram(mProgram);
  }
}
