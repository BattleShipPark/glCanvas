//uniform mat4 u_Matrix;

attribute vec4 a_Position;
attribute vec4 a_Color;

uniform mat4 u_ProjMatrix;
uniform mat4 u_CameraMatrix;

varying vec4 v_Color;

void main() {
	v_Color = a_Color;

	gl_Position = u_ProjMatrix * u_CameraMatrix * a_Position;
//	gl_Position = u_Matrix * a_Position;
//	gl_PointSize = 10.0;
}
