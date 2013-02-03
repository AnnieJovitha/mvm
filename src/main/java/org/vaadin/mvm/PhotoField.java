package org.vaadin.mvm;

import java.io.File;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;

public class PhotoField extends CustomField {
	private CssLayout layout = new CssLayout();
//	private UploadField uploadField;

	public PhotoField() {
		setCaption("Photo");
		setStyleName("photoupload");
	}
	
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		super.setPropertyDataSource(newDataSource);
		buildField();
//		if(uploadField != null) {
//			uploadField.setPropertyDataSource(newDataSource);
//		}
	}

	private void buildField() {
		layout.removeAllComponents();
		final File value = (File) getValue();
		if (value != null && value.exists()) {
			Embedded embedded = new Embedded();
			embedded.setType(Embedded.TYPE_IMAGE);
//			embedded.setSource(new FileResource(value, MobileVaadinMaps.get()));
			embedded.setHeight("150px");
			setHeight("220px");
			Button deleteButton = new Button("X");
			deleteButton.addClickListener(new Button.ClickListener() {
				public void buttonClick(ClickEvent event) {
					value.delete();
					setPropertyDataSource(getPropertyDataSource());
				}
			});
			layout.addComponent(deleteButton);
			layout.addComponent(embedded);
		} else {
//			uploadField = new UploadField(StorageMode.FILE);
//			uploadField.setFileDeletesAllowed(true);
//			uploadField.addListener(new ValueChangeListener() {
//				public void valueChange(
//						com.vaadin.data.Property.ValueChangeEvent event) {
//					buildField();
//				}
//			});
//			uploadField.setButtonCaption(" "); // we'll set camera icon in the
//												// theme

			Component uploadField  = new Label("Sorry, no EasyUploads in V7 yet");
			layout.addComponent(uploadField  );
			setHeight("70px");
		}
	}

	@Override
	public Class<?> getType() {
		return File.class;
	}

	@Override
	protected Component initContent() {
		return layout;
	}

}
