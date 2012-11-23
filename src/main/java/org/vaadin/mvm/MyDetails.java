package org.vaadin.mvm;

import java.util.Arrays;

import org.vaadin.mvm.domain.Person;
import org.vaadin.vol.Layer;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;

public class MyDetails extends CssLayout implements Property.ValueChangeListener {

	private OptionGroup optionGroup;
	private MainView master;
	private VerticalComponentGroup vcg = new VerticalComponentGroup();

	public MyDetails(MainView master) {
		this.master = master;
		setCaption("My details");
		setIcon(new ThemeResource("settings.png"));
		
		addComponent(new Label(
				"<div style='padding:0px 20px 10px 20px;'><h1>MVM</h1>This is a test and demo app for some Vaadin TouchKit technologies. <br/><strong>Note,</strong> there is currently no persistency in this demo app. When server is restarted, all saved stuff will get lost. Sorry.</div>",
				Label.CONTENT_XHTML));
		
		vcg.setCaption("My details");
		addComponent(vcg);
		
		Form form = new Form();
		Person user = (Person) TouchKitApplication.get().getUser();
		if(user != null) {
			form.setItemDataSource(new BeanItem<Person>(user), Arrays.asList("nickName"));
			vcg.addComponent(form);
		}
		
	}

	public void valueChange(ValueChangeEvent event) {
		Layer value = (Layer) optionGroup.getValue();
		master.setCurrentLayer(value);
	}

}
