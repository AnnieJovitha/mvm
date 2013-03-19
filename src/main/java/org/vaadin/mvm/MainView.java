package org.vaadin.mvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.addon.leaflet.LeafletClickEvent;
import org.vaadin.addon.leaflet.LeafletClickListener;
import org.vaadin.addon.leaflet.LMap;
import org.vaadin.addon.leaflet.LMarker;
import org.vaadin.addon.leaflet.LPolyline;
import org.vaadin.addon.leaflet.shared.BaseLayer;
import org.vaadin.addon.leaflet.shared.Bounds;
import org.vaadin.addon.leaflet.shared.Point;
import org.vaadin.mvm.domain.Person;
import org.vaadin.mvm.domain.PlaceMark;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.vaadin.addon.touchkit.extensions.Geolocator;
import com.vaadin.addon.touchkit.extensions.PositionCallback;
import com.vaadin.addon.touchkit.gwt.client.vcom.Position;
import com.vaadin.addon.touchkit.ui.HorizontalButtonGroup;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressIndicator;

public class MainView extends NavigationView implements
		ClickListener, LeafletClickListener, PositionCallback {

	private static final ThemeResource SETTINGS_ICON = new ThemeResource("settings_g.png");
	private static final int MAX_POINTS = 100;
	private boolean keepTracking = false;
	private boolean drawRoute = false;
	private List<Point> lastPoints = new LinkedList<Point>();

	private Map<Person, LMarker> displayedPersons = new HashMap<Person, LMarker>();
	private Map<PlaceMark, LMarker> displayedPlaces = new HashMap<PlaceMark, LMarker>();

	private LMap map = new LMap();

	private LMarker myPlace = new LMarker();
	private HorizontalButtonGroup mapTools = new HorizontalButtonGroup();
	private Button optionsButton = new Button("⚒", this);
	private Button locateButton = new Button("ʘ", this);
	private Button addPlacemark = new Button("⚑", this);
	private OptionsDialog settings = new OptionsDialog(this);

	private ProgressIndicator progressIndicator = new ProgressIndicator();

	private CssLayout content = new CssLayout() {
		protected String getCss(com.vaadin.ui.Component c) {
			if (c == progressIndicator) {
				return "margin-left:-2000px;overflow:hidden;";
			}
			return "position:absolute;top:0;right:0;left:0;bottom:0;";
		};
	};

	public MainView() {
		setCaption("MVM");
		content.addStyleName("mainview");
		setSizeFull();
		map.setSizeFull();

		content.setSizeFull();
		content.addComponent(map);
		setContent(content);
		setRightComponent(optionsButton);

		mapTools.addComponent(locateButton);
		mapTools.addComponent(addPlacemark);
		setLeftComponent(mapTools);

//		progressIndicator.setPollingInterval(5000);
//		content.addComponent(progressIndicator);

		/*
		 * Android font don't contain all cool unicode characters, as a fallback
		 * use graphics
		 */
		optionsButton.setCaption(null);
		optionsButton.setIcon(SETTINGS_ICON);
		addPlacemark.setCaption(null);
		addPlacemark.setIcon(new ThemeResource("placemark_g.png"));
		myPlace.addClickListener(this);
	}

	private List<BaseLayer> availableLayers = new ArrayList<BaseLayer>();
	private BaseLayer currentLayer;
	private LPolyline routeVector;
	private boolean automaticDetection;

	private void defineBaseLayers() {
		
		BaseLayer baselayer = new BaseLayer();
		baselayer.setName("CloudMade");

		// Note, this url should only be used for testing purposes. If you wish
		// to use cloudmade base maps, get your own API key.
		baselayer
				.setUrl("http://{s}.tile.cloudmade.com/a751804431c2443ab399100902c651e8/997/256/{z}/{x}/{y}.png");

		// OSM layer as default
		availableLayers.add(baselayer);

		// For users in Finland, some detailed terrain maps by MML, hosted by
		// kapsi
		setCurrentLayer(baselayer);
		
		baselayer = new BaseLayer();
		baselayer.setName("Peruskartta");
		baselayer.setUrl(Servlet.contextPath + "/tiles/peruskartta/{z}/{x}/{y}.png");
		baselayer.setAttributionString("Maanmittauslaitos, hosted by kartat.kapsi.fi");
		baselayer.setMaxZoom(18);
		baselayer.setSubDomains("tile2");
		baselayer.setDetectRetina(true);
		availableLayers.add(baselayer);

		baselayer = new BaseLayer();
		baselayer.setName("Ortokuva");
		baselayer.setUrl(Servlet.contextPath + "/tiles/ortokuva/{z}/{x}/{y}.png");
		baselayer.setAttributionString("Maanmittauslaitos, hosted by kartat.kapsi.fi");
		baselayer.setMaxZoom(18);
		baselayer.setSubDomains("tile2");
		availableLayers.add(baselayer);
		
	}

	public List<BaseLayer> getAvailableLayers() {
		return availableLayers;
	}

	public void setCurrentLayer(BaseLayer l) {
		map.setBaseLayers(l);
		currentLayer = l;
		int indexOf = availableLayers.indexOf(l);
		Person user = getUser();
		user.setLastLayerIndex(indexOf);
	}

	public BaseLayer getCurrentLayer() {
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
		Geolocator.detect(this);
	}

	public Person getUser() {
		return MobileVaadinMaps.getUser();
	}

	public void onSuccess(Position position) {
		getUser().setAccuracy(position.getAccuracy());
		getUser().setLastLocation(
				new Point(position.getLatitude(),position.getLongitude()));
		updateMap();
	}
	
	public List<Point> getLastPoints() {
		return lastPoints;
	}

	public void updateMap() {
		lastPoints.add(getUser().getLastLocation());
		if (lastPoints.size() > MAX_POINTS) {
			lastPoints.remove(0);
		}
		myPlace.setPoint(getUser().getLastLocation());
		if (myPlace.getParent() == null) {
			map.addComponent(myPlace);
			map.setZoomLevel(10);
		}
		if (keepTracking && isTrackingEnabled()) {
			// TODO TouchKit should support continuous tracking
			
			final VaadinSession session = getSession();
			new Thread() {
				public void run() {
					try {
						Thread.sleep(5000);
						session.lock();
						detectPosition(true);
					} catch (InterruptedException e) {
						
					} finally {
						session.unlock();
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
		Collection<LMarker> values = displayedPersons.values();
		for (LMarker LeafletMarker : values) {
			bounds.extend(LeafletMarker.getPoint());
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
				routeVector = new LPolyline();
				map.addComponent(routeVector);
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
			if (drawing) {
				disableDrawingMode();
			} else {
				enableDrawingMode();
			}
		}
	}
	
	LeafletClickListener drawingMode = new LeafletClickListener() {
		
		@Override
		public void onClick(LeafletClickEvent event) {
			new PlaceMarkEditor(MainView.this, addPlacemark, event.getPoint());
			disableDrawingMode();
		}
	};
	private boolean drawing = false;

	private void enableDrawingMode() {
		addPlacemark.addStyleName("green");
		map.addClickListener(drawingMode);
		drawing = true;
	}

	private void disableDrawingMode() {
		addPlacemark.removeStyleName("green");
		map.removeClickListener(drawingMode);
		drawing = false;
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
			LMarker marker = new LMarker();
			marker.addClickListener(this);
//			LeafletMarker.setRenderIntent("other");
			marker.setPoint(p.getLastLocation());
			marker.setData(p);
			displayedPersons.put(p, marker);
			map.addComponent(marker);
		}
	}

	public void removeDisplayedPersons(Person person) {
		LMarker m = displayedPersons.remove(person);
		map.removeComponent(m);
	}

	public Map<PlaceMark, LMarker> getDisplayedPlaces() {
		return displayedPlaces;
	}

	public void addDisplayedPlaceMark(PlaceMark pm) {
		LMarker marker = new LMarker();
		marker.addClickListener(this);
		marker.setPoint(new Point(pm.getLat(), pm.getLon()));
		marker.setData(pm);
		displayedPlaces.put(pm, marker);
		map.addComponent(marker);
	}

	public void removeDisplayedPlaceMark(PlaceMark pm) {
		LMarker remove = displayedPlaces.remove(pm);
		if (remove != null) {
			map.removeComponent(remove);
		}
	}

	public void setDisplayedPlaces(Map<PlaceMark, LMarker> displayedPlaces) {
		this.displayedPlaces = displayedPlaces;
	}

	private void showFriendDetails(LMarker vector, Object data) {
		LatLng point1 = new LatLng(myPlace.getPoint().getLat(), myPlace
				.getPoint().getLon());
		LatLng point2 = new LatLng(vector.getPoint().getLat(),
				vector.getPoint().getLon());
		double distanceInKilometers = LatLngTool.distance(point1, point2,
				LengthUnit.KILOMETER);
		
		Person p = (Person) data;
		Notification.show(
				String.format("That is your friend %s, %.2f km away",
						p.getNickName(), distanceInKilometers));
	}

	@Override
	public void onClick(LeafletClickEvent event) {
		LMarker marker = (LMarker) event.getConnector();
		if (marker == myPlace) {
			Notification.show("That is you!");
		} else {
			Object data = marker.getData();
			if (data instanceof Person) {
				showFriendDetails(marker, data);
			} else if (data instanceof PlaceMark) {
				new PlaceMarkEditor(this, addPlacemark, (PlaceMark) data);
			}
		}
		
	}

}
