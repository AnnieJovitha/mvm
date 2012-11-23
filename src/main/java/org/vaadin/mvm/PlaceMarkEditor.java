package org.vaadin.mvm;

import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.StorageMode;
import org.vaadin.mvm.domain.Person;
import org.vaadin.mvm.domain.PlaceMark;
import org.vaadin.vol.Point;
import org.vaadin.vol.VectorLayer.VectorDrawnEvent;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Label;

public class PlaceMarkEditor extends Popover implements ClickListener {

	private NavigationView c = new NavigationView();

	private Button close = new Button("✓");

	private PlaceMark placeMark;

	private MainView master;

	public PlaceMarkEditor(MainView master, Component relativeComponent, VectorDrawnEvent event) {
		this.master = master;
		setWidth("90%");
		setHeight("80%");
		placeMark = new PlaceMark();
		Person user = (Person) MobileVaadinMaps.get().getUser();
		user.getPlaceMarks().add(placeMark);
		Point point = event.getVector().getPoints()[0];
		placeMark.setPoint(point);
		close.addListener(this);
		c.setRightComponent(close);
		c.setCaption("Edit Placemark");
		setContent(c);
		CssLayout l = new CssLayout();

		VerticalComponentGroup verticalComponentGroup = new VerticalComponentGroup();
		
		verticalComponentGroup.addComponent(new Label("TODO actually list and show these"));

		Form form = new Form();
		form.setFormFieldFactory(new FormFieldFactory() {

			public Field createField(Item item, Object propertyId,
					Component uiContext) {
				if (propertyId.equals("photo")) {
					UploadField uploadField = new UploadField(StorageMode.FILE);
					uploadField.setCaption(DefaultFieldFactory
							.createCaptionByPropertyId(propertyId));
					uploadField.setButtonCaption("📷");
					// TODO show image and "replace" button if file contains an image
					return uploadField;
				}
				return DefaultFieldFactory.get().createField(item, propertyId,
						uiContext);
			}
		});
		form.setItemDataSource(new BeanItem<PlaceMark>(placeMark));
		
		verticalComponentGroup.addComponent(form);

		l.addComponent(verticalComponentGroup);

		c.setContent(l);
		showRelativeTo(relativeComponent);
	}

	public void buttonClick(ClickEvent event) {
		getParent().removeWindow(this);
		master.addDisplayedPlaceMark(placeMark);
	}

}
