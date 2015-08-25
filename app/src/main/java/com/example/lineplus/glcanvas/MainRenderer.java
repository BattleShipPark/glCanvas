package com.example.lineplus.glcanvas;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainRenderer implements GLSurfaceView.Renderer {
	private final Context context;
	private final MainModel mainModel;
	private final Bus eventBus;

	/*	private static final float vertexBuffer[] = new float[]{
				-0.5f, 0.5f,
				0.5f, 0.5f,
				0.5f, -0.5f,
				-0.5f, -0.5f
		};
		private static final float colorBuffer[] = new float[]{
				0f, 0f, 1f,
				0f, 1f, 0f,
				1f, 0f, 0f,
				1f, 1f, 1f
		};*/
	private float[] projectionM = new float[16];

	private LineShaderProgram lineShaderProgram;
	private Queue<Runnable> runOnDraw;
	private GLSurfaceView view;

	private float[] cameraEyeV;
	private float[] cameraUpV;
	private float[] cameraMatrix;

	public MainRenderer(Context context, MainModel mainModel, Bus eventBus) {
		this.context = context;
		this.mainModel = mainModel;
		this.eventBus = eventBus;

		runOnDraw = new LinkedList<>();

		cameraEyeV = new float[]{0, 0, 2, 0};
		cameraUpV = new float[]{0, 1, 0, 0};
		cameraMatrix = new float[16];
		Matrix.setLookAtM(cameraMatrix, 0, cameraEyeV[0], cameraEyeV[1], cameraEyeV[2], 0, 0, 0, cameraUpV[0], cameraUpV[1], cameraUpV[2]);

		eventBus.register(this);

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		lineShaderProgram = new LineShaderProgram(context);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		glViewport(0, 0, width, height);

		/*		float aspectRatio = width > height ? 1.1f * width / height : 1.1f * height / width;
				if (width > height)
					Matrix.orthoM(projectionM, 0, -aspectRatio, aspectRatio, -1.1f, 1.1f, -2f, 2f);
				else
					Matrix.orthoM(projectionM, 0, -1.1f, 1.1f, -aspectRatio, aspectRatio, -2f, 2f);*/
		Matrix.perspectiveM(projectionM, 0, 90, (float)width / height, -1, 1);

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				eventBus.post(new MainEvent.SurfaceChanged(projectionM));
			}
		});
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glClear(GL_COLOR_BUFFER_BIT);

		runAll(runOnDraw);

		mainModel.getLines().draw(lineShaderProgram, projectionM, cameraMatrix);
	}

	@Subscribe
	public void onPointsUpdated(MainEvent.PointsUpdated event) {
		runOnDraw(new Runnable() {
			@Override
			public void run() {
				mainModel.getLines().setPointsUpdated(true);
			}
		});
		view.requestRender();
	}

	@Subscribe
	public void onMatrixUpdated(final MainEvent.MatrixUpdated event) {
		runOnDraw(new Runnable() {
			@Override
			public void run() {
				//				mainModel.getLines().setMatrix(event.mRotationM);
				setCameraMatrix(event.mRotationM);
			}
		});
		view.requestRender();
	}

	/**
	 * @param values azimuth, pitch, roll
	 */
	private void setCameraMatrix(float[] values) {
		float[] rotationM = new float[16];
		Matrix.setRotateM(rotationM, 0, values[0], 0, 0, 1);
		Matrix.rotateM(rotationM, 0, values[1], 1, 0, 0);
		Matrix.rotateM(rotationM, 0, values[2], 0, 1, 0);

		Matrix.multiplyMV(cameraEyeV, 0, rotationM, 0, cameraEyeV, 0);

		/* */

		//		Matrix.setRotateM(rotationM, 0, values[0], 0, 0, 1);
		//		Matrix.rotateM(rotationM, 0, values[1], 1, 0, 0);
		//		Matrix.rotateM(rotationM, 0, values[2], 0, 1, 0);

		Matrix.multiplyMV(cameraUpV, 0, rotationM, 0, cameraUpV, 0);

		Matrix.setLookAtM(cameraMatrix, 0, cameraEyeV[0], cameraEyeV[1], cameraEyeV[2], 0, 0, 0, cameraUpV[0], cameraUpV[1], cameraUpV[2]);
	}

	public void setView(GLSurfaceView view) {
		this.view = view;
	}

	synchronized private void runAll(Queue<Runnable> queue) {
		synchronized (this) {
			while (!queue.isEmpty()) {
				queue.poll().run();
			}
		}
	}

	private void runOnDraw(final Runnable runnable) {
		synchronized (runOnDraw) {
			runOnDraw.add(runnable);
		}
	}
}
