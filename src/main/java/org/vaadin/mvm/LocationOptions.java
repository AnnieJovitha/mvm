package org.vaadin.mvm;

import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.MethodProperty;

public class LocationOptions extends VerticalComponentGroup implements
		Property.ValueChangeListener {

	private Switch keepTracking = new Switch("Keep tracking");
	private Switch drawRoute = new Switch("Draw route");

	public LocationOptions(MainView master) {
		setCaption("Location");
		addComponent(keepTracking);
		addComponent(drawRoute);
		keepTracking.setPropertyDataSource(new MethodProperty<MainView>(master,
				"keepTracking"));
		drawRoute.setPropertyDataSource(new MethodProperty<MainView>(master,
				"drawRoute"));
	}

	public void valueChange(ValueChangeEvent event) {
	}

}
