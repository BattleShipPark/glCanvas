package com.example.lineplus.glcanvas;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainRenderer implements GLSurfaceView.Renderer {
	private final Context context;
	private final MainModel mainModel;

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

	private LineShaderProgram lineShaderProgram;
	private Queue<Runnable> runOnDraw;
	private GLSurfaceView view;

	public MainRenderer(Context context, MainModel mainModel, Bus eventBus) {
		this.context = context;
		this.mainModel = mainModel;

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
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glClear(GL_COLOR_BUFFER_BIT);

		runAll(runOnDraw);

		mainModel.getLines().draw(lineShaderProgram);
	}

	@Subscribe
	public void onPointsUpdated(MainEvent.PointsUpdated event) {
		runOnDraw(new Runnable() {
			@Override
			public void run() {
				mainModel.getLines().setDirty(true);
			}
		});
		view.requestRender();
	}

	synchronized private void runAll(Queue<Runnable> queue) {
		synchronized (this) {
			while (!queue.isEmpty()) {
				queue.poll().run();
			}
		}
	}

	protected void runOnDraw(final Runnable runnable) {
		synchronized (runOnDraw) {
			runOnDraw.add(runnable);
		}
	}

	public void setView(GLSurfaceView view) {
		this.view = view;
	}
}
