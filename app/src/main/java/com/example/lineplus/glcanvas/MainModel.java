package com.example.lineplus.glcanvas;

import android.opengl.Matrix;

import com.example.lineplus.glcanvas.object.Lines;
import com.squareup.otto.Bus;

public class MainModel {
	private final Lines lines;

	public MainModel(Bus eventBus) {
		lines = new Lines(eventBus);
	}

	public Lines getLines() {
		return lines;
	}
}
