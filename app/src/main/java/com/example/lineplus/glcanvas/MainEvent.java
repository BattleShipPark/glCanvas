package com.example.lineplus.glcanvas;

public class MainEvent {
	public enum PointsUpdated {
		UPDATED
	}

	public enum SurfaceChanged {Changed}

/*	public enum Touched {
		TAP
	}*/

	public static class MatrixUpdated {
		public final float[] mRotationM;

		MatrixUpdated(float[] mRotationM) {
			this.mRotationM = mRotationM;
		}
	}
}
