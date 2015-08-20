package com.example.lineplus.glcanvas;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.lineplus.glcanvas.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private MainModel mainModel;

	private GLSurfaceView surfaceView;

	//	private MainEventController eventController;
	private MainController controller;
	private SystemUiHider mSystemUiHider;
	private TouchController touchController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mainModel = new MainModel();

		controller = new MainController();

		MainRenderer renderer = new MainRenderer(this, mainModel, controller.getEventBus());

		surfaceView = (GLSurfaceView)findViewById(R.id.surface_view);
		surfaceView.setEGLContextClientVersion(2);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.setRenderer(renderer);
		surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		renderer.setView(surfaceView);

		//		eventController = new MainEventController();

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, surfaceView, HIDER_FLAGS);
		mSystemUiHider.setup();

		touchController = new TouchController(this, mainModel, surfaceView, controller.getEventBus());
	}
}
