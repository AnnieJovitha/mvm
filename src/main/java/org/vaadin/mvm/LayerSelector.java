package org.vaadin.mvm;

import org.vaadin.vol.Layer;

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
		BeanItemContainer<Layer> beanItemContainer = new BeanItemContainer<Layer>(Layer.class);
		beanItemContainer.addAll(master.getAvailableLayers());
		optionGroup = new OptionGroup(null, beanItemContainer);
		optionGroup.setValue(master.getCurrentLayer());
		optionGroup.addListener(this);
		optionGroup.setItemCaptionPropertyId("caption");
		addComponent(optionGroup);
	}

	public void valueChange(ValueChangeEvent event) {
		Layer value = (Layer) optionGroup.getValue();
		master.setCurrentLayer(value);
	}

}
