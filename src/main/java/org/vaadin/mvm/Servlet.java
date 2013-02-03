package org.vaadin.mvm;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.vaadin.mvm.domain.Person;

import com.vaadin.addon.touchkit.server.TouchKitServlet;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;

@WebServlet("/*")
public class Servlet extends TouchKitServlet {

	public static final String MVM_COOKIE_NAME = "mvmuser";
	
    private MyUIProvider uiProvider = new MyUIProvider();
    
    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException {
                event.getSession().addUIProvider(uiProvider);
            }
        });
    }
    
    @Override
    protected void service(HttpServletRequest request,
    		HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if(!pathInfo.contains("VAADIN/")) {
			HttpSession session = request.getSession();
			
			if (session.getAttribute("user") == null) {
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
				session.setAttribute("user", user);
			}			
		}
    	super.service(request, response);
    }


}
