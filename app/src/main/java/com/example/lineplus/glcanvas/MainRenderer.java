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

	private static final float vertexBuffer[] = new float[]{
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
	};
	private float[] projectionM = new float[16];

	private LineShaderProgram lineShaderProgram;
	private Queue<Runnable> runOnDraw;
	private GLSurfaceView view;

	public MainRenderer(Context context, MainModel mainModel, Bus eventBus) {
		this.context = context;
		this.mainModel = mainModel;
		this.eventBus = eventBus;

		eventBus.register(this);

		runOnDraw = new LinkedList<>();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		lineShaderProgram = new LineShaderProgram(context);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		glViewport(0, 0, width, height);

		float aspectRatio = width > height ? 1.f * width / height : 1.f * height / width;
		if (width > height)
			Matrix.orthoM(projectionM, 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
		else
			Matrix.orthoM(projectionM, 0, -1, 1, -aspectRatio, aspectRatio, -1, 1);

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				eventBus.post(MainEvent.SurfaceChanged.Changed);
			}
		});
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glClear(GL_COLOR_BUFFER_BIT);

		runAll(runOnDraw);

		mainModel.getLines().draw(lineShaderProgram, projectionM);
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
				mainModel.getLines().setMatrix(event.mRotationM);
			}
		});
		view.requestRender();
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
