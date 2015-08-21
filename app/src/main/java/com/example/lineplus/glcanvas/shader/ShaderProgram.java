package com.example.lineplus.glcanvas.shader;

import static android.opengl.GLES20.glUseProgram;

import android.content.Context;

public class ShaderProgram {
	protected static final String U_PROJ_MATRIX = "u_ProjMatrix";
	protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

	protected static final String A_POSITION = "a_Position";
	protected static final String A_COLOR = "a_Color";
	protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

	protected final int program;

	protected ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
		program = ShaderHelper.buildProgram(ShaderHelper.readTextFileFromResource(context, vertexShaderResourceId), ShaderHelper.readTextFileFromResource(context, fragmentShaderResourceId));
	}

	public void useProgram() {
		glUseProgram(program);
	}
}
