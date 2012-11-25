package org.vaadin.mvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.mvm.domain.Person;
import org.vaadin.mvm.domain.PlaceMark;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Control;
import org.vaadin.vol.Layer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.OpenStreetMapLayer;
import org.vaadin.vol.Point;
import org.vaadin.vol.PointVector;
import org.vaadin.vol.PolyLine;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;
import org.vaadin.vol.VectorLayer.DrawingMode;
import org.vaadin.vol.VectorLayer.SelectionMode;
import org.vaadin.vol.VectorLayer.VectorDrawnEvent;
import org.vaadin.vol.VectorLayer.VectorDrawnListener;
import org.vaadin.vol.VectorLayer.VectorSelectedEvent;
import org.vaadin.vol.VectorLayer.VectorSelectedListener;
import org.vaadin.vol.XYZLayer;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.vaadin.addon.touchkit.service.Position;
import com.vaadin.addon.touchkit.service.PositionCallback;
import com.vaadin.addon.touchkit.ui.HorizontalComponentGroup;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.TouchKitWindow;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.ProgressIndicator;

public class MainView extends NavigationView implements PositionCallback,
		ClickListener, VectorDrawnListener, VectorSelectedListener {

	private static final ThemeResource SETTINGS_ICON = new ThemeResource("settings_g.png");
	private static final int MAX_POINTS = 100;
	private boolean keepTracking = false;
	private boolean drawRoute = false;
	List<Point> lastPoints = new LinkedList<Point>();

	private Map<Person, PointVector> displayedPersons = new HashMap<Person, PointVector>();
	private Map<PlaceMark, PointVector> displayedPlaces = new HashMap<PlaceMark, PointVector>();

	private OpenLayersMap map = new OpenLayersMap();
	private OpenStreetMapLayer osm = new OpenStreetMapLayer();
	private VectorLayer vl = new VectorLayer();

	private PointVector myPlace = new PointVector();
	private HorizontalComponentGroup mapTools = new HorizontalComponentGroup();
	private Button optionsButton = new Button("⚒", this);
	private Button locateButton = new Button("ʘ", this);
	private Button addPlacemark = new Button("⚑", this);
	private OptionsDialog settings = new OptionsDialog(this);

	private ProgressIndicator progressIndicator = new ProgressIndicator();

	private CssLayout content = new CssLayout() {
		protected String getCss(com.vaadin.ui.Component c) {
			// TODO move this stuff to styles.css
			if (c == progressIndicator) {
				return "margin-left:-2000px;;overflow:hidden;";
			}
			String css = "position:absolute;top:0;left:0;";
			if (c == settings) {
				css += "bottom:0;";
			}
			return css;
		};
	};

	public MainView() {
		setCaption("MVM");
		content.addStyleName("mainview");
		setSizeFull();
		map.setSizeFull();
		map.getControls().clear();
		map.addControl(Control.TouchNavigation);
		map.addControl(Control.ZoomPanel);
		map.addControl(Control.ScaleLine);
		map.addControl(Control.Attribution);

		content.setSizeFull();
		content.addComponent(map);
		setContent(content);
		setRightComponent(optionsButton);

		mapTools.addComponent(locateButton);
		mapTools.addComponent(addPlacemark);
		setLeftComponent(mapTools);

		progressIndicator.setPollingInterval(5000);
		content.addComponent(progressIndicator);

		vl.addListener((VectorDrawnListener) this);
		vl.addListener((VectorSelectedListener) this);
		vl.setSelectionMode(SelectionMode.SIMPLE);

		/*
		 * Android font don't contain all cool unicode characters, as a fallback
		 * use graphics
		 */
		optionsButton.setCaption(null);
		optionsButton.setIcon(SETTINGS_ICON);
		addPlacemark.setCaption(null);
		addPlacemark.setIcon(new ThemeResource("placemark_g.png"));
	}

	@Override
	public TouchKitWindow getWindow() {
		return (TouchKitWindow) super.getWindow();
	}

	private List<Layer> availableLayers = new ArrayList<Layer>();
	private Layer currentLayer;
	private PolyLine routeVector;
	private boolean automaticDetection;

	private void defineBaseLayers() {
		buildStyleMap();
		map.setJsMapOptions("{projection: "
				+ "new OpenLayers.Projection(\"EPSG:900913\"),"
				+ "units: \"m\","
				+ "numZoomLevels: 18,"
				+ "maxResolution: 156543.0339, "
				+ "maxExtent: new OpenLayers.Bounds(-20037508, -20037508,20037508, 20037508.34)}");

		osm.setCaption("OpenStreetMap");
		map.setZoom(13);

		// OSM layer as default
		availableLayers.add(osm);

		// For users in Finland, some detailed terrain maps by MML, hosted by
		// kapsi
		XYZLayer xyzLayer = new XYZLayer();
		xyzLayer.setUri("http://tiles.kartat.kapsi.fi/peruskartta/${z}/${x}/${y}.png");
		xyzLayer.setSphericalMercator(true);
		xyzLayer.setDisplayName("Peruskartta");
		xyzLayer.setCaption("Peruskartta");
		xyzLayer.setAttribution("&copy; Maanmittauslaitos, hosted by kartat.kapsi.fi");
		availableLayers.add(xyzLayer);

		// ... and areal imaginary
		xyzLayer = new XYZLayer();
		xyzLayer.setUri("http://tiles.kartat.kapsi.fi/ortokuva/${z}/${x}/${y}.png");
		xyzLayer.setSphericalMercator(true);
		xyzLayer.setDisplayName("Ortokuva");
		xyzLayer.setCaption("Ortokuva");
		xyzLayer.setAttribution("&copy; Maanmittauslaitos, hosted by kartat.kapsi.fi");
		availableLayers.add(xyzLayer);

		setCurrentLayer(osm);

		map.addLayer(vl);
	}

	private void buildStyleMap() {
		StyleMap styleMap = new StyleMap();
		styleMap.setExtendDefault(true);

		Style style = new Style();
		style.setStrokeColor("#00F9FF");
		style.setFillColor("#00F9FF");
		style.setPointRadius(12);
		styleMap.setStyle("other", style);

		style = new Style();
		style.setStrokeColor("#F200FF");
		style.setFillColor("#F200FF");
		style.setPointRadius(12);
		styleMap.setStyle("default", style);

		style = new Style();
		style.setStrokeColor("#00FF06");
		style.setFillColor("#00FF06");
		style.setPointRadius(12);
		styleMap.setStyle("placemark", style);

		vl.setStyleMap(styleMap);

	}

	public List<Layer> getAvailableLayers() {
		return availableLayers;
	}

	public void setCurrentLayer(Layer l) {
		map.addLayer(l);
		if (currentLayer != null) {
			map.removeLayer(currentLayer);
		}
		currentLayer = l;
		getUser().setLastLayerIndex(availableLayers.indexOf(l));
	}

	public Layer getCurrentLayer() {
		return currentLayer;
	}

	@Override
	public void attach() {
		super.attach();
		defineBaseLayers();
		detectPosition(false);
		content.addComponent(settings);
	}

	public void detectPosition(boolean automaticDetection) {
		this.automaticDetection = automaticDetection;
		getWindow().detectCurrentPosition(this);
	}

	private Person getUser() {
		return ((MobileVaadinMaps) getApplication()).getUser();
	}

	public void onSuccess(Position position) {
		getUser().setAccuracy(position.getAccuracy());
		getUser().setLastLocation(
				new Point(position.getLongitude(), position.getLatitude()));
		updateMap();
	}

	public void updateMap() {
		lastPoints.add(getUser().getLastLocation());
		if (lastPoints.size() > MAX_POINTS) {
			lastPoints.remove(0);
		}
		myPlace.setPoints(getUser().getLastLocation());
		if (myPlace.getParent() == null) {
			vl.addVector(myPlace);
			map.setZoom(10);
		}
		if (keepTracking && isTrackingEnabled()) {
			// TODO TouchKit should support continuous tracking
			new Thread() {
				public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
					synchronized (getApplication()) {
						detectPosition(true);
					}
				};
			}.start();
		}
		if (!automaticDetection) {
			setExtent();
		}
		updateMyRoute();
	}

	private void setExtent() {
		Bounds bounds = new Bounds(myPlace.getPoint());
		Collection<PointVector> values = displayedPersons.values();
		for (PointVector pointVector : values) {
			bounds.extend(pointVector.getPoint());
		}
		if (values.isEmpty()) {
			map.setCenter(bounds);
		} else {
			map.zoomToExtent(bounds);
		}
	}

	private void updateMyRoute() {
		if (drawRoute) {
			if (routeVector == null) {
				routeVector = new PolyLine();
				vl.addComponent(routeVector);
			}
			routeVector.setPoints(lastPoints.toArray(new Point[lastPoints
					.size()]));
		}
	}

	public void onFailure(int errorCode) {

	}

	public void buttonClick(ClickEvent event) {
		if (event.getButton() == optionsButton) {
			toggleOptions();
		} else if (event.getButton() == locateButton) {
			if (keepTracking) {
				toggleTracking();
			} else {
				detectPosition(false);
			}
		} else if (event.getButton() == addPlacemark) {
			if (vl.getDrawingMode() == DrawingMode.POINT) {
				disableDrawingMode();
			} else {
				enableDrawingMode();
			}
		}
	}

	private void enableDrawingMode() {
		addPlacemark.addStyleName("green");
		vl.setDrawingMode(DrawingMode.POINT);

	}

	private void disableDrawingMode() {
		addPlacemark.removeStyleName("green");
		vl.setDrawingMode(DrawingMode.NONE);
	}

	private void toggleOptions() {
		if (getStyleName().contains("options-on")) {
			removeStyleName("options-on");
			optionsButton.setCaption(null);
			optionsButton.setIcon(SETTINGS_ICON);
			mapTools.setVisible(true);
		} else {
			addStyleName("options-on");
			optionsButton.setIcon(null);
			optionsButton.setCaption("✓");
			mapTools.setVisible(false);
			settings.refresh();
		}
	}

	private void toggleTracking() {
		setTracking(!isTrackingEnabled());
		if (isTrackingEnabled()) {
			detectPosition(false);
		}
	}

	private void setTracking(boolean b) {
		if (b) {
			locateButton.addStyleName("green");
		} else {
			locateButton.removeStyleName("green");
		}
	}

	private boolean isTrackingEnabled() {
		return locateButton.getStyleName().contains("green");
	}

	public boolean isDrawRoute() {
		return drawRoute;
	}

	public void setDrawRoute(boolean drawRout) {
		this.drawRoute = drawRout;
	}

	public boolean isKeepTracking() {
		return keepTracking;
	}

	public void setKeepTracking(boolean keepTracking) {
		this.keepTracking = keepTracking;
	}

	public Collection<Person> getDisplayedPersons() {
		return displayedPersons.keySet();
	}

	public void addDisplayedPersons(Person p) {
		if (p.getLastLocation() != null) {
			PointVector pointVector = new PointVector();
			pointVector.setRenderIntent("other");
			pointVector.setPoints(p.getLastLocation());
			pointVector.setData(p);
			displayedPersons.put(p, pointVector);
			vl.addVector(pointVector);
		}
	}

	public void removeDisplayedPersons(Person person) {
		PointVector pointVector = displayedPersons.remove(person);
		vl.removeComponent(pointVector);
	}

	public void vectorDrawn(VectorDrawnEvent event) {
		new PlaceMarkEditor(this, addPlacemark, event);
		disableDrawingMode();
	}

	public Map<PlaceMark, PointVector> getDisplayedPlaces() {
		return displayedPlaces;
	}

	public void addDisplayedPlaceMark(PlaceMark pm) {
		PointVector pointVector = new PointVector();
		pointVector.setRenderIntent("placemark");
		pointVector.setPoints(new Point(pm.getLon(), pm.getLat()));
		pointVector.setData(pm);
		displayedPlaces.put(pm, pointVector);
		vl.addVector(pointVector);
	}

	public void removeDisplayedPlaceMark(PlaceMark pm) {
		PointVector remove = displayedPlaces.remove(pm);
		if (remove != null) {
			vl.removeComponent(remove);
		}
	}

	public void setDisplayedPlaces(Map<PlaceMark, PointVector> displayedPlaces) {
		this.displayedPlaces = displayedPlaces;
	}

	public void vectorSelected(VectorSelectedEvent event) {
		Vector vector = event.getVector();
		if (vector == myPlace) {
			getWindow().showNotification("That is you!");
		} else {

			LatLng point1 = new LatLng(myPlace.getPoint().getLat(), myPlace
					.getPoint().getLon());
			LatLng point2 = new LatLng(vector.getPoints()[0].getLat(),
					vector.getPoints()[0].getLon());
			double distanceInKilometers = LatLngTool.distance(point1, point2,
					LengthUnit.KILOMETER);

			Object data = vector.getData();
			if (data instanceof Person) {
				Person p = (Person) data;
				getWindow().showNotification(
						String.format("That is your friend %s, %.2f km away",
								p.getNickName(), distanceInKilometers));
			} else if (data instanceof PlaceMark) {
				PlaceMark pm = (PlaceMark) data;
				getWindow().showNotification(
						String.format("That is %s, %.2f km away", pm.getName(),
								distanceInKilometers));
			}
		}
		vl.setSelectedVector(null);
	}

}
