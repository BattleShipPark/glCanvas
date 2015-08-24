package com.example.lineplus.glcanvas;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class SensorController implements SensorEventListener,MainEvent.SurfaceChanged.SurfaceChangedListener {
	private static final int MIN_DEGREE = 0;
	private final SensorManager mSensorManager;
	private final WindowManager mWindowManager;
	private final Bus eventBus;
	private float[] mRotationM = new float[16];
	private float[] mInitialRotationV = new float[3];
	private float[] mPrevRotationM = new float[16];
	private boolean needToSetInitialValue;

	public SensorController(Activity activity, Bus eventBus) {
		this.eventBus = eventBus;

		mSensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
		mWindowManager = activity.getWindow().getWindowManager();

		eventBus.register(this);
	}

	float z = 0.0f;

	@Override
	public void onSensorChanged(SensorEvent event) {
		SensorManager.getRotationMatrixFromVector(mRotationM, event.values);

		float[] values = new float[3];
		SensorManager.getOrientation(mRotationM, values);

		float[] degrees = new float[3];
		degrees[0] = (float)Math.toDegrees(values[0]);
		degrees[1] = (float)Math.toDegrees(values[1]);
		degrees[2] = (float)Math.toDegrees(values[2]);
//		Log.i("sensor", String.format("%f, %f, %f", degrees[0], degrees[1], degrees[2]));

		if (needToSetInitialValue) {
			mInitialRotationV = degrees.clone();
			mPrevRotationM = mRotationM.clone();

			needToSetInitialValue = false;
		}


/*		int screenRotation = mWindowManager.getDefaultDisplay().getRotation();
		switch (screenRotation) {
			case Surface.ROTATION_0:
			case Surface.ROTATION_180:
				break;
			case Surface.ROTATION_90:
				SensorManager.remapCoordinateSystem(mRotationM, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationM);
				break;
			case Surface.ROTATION_270:
				SensorManager.remapCoordinateSystem(mRotationM, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X, mRotationM);
				break;
			default:
				break;
		}*/

		float[] delta = new float[3];
		SensorManager.getAngleChange(delta, mRotationM, mPrevRotationM);
		toDegrees(delta);
//		Log.i("delta ", String.format("%f, %f, %f", delta[0], delta[1], delta[2]));

		if (Math.abs(delta[0]) >= MIN_DEGREE
			|| Math.abs(delta[1]) >= MIN_DEGREE
			|| Math.abs(delta[2]) >= MIN_DEGREE) {

			mPrevRotationM = mRotationM.clone();

//			Log.i("new  ", String.format("%f, %f, %f", delta[0], delta[1], delta[2]));

			eventBus.post(new MainEvent.MatrixUpdated(delta));
		}
	}

	private void toDegrees(float[] values) {
		values[0] = (float)Math.toDegrees(values[0]);
		values[1] = (float)Math.toDegrees(values[1]);
		values[2] = (float)Math.toDegrees(values[2]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	@Subscribe
	public void onSurfaceChanged(MainEvent.SurfaceChanged event) {
		needToSetInitialValue = true;
	}

	public void resume() {
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void pause() {
		mSensorManager.unregisterListener(this);
	}
}
