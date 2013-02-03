package org.vaadin.mvm;

import org.vaadin.addon.leaflet.shared.BaseLayer;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.OptionGroup;

public class LayerSelector extends VerticalComponentGroup implements Property.ValueChangeListener {

	private OptionGroup optionGroup;
	private MainView master;

	public LayerSelector(MainView master) {
		this.master = master;
		setCaption("Background map");
		BeanItemContainer<BaseLayer> beanItemContainer = new BeanItemContainer<BaseLayer>(BaseLayer.class);
		beanItemContainer.addAll(master.getAvailableLayers());
		optionGroup = new OptionGroup(null, beanItemContainer);
		optionGroup.setValue(master.getCurrentLayer());
		optionGroup.addValueChangeListener(this);
		addComponent(optionGroup);
	}

	public void valueChange(ValueChangeEvent event) {
		BaseLayer value = (BaseLayer) optionGroup.getValue();
		master.setCurrentLayer(value);
	}

}
