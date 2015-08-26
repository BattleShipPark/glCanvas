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

import android.util.Log;

import com.example.lineplus.glcanvas.LineShaderProgram;
import com.example.lineplus.glcanvas.MainEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class Lines implements MainEvent.SurfaceChanged.SurfaceChangedListener {
	private static final int POSITION_COMPONENT_COUNT = 3;
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final int NUM_DIVISION_CIRCLE = 3;
	private static final int MAX_POINTS_COUNT = 1024;
	private static final float LINE_WIDTH = 0.05f;
	private static final int VERTEX_COUNT_COEFF = 2;//(NUM_DIVISION_CIRCLE + 1) * 2;

	private List<Line> lines = new Vector<>();
	//	private List<Float> vertices = new ArrayList<>();

	private FloatBuffer vertexData = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private FloatBuffer colorData = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder()).asFloatBuffer();
	private int screenWidth, screenHeight;

	private boolean pointsUpdated;
	private boolean cameraMatrixUpdated;

	public Lines(Bus eventBus) {
		vertexData = ByteBuffer.allocateDirect(MAX_POINTS_COUNT * POSITION_COMPONENT_COUNT
			* VERTEX_COUNT_COEFF * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		colorData = ByteBuffer.allocateDirect(MAX_POINTS_COUNT * COLOR_COMPONENT_COUNT * VERTEX_COUNT_COEFF
			* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

		eventBus.register(this);
	}

	@Override
	@Subscribe
	public void onSurfaceChanged(MainEvent.SurfaceChanged event) {
		screenWidth = event.width;
		screenHeight = event.height;

		/*		Line line = new Line(0, 0);
				line.add(540, 400);
				line.add(980, 900);
				line.add(100, 1500);
		
				lines.add(line);
		
				setPointsUpdated(true);*/
	}

	public void addStartPoint(float x, float y, long eventTime) {
		lines.add(new Line(x, y));
	}

	public void addDragPoint(float x, float y, long eventTime) {
		Line line = lines.get(lines.size() - 1);
		line.add(x, y);
	}

	public void addEndPoint(float x, float y, long eventTime) {
	}

	public void draw(LineShaderProgram program, float[] projectionM, float[] cameraM) {
		if (lines.size() == 0)
			return;

		program.useProgram();

		program.setUniforms(projectionM, cameraM);

		for (Line line : lines) {
			if (cameraMatrixUpdated || pointsUpdated) {
				/*				if (vertexData.capacity() < line.points.size() * POSITION_COMPONENT_COUNT * VERTEX_COUNT_COEFF) {
									vertexData = ByteBuffer.allocateDirect(line.points.size() * POSITION_COMPONENT_COUNT
										* VERTEX_COUNT_COEFF * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
								}
								if (colorData.capacity() < line.points.size() * COLOR_COMPONENT_COUNT * VERTEX_COUNT_COEFF) {
									colorData = ByteBuffer.allocateDirect(line.points.size()
										* COLOR_COMPONENT_COUNT * VERTEX_COUNT_COEFF
										* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
								}*/

				//				Log.w("", String.format("%d, %d, %d, %d, %d", line.points.size(), vertexData.capacity(), vertexData.limit(), vertexData.remaining(), line.pointsPositionToArray().length));
				vertexData.clear();
				vertexData.put(line.pointsPositionToArray());
				vertexData.limit(line.points.size() * POSITION_COMPONENT_COUNT * VERTEX_COUNT_COEFF);

				colorData.clear();
				colorData.put(line.pointsColorToArray());
				colorData.limit(line.points.size() * COLOR_COMPONENT_COUNT * VERTEX_COUNT_COEFF);
			}

			vertexData.position(0);
			glVertexAttribPointer(program.getAttrPosition(), POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
			glEnableVertexAttribArray(program.getAttrPosition());

			colorData.position(0);
			glVertexAttribPointer(program.getAttrColor(), COLOR_COMPONENT_COUNT, GL_FLOAT, false, 0, colorData);
			glEnableVertexAttribArray(program.getAttrColor());

			//			long ts = System.currentTimeMillis();
			//			Log.w("", String.format("%d", line.points.size()));
			//			for (int index = 0; index < line.points.size();) {
			//				Log.w("", String.format("%d, %f, %f", ts, vertexData.get(index++), vertexData.get(index++)));
			//			}
			glDrawArrays(GL_TRIANGLE_STRIP, 0, line.points.size() * 2);
		}

		cameraMatrixUpdated = pointsUpdated = false;
	}

	public void setPointsUpdated(boolean pointsUpdated) {
		this.pointsUpdated = pointsUpdated;
	}

	public void setCameraMatrixUpdated(boolean cameraMatrixUpdated) {
		this.cameraMatrixUpdated = cameraMatrixUpdated;
	}

	/**
	 * @param values azimuth, pitch, roll
	 */
	/*	public void setMatrix(float[] values) {
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
		}*/

	class Line {
		final List<Point3F> points = new Vector<>();

		Line(float x, float y) {
			add(x, y);
		}

		void add(float x, float y) {
			if (points.size() >= MAX_POINTS_COUNT) {
				Log.w("", String.format("Line.add(): %d, %d", points.size(), MAX_POINTS_COUNT));
				return;
			}

			Point3F pointF = new Point3F(convertX(x), convertY(y), 0);
			points.add(pointF);

			//			Log.w("add", String.format("%f,%f - %f,%f", x, y, pointF.x, pointF.y));
		}

		private float convertX(float value) {
			return (value / screenWidth - 0.5f) * 2;
		}

		private float convertY(float value) {
			return (value / screenHeight - 0.5f) * -2;
		}

		/*		public float[] pointsPositionToArray() {
					float[] result = new float[points.size() * POSITION_COMPONENT_COUNT * VERTEX_COUNT_COEFF];
		
					int index = 0;
					synchronized (points) {
						Double rightRadian;
						for (int pointIndex = 0; pointIndex < points.size() - 1; pointIndex++) {
							Point3F curPoint = points.get(pointIndex);
							Point3F nextPoint = points.get(pointIndex + 1);
		
							Double radian = Math.atan((nextPoint.y - curPoint.y) / (nextPoint.x - curPoint.x));
							rightRadian = radian - Math.PI / 2;
		
							float curRightX = (float)(curPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
							float curRightY = (float)(curPoint.y + Math.sin(rightRadian) * LINE_WIDTH);
		
							float nextRightX = (float)(nextPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
							float nextRightY = (float)(nextPoint.y + Math.sin(rightRadian) * LINE_WIDTH);
		
							int startIndex = index;
							for (float rotationAngle = 0; rotationAngle < 2 * Math.PI; rotationAngle += 2 * Math.PI
								/ NUM_DIVISION_CIRCLE) {
								result[index++] = (float)((curRightX - curPoint.x) * Math.cos(rotationAngle) + curRightX);
								result[index++] = (float)(curPoint.y - (curPoint.y - curRightY) * Math.cos(rotationAngle));
								result[index++] = (float)(curPoint.z - Math.sin(rotationAngle));
		
								result[index++] = (float)((nextRightX - nextPoint.x) * Math.cos(rotationAngle) + nextRightX);
								result[index++] = (float)(nextPoint.y - (nextPoint.y - nextRightY) * Math.cos(rotationAngle));
								result[index++] = (float)(nextPoint.z - Math.sin(rotationAngle));
							}
							result[index++] = result[startIndex++];
							result[index++] = result[startIndex++];
							result[index++] = result[startIndex++];
		
							result[index++] = result[startIndex++];
							result[index++] = result[startIndex++];
							result[index++] = result[startIndex++];
						}
					}
		
					return result;
				}*/

		public float[] pointsPositionToArray() {
			float[] result = new float[points.size() * POSITION_COMPONENT_COUNT * VERTEX_COUNT_COEFF];

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

					result[index++] = leftX;
					result[index++] = leftY;
					result[index++] = curPoint.z;

					result[index++] = rightX;
					result[index++] = rightY;
					result[index++] = curPoint.z;
				}

				Point3F curPoint = points.get(points.size() - 1);
				result[index++] = (float)(curPoint.x + Math.cos(leftRadian) * LINE_WIDTH);
				result[index++] = (float)(curPoint.y + Math.sin(leftRadian) * LINE_WIDTH);
				result[index++] = curPoint.z;

				result[index++] = (float)(curPoint.x + Math.cos(rightRadian) * LINE_WIDTH);
				result[index++] = (float)(curPoint.y + Math.sin(rightRadian) * LINE_WIDTH);
				result[index++] = curPoint.z;
			}

			return result;
		}

		public float[] pointsColorToArray() {
			float[] result = new float[points.size() * COLOR_COMPONENT_COUNT * VERTEX_COUNT_COEFF];

			synchronized (points) {
				for (int index = 0; index < result.length; index++) {
					result[index] = Math.min(1.f * index / result.length + 0.5f, 1f);
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

		public Point3F(float[] vec) {
			this.x = vec[0];
			this.y = vec[1];
			this.z = vec[1];
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
