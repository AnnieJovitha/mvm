package org.vaadin.mvm;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.vaadin.mvm.domain.Person;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class MobileVaadinMaps extends TouchKitApplication {

	public static final String MVM_COOKIE_NAME = "mvmuser";

	@Override
	public void init() {
		super.init();
		// With these settings iphone 5 uses all estate as home screen web app
		getMainWindow().setCaption("MVM");
		getMainWindow().setViewPortWidth(null);
		getMainWindow().setViewPortMaximumScale(null);
		getMainWindow().setViewPortMinimumScale(null);
		setTheme("mvm");
	}

	@Override
	public void onBrowserDetailsReady() {
		MainView newContent = new MainView();
		getMainWindow().setContent(newContent);
		if (getUser().getLastLocation() != null) {
			newContent.updateMap();
			try {
				newContent.getAvailableLayers().get(
						getUser().getLastLayerIndex());
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Set user from cookie, create new one if needed.
	 */
	@Override
	public void onRequestStart(HttpServletRequest request,
			HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		if(pathInfo.contains("UIDL")) {
			if (getUser() == null) {
				Person user = null;
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						String name = cookie.getName();
						if (name.equals(MVM_COOKIE_NAME)) {
							user = Person.withId(cookie.getValue());
							break;
						}
					}
				}
				if (user == null) {
					user = new Person();
					try {
						// Strip device name from ua as default nick
						String header = request.getHeader("User-Agent");
						String ua = header.substring(header.indexOf("(") + 1,
								header.indexOf(";"));
						user.setNickName(ua + " " + user.getNickName());
					} catch (Exception e) {

					}
					// Note, following will not work with XHR's so we are using
					// BrowserCookie addon in MainView
					 Cookie cookie = new Cookie(MVM_COOKIE_NAME, user.getId());
					 cookie.setMaxAge(60*60*24*365);
					response.addCookie(cookie);
				}
				setUser(user);
			}			
		}
		super.onRequestStart(request, response);
	}

	@Override
	public Person getUser() {
		return (Person) super.getUser();
	}

}
