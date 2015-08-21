package com.example.lineplus.glcanvas;

import com.squareup.otto.Bus;

public class MainController {
	private final Bus bus;

	public MainController() {
		bus = new Bus();
	}

	public Bus getEventBus() {
		return bus;
	}
}
