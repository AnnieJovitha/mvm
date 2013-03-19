package org.vaadin.mvm.gwt;

import java.util.ArrayList;
import java.util.Collection;

import org.vaadin.addon.leaflet.client.vaadin.LeafletMapConnector;
import org.vaadin.addon.leaflet.client.vaadin.LeafletMarkerConnector;
import org.vaadin.addon.leaflet.client.vaadin.LeafletPolylineConnector;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.vaadin.addon.touchkit.gwt.client.vcom.EmailFieldConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.GeolocatorConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.HorizontalButtonGroupConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.NumberFieldConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.OfflineModeConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.SwitchConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.TabBarConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.VerticalComponentGroupConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.navigation.NavigationBarConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.navigation.NavigationViewConnector;
import com.vaadin.addon.touchkit.gwt.client.vcom.popover.PopoverConnector;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.client.ui.csslayout.CssLayoutConnector;
import com.vaadin.client.ui.customfield.CustomFieldConnector;
import com.vaadin.client.ui.image.ImageConnector;
import com.vaadin.client.ui.label.LabelConnector;
import com.vaadin.client.ui.optiongroup.OptionGroupConnector;
import com.vaadin.client.ui.progressindicator.ProgressIndicatorConnector;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.client.ui.upload.UploadConnector;
import com.vaadin.server.widgetsetutils.ConnectorBundleLoaderFactory;

public class OptimizedWidgetBundle extends ConnectorBundleLoaderFactory {

	private final ArrayList<String> eagerWidgets;

	public OptimizedWidgetBundle() {
		eagerWidgets = new ArrayList<String>();
		eagerWidgets.add(SwitchConnector.class.getName());
		eagerWidgets.add(NumberFieldConnector.class.getName());
		eagerWidgets.add(EmailFieldConnector.class.getName());
		eagerWidgets.add(CssLayoutConnector.class.getName());
		eagerWidgets.add(NavigationViewConnector.class.getName());
		eagerWidgets.add(TabBarConnector.class.getName());
		eagerWidgets.add(NavigationBarConnector.class.getName());
		eagerWidgets.add(ButtonConnector.class.getName());
		eagerWidgets.add(PopoverConnector.class.getName());
		eagerWidgets.add(LeafletMapConnector.class.getName());
		eagerWidgets.add(LeafletMarkerConnector.class.getName());
		eagerWidgets.add(LeafletPolylineConnector.class.getName());
		eagerWidgets.add(LabelConnector.class.getName());
		eagerWidgets.add(VerticalComponentGroupConnector.class.getName());
		eagerWidgets.add(TextFieldConnector.class.getName());
		eagerWidgets.add(OptionGroupConnector.class.getName());
		eagerWidgets.add(UploadConnector.class.getName());
		eagerWidgets.add(GeolocatorConnector.class.getName());
		eagerWidgets.add(HorizontalButtonGroupConnector.class.getName());
		eagerWidgets.add(OfflineModeConnector.class.getName());
		eagerWidgets.add(CustomFieldConnector.class.getName());
		eagerWidgets.add(ImageConnector.class.getName());
		eagerWidgets.add(ProgressIndicatorConnector.class.getName());
		eagerWidgets.add(UIConnector.class.getName());
	}

	@Override
	protected Collection<JClassType> getConnectorsForWidgetset(
			TreeLogger logger, TypeOracle typeOracle)
			throws UnableToCompleteException {
		// Strip out everything else but necessary stuff
		// FIXME, this does not work properly. E.g. RPC stuff of all components
		// on classpath is still there
		Collection<JClassType> connectorsForWidgetset = super
				.getConnectorsForWidgetset(logger, typeOracle);
		ArrayList<JClassType> arrayList = new ArrayList<JClassType>();
		for (JClassType jClassType : connectorsForWidgetset) {
			String qualifiedSourceName = jClassType.getQualifiedSourceName();
			if (eagerWidgets.contains(qualifiedSourceName)) {
				arrayList.add(jClassType);
			}
		}
		return arrayList;
	}

}
