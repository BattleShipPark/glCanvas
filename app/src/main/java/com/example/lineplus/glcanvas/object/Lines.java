package com.example.lineplus.glcanvas.object;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import android.opengl.Matrix;
import android.util.Log;

import com.example.lineplus.glcanvas.LineShaderProgram;

public class Lines {
	private static final int POSITION_COMPONENT_COUNT = 3;
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final float LINE_WIDTH = 0.01f;

	private List<Line> lines = new Vector<>();
	//	private List<Float> vertices = new ArrayList<>();
	private boolean pointsUpdated;

	private FloatBuffer vertexData = ByteBuffer.allocate(0).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer colorData = ByteBuffer.allocate(0).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private float[] vertex;
	private float[] color;

	/*	public List<Float> getVertices() {
			return vertices;
		}
	
		public void setVertices(List<Float> vertices) {
			this.vertices = vertices;
		}*/
	public Lines() {
		vertex = new float[]{
			-0.5f, 0.5f, 0,
			0.5f, 0.5f, 0,
			0.5f, -0.5f, 0,
			-0.5f, -0.5f, 0
		};
		color = new float[]{
			1, 0, 0,
			0, 1, 0,
			0, 0, 1,
			1, 1, 1
		};
	}

	public void addStartPoint(float x, float y, long eventTime) {
		lines.add(new Line(x, y));
		/*		Line line = new Line(100, 200);
				line.add(200,400);
				line.add(400,300);
				line.add(800,800);
		
				lines.add(line);*/
	}

	public void addDragPoint(float x, float y, long eventTime) {
		Line line = lines.get(lines.size() - 1);
		line.add(x, y);
	}

	public void addEndPoint(float x, float y, long eventTime) {
	}

