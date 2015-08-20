package com.example.lineplus.glcanvas;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

import android.content.Context;

import com.example.lineplus.glcanvas.shader.ShaderProgram;

public class LineShaderProgram extends ShaderProgram {
//	private final int uMatrixLocation;
//	private final int uTextureUnitLocation;

	private final int aPosition;
//	private final int aTextureCoordinatesLocation;
	private final int aColor;

	public LineShaderProgram(Context context) {
		super(context, R.raw.line_vertex_shader, R.raw.line_vertex_fragment);

//		uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
//		uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

		aPosition = glGetAttribLocation(program, A_POSITION);
		aColor = glGetAttribLocation(program, A_COLOR);
//		aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
	}

/*	public void setUniforms(float[] matrix, int textureId) {
		glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

		glActiveTexture(GL_TEXTURE0);

		glBindTexture(GL_TEXTURE_2D, textureId);

		glUniform1i(uTextureUnitLocation, 0);
	}*/

	public int getAttrPosition() {
		return aPosition;
	}

	public int getAttrColor() {
		return aColor;
	}


//	public int getTextureCoordinatesAttributeLocation() {
//		return aTextureCoordinatesLocation;
//	}
}
