package com.example.lineplus.glcanvas.object;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import android.graphics.PointF;

import com.example.lineplus.glcanvas.LineShaderProgram;

public class Lines {
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final float LINE_WIDTH = 0.01f;

	private List<Line> lines = new Vector<>();
	//	private List<Float> vertices = new ArrayList<>();
	private boolean dirty;

	private FloatBuffer vertexData = FloatBuffer.allocate(0);
	private FloatBuffer colorData = FloatBuffer.allocate(0);

	/*	public List<Float> getVertices() {
			return vertices;
		}
	
		public void setVertices(List<Float> vertices) {
			this.vertices = vertices;
		}*/

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
			if (dirty) {
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

			/*			long ts = System.currentTimeMillis();
						Log.w("", String.format("%d, %d", ts, line.points.size()));
						for (int index = 0; index < line.points.size();) {
							Log.w("", String.format("%d, %f, %f", ts, vertexData.get(index++), vertexData.get(index++)));
						}*/
			glDrawArrays(GL_TRIANGLE_STRIP, 0, line.points.size() * 2);
		}

		dirty = false;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	class Line {
		final List<PointF> points = new Vector<>();

		Line(float startX, float startY, float endX, float endY) {
			add(startX, startY);
			add(endX, endY);
		}

		Line(float x, float y) {
			add(x, y);
		}

		void add(float x, float y) {
			PointF pointF = new PointF(convertX(x), convertY(y));
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
					PointF curPoint = points.get(pointIndex);
					PointF nextPoint = points.get(pointIndex + 1);

					Double radian = Math.atan((nextPoint.y - curPoint.y) / (nextPoint.x - curPoint.x));
					rightRadian = radian - Math.PI / 2;
					leftRadian = radian + Math.PI / 2;

					float leftX = (float)(curPoint.x + Math.cos(leftRadian) * LINE_WIDTH);
					float leftY = (float)(curPoint.y + Math.sin(leftRadian) * LINE_WIDTH);

					float rightX = (float)(curPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
					float rightY = (float)(curPoint.y + Math.sin(rightRadian) * LINE_WIDTH);

					if (pointIndex == 0) {
						result[index++] = leftX;
						result[index++] = leftY;

						result[index++] = rightX;
						result[index++] = rightY;
					} else {
						int savedIndex=index;
						result[index] = (result[savedIndex - 4] + leftX) / 2;
						index++;
						result[index] = (result[savedIndex - 3] + leftY) / 2;
						index++;

						result[index] = (result[savedIndex - 2] + rightX) / 2;
						index++;
						result[index] = (result[savedIndex - 1] + rightY) / 2;
						index++;
					}
				}

				PointF curPoint = points.get(points.size() - 1);
				result[index++] = (float)(curPoint.x + Math.cos(leftRadian) * LINE_WIDTH);
				result[index++] = (float)(curPoint.y + Math.sin(leftRadian) * LINE_WIDTH);

				result[index++] = (float)(curPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
				result[index++] = (float)(curPoint.y + Math.sin(rightRadian) * LINE_WIDTH);
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
				for (PointF pointF : points) {
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
}
