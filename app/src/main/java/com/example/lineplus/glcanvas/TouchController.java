package com.example.lineplus.glcanvas;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;

import com.squareup.otto.Bus;

public class TouchController implements View.OnTouchListener {
	private static final int MIN_MOVE = 20;

	private final Context context;
	private final MainModel mainModel;
	private final GLSurfaceView surfaceView;
	private final Bus eventBus;
	private final PointF prevTouch;
	private boolean touching;

	public TouchController(Context context, MainModel mainModel, GLSurfaceView surfaceView, Bus eventBus) {
		this.context = context;
		this.mainModel = mainModel;
		this.surfaceView = surfaceView;
		this.eventBus = eventBus;

		prevTouch = new PointF();

		surfaceView.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!touching) {
					mainModel.getLines().addStartPoint(event.getX(), event.getY(), event.getEventTime());
					prevTouch.set(event.getX(), event.getY());

					touching = true;
				}

				break;
			case MotionEvent.ACTION_MOVE:
				if (Math.abs(prevTouch.x - event.getX()) >= MIN_MOVE
					|| Math.abs(prevTouch.y - event.getY()) >= MIN_MOVE) {
					mainModel.getLines().addDragPoint(event.getX(), event.getY(), event.getEventTime());
					prevTouch.set(event.getX(), event.getY());
					eventBus.post(MainEvent.PointsUpdated.UPDATED);
				}

				break;
			case MotionEvent.ACTION_UP:
				mainModel.getLines().addEndPoint(event.getX(), event.getY(), event.getEventTime());
				eventBus.post(MainEvent.PointsUpdated.UPDATED);

				touching = false;
				//				eventBus.post(MainEvent.Touched.TAP);
				break;
		}
		return true;
	}
}
