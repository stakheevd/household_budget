package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Login")
public class LoginView extends VerticalLayout implements BeforeEnterListener {

    private LoginForm login = new LoginForm();

    Button articlesButton = new Button("Articles", e -> { UI.getCurrent().getPage().setLocation("/articles"); });
    Button operationsButton = new Button("Operations", e -> { UI.getCurrent().getPage().setLocation("/"); });
    Button balancesButton = new Button("Balances", e -> { UI.getCurrent().getPage().setLocation("/balances"); });

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        login.setAction("login");

        login.setForgotPasswordButtonVisible(false);

        add(
                new H1("Household budget"),
                login,
                configureButton()
        );
    }

    private Component configureButton() {
        return new HorizontalLayout(articlesButton,
                operationsButton,
                balancesButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")){
            login.setError(true);
        }
    }
}
