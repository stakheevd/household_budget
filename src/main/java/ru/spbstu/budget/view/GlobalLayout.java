package ru.spbstu.budget.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightCondition;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import ru.spbstu.budget.security.SecurityService;

public class GlobalLayout extends AppLayout {

    private SecurityService securityService;

    public GlobalLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H2 logo = new H2("Household budget");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header;

        if (SecurityService.isAuthenticated()) {
            Button logout = new Button("Log out", click ->
                    securityService.logout());
            logout.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            header = new HorizontalLayout(new DrawerToggle(), logo, logout);
        }
        else
        {
            Button login = new Button("Log in", e -> { UI.getCurrent().getPage().setLocation("/login"); });
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            header = new HorizontalLayout(new DrawerToggle(), logo, login);
        }

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }
    private void createDrawer() {
        RouterLink mainView = new RouterLink("Operations", MainView.class);
        mainView.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
             mainView,
             new RouterLink("Balances", BalanceView.class),
             new RouterLink("Articles", ArticleView.class)
        ));
    }
}
