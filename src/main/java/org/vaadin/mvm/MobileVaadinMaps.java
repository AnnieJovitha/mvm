package org.vaadin.mvm;

import org.vaadin.mvm.domain.Person;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
@Widgetset("org.vaadin.mvm.gwt.AppWidgetSet")
@Theme("mvm")
@Title("MVM")
public class MobileVaadinMaps extends UI {
	
	@Override
	protected void init(VaadinRequest request) {
		MainView newContent = new MainView();
		setContent(newContent);
		if (getUser().getLastLocation() != null) {
			newContent.updateMap();
			try {
				newContent.getAvailableLayers().get(
						getUser().getLastLayerIndex());
			} catch (Exception e) {
			}
		}
	}

	public static Person getUser() {
		VaadinSession session = UI.getCurrent().getSession();
		return (Person) session.getSession().getAttribute("user");
	}
	
	public MainView getMainView() {
		return (MainView) getContent();
	}

}
