package org.vaadin.mvm;

import java.util.Arrays;

import org.vaadin.mvm.domain.Person;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;

public class MyDetails extends CssLayout {

	private MainView master;
	private VerticalComponentGroup vcg = new VerticalComponentGroup();

	public MyDetails(MainView master) {
		this.master = master;
		setCaption("My details");
		setIcon(new ThemeResource("settings.png"));
		
		VerticalComponentGroup about = new VerticalComponentGroup();
		about.setCaption("About MVM");
		about.addComponent(new Label(
				"<div style='padding: 10px 0;'>This is a test and demo app for some Vaadin TouchKit technologies. <br/><strong>Note,</strong> there is currently no persistency in this demo app. When server is restarted, all saved stuff will get lost. Sorry.</div>",
				Label.CONTENT_XHTML));
		addComponent(about);
		
		vcg.setCaption("My details");
		addComponent(vcg);
		
		Form form = new Form();
		Person user = MobileVaadinMaps.getUser();
		if(user != null) {
			form.setItemDataSource(new BeanItem<Person>(user), Arrays.asList("nickName"));
			vcg.addComponent(form);
		}
		
	}

}
