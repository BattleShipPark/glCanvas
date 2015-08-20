package com.example.lineplus.glcanvas.object;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Vector;

import android.graphics.PointF;
import android.util.Log;

import com.example.lineplus.glcanvas.LineShaderProgram;

public class Lines {
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int COLOR_COMPONENT_COUNT = 3;

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
				if (vertexData.capacity() < line.points.size() * POSITION_COMPONENT_COUNT) {
					vertexData = ByteBuffer.allocateDirect(line.points.size() * POSITION_COMPONENT_COUNT
						* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
				}
				//				vertexData.position(0);
				vertexData.clear();
				vertexData.put(line.pointsPositionToArray());
				vertexData.limit(line.points.size() * POSITION_COMPONENT_COUNT);

				if (colorData.capacity() < line.points.size() * COLOR_COMPONENT_COUNT) {
					colorData = ByteBuffer.allocateDirect(line.points.size()
						* COLOR_COMPONENT_COUNT * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
				}
				//				colorData.position(0);
				colorData.clear();
				colorData.put(line.pointsColorToArray());
				colorData.limit(line.points.size() * COLOR_COMPONENT_COUNT);
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
			glDrawArrays(GL_LINE_STRIP, 0, line.points.size());
		}

		dirty = false;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	private class Line {
		private final List<PointF> points = new Vector<>();

		private Line(float startX, float startY, float endX, float endY) {
			add(startX, startY);
			add(endX, endY);
		}

		private Line(float x, float y) {
			add(x, y);
		}

		private void add(float x, float y) {
			points.add(new PointF(x, y));

//			Log.w("add", String.format("%f,%f", x, y));
		}

		public float[] pointsPositionToArray() {
			float[] result = new float[points.size() * POSITION_COMPONENT_COUNT];

			int index = 0;
			synchronized (points) {
				for (PointF pointF : points) {
					//					Log.w("toArray", String.format("%f,%f", pointF.x, pointF.y));
					result[index++] = (pointF.x / 1080 - 0.5f) * 2;
					result[index++] = (pointF.y / 1920 - 0.5f) * -2;

//					Log.w("toArray2", String.format("%d,%f,%f", index, result[index - 2], result[index - 1]));
				}
			}

			return result;
		}

		public float[] pointsColorToArray() {
			float[] result = new float[points.size() * COLOR_COMPONENT_COUNT];

			int index = 0;
			synchronized (points) {
				for (PointF pointF : points) {
					result[index++] = (System.currentTimeMillis() % 101) / 100.f;
					result[index++] = (System.currentTimeMillis() % 211) / 100.f;
					result[index++] = (System.currentTimeMillis() % 311) / 100.f;
				}
			}
			return result;
		}
	}
}
