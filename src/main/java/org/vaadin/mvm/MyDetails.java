package org.vaadin.mvm;

import org.vaadin.mvm.domain.Person;

import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class MyDetails extends CssLayout {

	private MainView master;
	private VerticalComponentGroup vcg = new VerticalComponentGroup();
	
	private TextField nickName = new TextField();

	public MyDetails(MainView master) {
		this.master = master;
		setCaption("My details");
		setIcon(new ThemeResource("settings.png"));
		
		VerticalComponentGroup about = new VerticalComponentGroup();
		about.setCaption("About MVM");
		about.addComponent(new Label(
				"<div style='padding: 10px 0;'>This is a test and demo app for some Vaadin TouchKit technologies. <br/><strong>Note,</strong> there is currently no persistency in this demo app. When server is restarted, all saved stuff will get lost. Sorry.</div>",
				ContentMode.HTML));
		addComponent(about);
		
		vcg.setCaption("My details");
		addComponent(vcg);
		
		Person user = MobileVaadinMaps.getUser();
		vcg.addComponent(nickName);
		FieldGroup fieldGroup = new FieldGroup();
		fieldGroup.setItemDataSource(new BeanItem<Person>(user));
		fieldGroup.buildAndBindMemberFields(this);
		
	}

}