	public void draw(LineShaderProgram program) {
		program.useProgram();

		for (Line line : lines) {
			if (pointsUpdated) {
				if (vertexData.capacity() < line.points.size() * POSITION_COMPONENT_COUNT * 2) {
					vertexData = ByteBuffer.allocateDirect(line.points.size() * POSITION_COMPONENT_COUNT * 2
						* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
				}
				vertexData.clear();
				vertexData.put(line.pointsPositionToArray());
				vertexData.limit(line.points.size() * POSITION_COMPONENT_COUNT * 2);

				if (colorData.capacity() < line.points.size() * COLOR_COMPONENT_COUNT * 2) {
					colorData = ByteBuffer.allocateDirect(line.points.size()
						* COLOR_COMPONENT_COUNT * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
				}
				colorData.clear();
				colorData.put(line.pointsColorToArray());
				colorData.limit(line.points.size() * COLOR_COMPONENT_COUNT * 2);
			}

			vertexData.position(0);
			glVertexAttribPointer(program.getAttrPosition(), POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
			glEnableVertexAttribArray(program.getAttrPosition());

			colorData.position(0);
			glVertexAttribPointer(program.getAttrColor(), COLOR_COMPONENT_COUNT, GL_FLOAT, false, 0, colorData);
			glEnableVertexAttribArray(program.getAttrColor());

//			long ts = System.currentTimeMillis();
//			Log.w("", String.format("%d, %d", ts, line.points.size()));
//			for (int index = 0; index < line.points.size();) {
//				Log.w("", String.format("%d, %f, %f", ts, vertexData.get(index++), vertexData.get(index++)));
//			}
			glDrawArrays(GL_TRIANGLE_STRIP, 0, line.points.size() * 2);
		}

		pointsUpdated = false;
/*		vertexData = ByteBuffer.allocateDirect(4 * POSITION_COMPONENT_COUNT
			* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexData.clear();
		vertexData.put(vertex);

		colorData = ByteBuffer.allocateDirect(4 * COLOR_COMPONENT_COUNT * 2
			* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		colorData.clear();
		colorData.put(color);

		vertexData.position(0);
		glVertexAttribPointer(program.getAttrPosition(), POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
		glEnableVertexAttribArray(program.getAttrPosition());

		colorData.position(0);
		glVertexAttribPointer(program.getAttrColor(), COLOR_COMPONENT_COUNT, GL_FLOAT, false, 0, colorData);
		glEnableVertexAttribArray(program.getAttrColor());

		glDrawArrays(GL_LINE_STRIP, 0, 4);*/
	}

	public void setPointsUpdated(boolean pointsUpdated) {
		this.pointsUpdated = pointsUpdated;
	}

	public boolean isPointsUpdated() {
		return pointsUpdated;
	}

	/**
	 *
	 * @param matrix 4x4
	 */
	public void setMatrix(float[] values) {
				for (Line line : lines) {
					for (Point3F point : line.points) {
						//				float[] vec = point.toArray();
						//				Matrix.multiplyMV(vec, 0, matrix, 0, vec, 0);
						//				point.set(vec);
		
						float[] m = new float[16];
						Matrix.setRotateM(m, 0, values[0], 0, 0, 1);
						Matrix.rotateM(m, 0, values[1], 1, 0, 0);
						Matrix.rotateM(m, 0, values[2], 0, 1, 0);
		
						float[] vec4 = new float[4];
						Matrix.multiplyMV(vec4, 0, m, 0, point.toArray(), 0);
						point.set(vec4);
					}
				}

		setPointsUpdated(true);

/*		for (int index = 0; index < 4; index++) {
			//				float[] vec = point.toArray();
			//				Matrix.multiplyMV(vec, 0, matrix, 0, vec, 0);
			//				point.set(vec);

			float[] v = new float[4];
			v[0] = vertex[index * 3];
			v[1] = vertex[index * 3 + 1];
			v[2] = vertex[index * 3 + 2];
			v[3] = 1;
			Log.w("set1", String.format("%f,%f,%f - %f,%f,%f", values[0], values[1], values[2], v[0], v[1], v[2]));

			float[] m = new float[16];
			Matrix.setRotateM(m, 0, values[0], 0, 0, 1);
			Matrix.rotateM(m, 0, values[1], 1, 0, 0);
			Matrix.rotateM(m, 0, values[2], 0, 1, 0);

			float[] vec4 = new float[4];
			Matrix.multiplyMV(vec4, 0, m, 0, v, 0);
			vertex[index * 3] = vec4[0];
			vertex[index * 3 + 1] = vec4[1];
			vertex[index * 3 + 2] = vec4[2];
			Log.w("set2", String.format("%f,%f,%f", vec4[0], vec4[1], vec4[2]));
		}*/
	}

	class Line {
		final List<Point3F> points = new Vector<>();

		/*		Line(float startX, float startY, float endX, float endY) {
					add(startX, startY);
					add(endX, endY);
				}*/

		Line(float x, float y) {
			add(x, y);
		}

		void add(float x, float y) {
			Point3F pointF = new Point3F(convertX(x), convertY(y), 0);
			points.add(pointF);

			//			Log.w("add", String.format("%f,%f - %f,%f", x, y, pointF.x, pointF.y));
		}

		private float convertX(float value) {
			return (value / 1080 - 0.5f) * 2;
		}

		private float convertY(float value) {
			return (value / 1920 - 0.5f) * -2;
		}

		public float[] pointsPositionToArray() {
			float[] result = new float[points.size() * POSITION_COMPONENT_COUNT * 2];

			int index = 0;
			synchronized (points) {
				Double rightRadian = 0.0;
				Double leftRadian = 0.0;
				for (int pointIndex = 0; pointIndex < points.size() - 1; pointIndex++) {
					Point3F curPoint = points.get(pointIndex);
					Point3F nextPoint = points.get(pointIndex + 1);

					Double radian = Math.atan((nextPoint.y - curPoint.y) / (nextPoint.x - curPoint.x));
					rightRadian = radian - Math.PI / 2;
					leftRadian = radian + Math.PI / 2;

					float leftX = (float)(curPoint.x + Math.cos(leftRadian) * LINE_WIDTH);
					float leftY = (float)(curPoint.y + Math.sin(leftRadian) * LINE_WIDTH);

					float rightX = (float)(curPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
					float rightY = (float)(curPoint.y + Math.sin(rightRadian) * LINE_WIDTH);

//					if (pointIndex == 0) {
						result[index++] = leftX;
						result[index++] = leftY;
						result[index++] = curPoint.z;

						result[index++] = rightX;
						result[index++] = rightY;
						result[index++] = curPoint.z;
/*					} else {
						int savedIndex = index;
						result[index++] = (result[savedIndex - 6] + leftX) / 2;
						result[index++] = (result[savedIndex - 5] + leftY) / 2;
						result[index++] = curPoint.z;

						result[index++] = (result[savedIndex - 3] + rightX) / 2;
						result[index++] = (result[savedIndex - 2] + rightY) / 2;
						result[index++] = curPoint.z;
					}*/
				}

				Point3F curPoint = points.get(points.size() - 1);
				result[index++] = (float)(curPoint.x + Math.cos(leftRadian) * LINE_WIDTH);
				result[index++] = (float)(curPoint.y + Math.sin(leftRadian) * LINE_WIDTH);
				result[index++] = curPoint.z;

				result[index++] = (float)(curPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
				result[index++] = (float)(curPoint.y + Math.sin(rightRadian) * LINE_WIDTH);
				result[index++] = curPoint.z;
			}

			//			for (float f : result) {
			//				Log.w("toArray", String.format("%f", f));
			//			}
			return result;
		}

		public float[] pointsColorToArray() {
			float[] result = new float[points.size() * COLOR_COMPONENT_COUNT * 2];

			int index = 0;
			synchronized (points) {
				for (Point3F pointF : points) {
					result[index++] = 1.0f;//(System.currentTimeMillis() % 101) / 100.f;
					result[index++] = 1.0f;//(System.currentTimeMillis() % 211) / 100.f;
					result[index++] = 1.0f;//(System.currentTimeMillis() % 311) / 100.f;

					result[index++] = 1.0f;//(System.currentTimeMillis() % 101) / 100.f;
					result[index++] = 1.0f;//(System.currentTimeMillis() % 211) / 100.f;
					result[index++] = 1.0f;//(System.currentTimeMillis() % 311) / 100.f;
				}
			}
			return result;
		}
	}

	class Point3F {
		float x;
		float y;
		float z;

		Point3F(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public float[] toArray() {
			return new float[]{x, y, z, 1};
		}

		public void set(float[] vec) {
			x = vec[0];
			y = vec[1];
			z = vec[2];
		}
	}
}
