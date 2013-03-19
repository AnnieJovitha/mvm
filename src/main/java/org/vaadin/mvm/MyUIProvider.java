package org.vaadin.mvm;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

public class MyUIProvider extends UIProvider {
    
    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
        if(userAgent.contains("webkit") || userAgent.contains("msie 10")) {
        	return MobileVaadinMaps.class;
        } else {
            return MyFallbackUI.class;
        }
    }

}
