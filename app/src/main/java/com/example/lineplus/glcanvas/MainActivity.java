package com.example.lineplus.glcanvas;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import com.example.lineplus.glcanvas.util.SystemUiHider;
import com.squareup.otto.Subscribe;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements SensorEventListener {
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
	private SensorManager mSensorManager;
	private Sensor mAccSensor;
	private Sensor mMagSensor;

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

		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		controller.getEventBus().register(this);
	}

	@Override
	protected void onResume() {
		mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mMagSensor, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	float[] mRefOrientation = new float[3];
	float[] mPrevOrientation = new float[3];
	float[] mGravity;
	float[] mGeomagnetic;

	private static final int MIN_DEGREE = 5;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				//				azimut = orientation[0]; // orientation contains: azimut, pitch and roll

				orientation[0] = (float)Math.toDegrees(orientation[0]);
				orientation[1] = (float)Math.toDegrees(orientation[1]);
				orientation[2] = (float)Math.toDegrees(orientation[2]);

				Log.i("input", String.format("%f, %f, %f %f, %f, %f", mRefOrientation[0], mRefOrientation[1], mRefOrientation[2], orientation[0], orientation[1], orientation[2]));

				toPositive(orientation);

				double azimuthDelta = getDelta(orientation[0], mPrevOrientation[0]);
				double pitchDelta = getDelta(orientation[1], mPrevOrientation[1]);
				double rollDelta = getDelta(orientation[2], mPrevOrientation[2]);
				if (Math.abs(azimuthDelta) >= MIN_DEGREE || Math.abs(pitchDelta) >= MIN_DEGREE
					|| Math.abs(rollDelta) >= MIN_DEGREE) {
					double azimuth = getDelta(orientation[0], mRefOrientation[0]);
					double pitch = getDelta(orientation[1], mRefOrientation[1]);
					double roll = getDelta(orientation[2], mRefOrientation[2]);
					Log.w("adjust", String.format("%f, %f, %f", azimuth, pitch, roll));
				}

				mPrevOrientation[0] = orientation[0];
				mPrevOrientation[1] = orientation[1];
				mPrevOrientation[2] = orientation[2];
			}
		}

	}

	private void toPositive(float[] degree) {
		for (int index = 0; index < degree.length; index++)
			if (degree[index] < 0)
				degree[index] += 360;
	}

	private float getDelta(float degree1, float degree2) {
		float delta = Math.abs(degree1 - degree2);
		if (delta > 180)
			delta -= 180;
		return delta;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Subscribe
	public void onTap(MainEvent.Touched event) {
		float R[] = new float[9];
		float I[] = new float[9];
		boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		if (success) {
			float orientation[] = new float[3];
			SensorManager.getOrientation(R, orientation);

			orientation[0] = (float)Math.toDegrees(orientation[0]);
			orientation[1] = (float)Math.toDegrees(orientation[1]);
			orientation[2] = (float)Math.toDegrees(orientation[2]);

			toPositive(orientation);

			mRefOrientation[0] = mPrevOrientation[0] = orientation[0];
			mRefOrientation[1] = mPrevOrientation[1] = orientation[1];
			mRefOrientation[2] = mPrevOrientation[2] = orientation[2];
		}
	}
}
