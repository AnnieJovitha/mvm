package org.vaadin.mvm;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class MapOptions extends CssLayout implements Component {
	
	public MapOptions(MainView master) {
		setCaption("Map");
		setIcon(new ThemeResource("map.png"));
		addComponent(new LayerSelector(master));
		addComponent(new LocationOptions(master));
	}

}
