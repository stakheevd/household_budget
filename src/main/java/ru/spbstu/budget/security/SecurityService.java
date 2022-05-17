package ru.spbstu.budget.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {
    public static boolean isAuthenticated() {
        VaadinServletRequest request = VaadinServletRequest.getCurrent();
        return request != null && request.getUserPrincipal() != null;
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation("/");
        SecurityContextLogoutHandler logoutHandler =  new SecurityContextLogoutHandler();

        logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }
}
