package org.vaadin.mvm;

import org.vaadin.mvm.domain.Person;

import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.CssLayout;

public class DisplayedPersons extends CssLayout {

	private final static int PAGE_LENGHT = 20;

	private int page = 0;

	private MainView master;

	private VerticalComponentGroup vcg = new VerticalComponentGroup();

	public DisplayedPersons(final MainView master) {
		this.master = master;
		setCaption("Users");
		setIcon(new ThemeResource("person.png"));
		vcg.setCaption("Users");
		addComponent(vcg);
		refreshView();
		// TODO paging
	}

	public void refreshView() {
		vcg.removeAllComponents();
		Person[] persons = Person.getPersons();
		int start = page * PAGE_LENGHT;
		int end = start + PAGE_LENGHT;
		if (persons.length < end) {
			end = persons.length;
		}
		for (int i = start; i < end; i++) {
			final Person person = persons[i];
			final Switch s = new Switch(person.getNickName());
			s.setValue(master.getDisplayedPersons().contains(person));
			s.addListener(new Property.ValueChangeListener() {
				public void valueChange(ValueChangeEvent event) {
					Boolean b = (Boolean) s.getValue();
					if (b) {
						master.addDisplayedPersons(person);
					} else {
						master.removeDisplayedPersons(person);
					}
				}
			});
			vcg.addComponent(s);
		}
	}

}
