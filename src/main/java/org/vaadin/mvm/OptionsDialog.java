package org.vaadin.mvm;

import com.vaadin.addon.touchkit.ui.TabBarView;

public class OptionsDialog extends TabBarView {

	private MainView master;

	private PlaceMarkOptions placeMarkOptions;
	private DisplayedPersons displayedPersons;
	
	public OptionsDialog(MainView master) {
		setSizeUndefined();
		setWidth("100%");
		addStyleName("options-dialog");
		addStyleName("v-touchkit-backgroundstripes");
		this.master = master;
		placeMarkOptions  = new PlaceMarkOptions(master);
		displayedPersons = new DisplayedPersons(master);
	}

	@Override
	public void attach() {
		super.attach();
		addTab(new MyDetails(master));
		addTab(new MapOptions(master));
		addTab(displayedPersons);
		addTab(placeMarkOptions);
	}

	public void refresh() {
		placeMarkOptions.refreshView();
		displayedPersons.refreshView();
	}

}
