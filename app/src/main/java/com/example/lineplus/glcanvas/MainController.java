package com.example.lineplus.glcanvas;

import com.squareup.otto.Bus;

public class MainController {
	private final Bus eventBus;

	MainController() {
		eventBus = new Bus();
		eventBus.register(new MainEventController());
	}

	public Bus getEventBus() {
		return eventBus;
	}
}
