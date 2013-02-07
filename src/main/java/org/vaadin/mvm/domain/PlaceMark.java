package org.vaadin.mvm.domain;

import java.io.File;

import org.vaadin.addon.leaflet.shared.Point;

public class PlaceMark {

	private static final File MVM_IMAGES = new File(
			System.getProperty("user.home") + "/mvm_images");

	static {
		if (!MVM_IMAGES.exists()) {
			MVM_IMAGES.mkdir();
		}
		File[] listFiles = MVM_IMAGES.listFiles();
		for (File file : listFiles) {
			file.delete();
		}
	}

	private static int counter = 0;
	private int id = counter++;
	private double lon;
	private double lat;
	private String name = "Placemark " + id;
//	private File photo = new File(MVM_IMAGES, id + ".jpg");

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public File getPhoto() {
//		return photo;
//	}
//
//	public void setPhoto(File photo) {
//		this.photo = photo;
//	}

	public void setPoint(Point point) {
		lon = point.getLon();
		lat = point.getLat();
	}

}
