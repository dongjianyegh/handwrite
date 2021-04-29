package com.dongjianye.handwrite.doublebuffer;

import android.opengl.GLES20;

import com.dongjianye.handwrite.base.ShaderProgram;


/**
 * Created by Administrator on 2016/7/5.
 */
public class HandWriteSpotShaderProgram extends ShaderProgram {

  private final static String VERTEX_SHADER_CODE =
          "attribute vec4 a_Position;\n" +
          "attribute vec2 a_TextureCoordinates;\n" +
          "\n" +
          "varying vec2 v_TextureCoordinates;\n" +
          "\n" +
          "void main()\n" +
          "{\n" +
          "  v_TextureCoordinates = a_TextureCoordinates;\n" +
          "  gl_Position = a_Position;\n" +
          "}\n";

  private final static String FRAGMENT_SHADER_CODE =
          "precision mediump float;\n" +
          "\n" +
          "uniform sampler2D u_TextureUnit;\n" +
          "varying vec2 v_TextureCoordinates;\n" +
          "\n" +
          "void main()\n" +
          "{\n" +
          "  gl_FragColor = texture2D(u_TextureUnit,v_TextureCoordinates);\n" +
          "}";

  protected static final String U_TEXTURE_UNIT =  "u_TextureUnit";

  protected static final String A_POSITION =      "a_Position";
  protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

  private final int uTextureUnitLocation;

  private final int aPositionLocation;
  private final int aTextureCoordnatesLocation;

  public HandWriteSpotShaderProgram() {
    super(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);

    uTextureUnitLocation = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
    aPositionLocation = GLES20.glGetAttribLocation(mProgram,  A_POSITION);
    aTextureCoordnatesLocation = GLES20.glGetAttribLocation(mProgram, A_TEXTURE_COORDINATES);
  }

  @Override
  public void useProgram() {
    super.useProgram();
  }

  public void setUniforms(int textureId) {
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    GLES20.glUniform1i(uTextureUnitLocation, 0);
  }

  public int getPositionAttributeLocation() {
    return aPositionLocation;
  }

  public int getTextureCoordinatesAttributeLocation() {
    return aTextureCoordnatesLocation;
  }
}
