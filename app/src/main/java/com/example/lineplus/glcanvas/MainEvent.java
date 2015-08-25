package com.example.lineplus.glcanvas;

public class MainEvent {
	public enum PointsUpdated {
		UPDATED
	}

	public static class SurfaceChanged {
		public final int width;
		public final int height;
		public float[] projectionM;

		public SurfaceChanged(int width, int height, float[] projectionM) {
			this.width = width;
			this.height = height;
			this.projectionM = projectionM.clone();
		}

		public interface SurfaceChangedListener {
			void onSurfaceChanged(SurfaceChanged event);
		}
	}

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
