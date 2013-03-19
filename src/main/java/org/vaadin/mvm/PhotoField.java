package org.vaadin.mvm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.vaadin.easyuploads.UploadField;
import org.vaadin.easyuploads.UploadField.StorageMode;

import com.vaadin.data.Property;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Image;

public class PhotoField extends CustomField<File> {
	private CssLayout layout = new CssLayout();
	private UploadField uploadField;

	public PhotoField() {
		setCaption("Photo");
		setStyleName("photoupload");
		buildField();
	}
	
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		super.setPropertyDataSource(newDataSource);
		buildField();
		if(uploadField != null) {
			uploadField.setPropertyDataSource(newDataSource);
		}
	}

	private void buildField() {
		layout.removeAllComponents();
		final File value = getValue();
		if (value != null && value.exists()) {
			Image embedded = new Image();
			
			try {
				// scale to max 500x500
				int maxsize = 500;
				BufferedImage bi = ImageIO.read(value);
				if(bi.getWidth() > maxsize || bi.getHeight() > maxsize) {
					int w = maxsize, h = maxsize;
					if(bi.getWidth() > bi.getHeight()) {
						h = 500*bi.getHeight()/bi.getWidth();
					} else {
						w = 500*bi.getWidth()/bi.getHeight();
					}
					java.awt.Image scaledInstance = bi.getScaledInstance(w, h, java.awt.Image.SCALE_DEFAULT);
					 BufferedImage scaled = new BufferedImage(scaledInstance.getWidth(null), scaledInstance.getHeight(null), BufferedImage.TYPE_INT_RGB);
					scaled.getGraphics().drawImage(scaledInstance, 0, 0, w, h, null);
					ImageIO.write(scaled, "jpg", value);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			embedded.setSource(new FileResource(value));
			embedded.setWidth("100%");
			setHeight("200px");
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
			uploadField = new UploadField(StorageMode.FILE);
			uploadField.setAcceptFilter("image/*");
			uploadField.setFileDeletesAllowed(true);
			uploadField.addListener(new ValueChangeListener() {
				public void valueChange(
						com.vaadin.data.Property.ValueChangeEvent event) {
					buildField();
				}
			});
			uploadField.setButtonCaption(" "); // we'll set camera icon in the
												// theme
			layout.addComponent(uploadField  );
			setHeight("70px");
		}
	}

	@Override
	public Class<File> getType() {
		return File.class;
	}

	@Override
	protected Component initContent() {
		return layout;
	}

}
