package org.vaadin.mvm;

import java.util.Set;

import org.vaadin.mvm.domain.Person;
import org.vaadin.mvm.domain.PlaceMark;

import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class PlaceMarkOptions extends CssLayout {

	private VerticalComponentGroup vcg;
	private MainView master;

	public PlaceMarkOptions(MainView master) {
		this.master = master;
		setCaption("Placemarks");
		setIcon(new ThemeResource("placemark.png"));
		vcg = new VerticalComponentGroup();
		vcg.setCaption("Placemarks");
		addComponent(vcg);
	}

	@Override
	public void attach() {
		super.attach();
		refreshView();
	}

	protected void refreshView() {
		if (getApplication() != null) {
			vcg.removeAllComponents();
			Person user = (Person) MobileVaadinMaps.get().getUser();
			Set<PlaceMark> placeMarks = user.getPlaceMarks();
			if (placeMarks.isEmpty()) {
				vcg.addComponent(new Label("You have no placemarks..."));
			}
			for (final PlaceMark placeMark : placeMarks) {
				Switch switch1 = new Switch();
				switch1.setImmediate(true);
				switch1.setCaption(placeMark.getName());
				switch1.setValue(master.getDisplayedPlaces().containsKey(placeMark));
				switch1.addListener(new ValueChangeListener() {
					public void valueChange(ValueChangeEvent event) {
						Boolean value = (Boolean) event.getProperty()
								.getValue();
						if (value) {
							master.addDisplayedPlaceMark(placeMark);
						} else {
							master.removeDisplayedPlaceMark(placeMark);
						}
					}
				});
				vcg.addComponent(switch1);
			}
		}
	}

}
