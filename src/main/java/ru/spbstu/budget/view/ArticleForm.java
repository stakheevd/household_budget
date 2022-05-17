package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import ru.spbstu.budget.controllers.BudgetController;
import ru.spbstu.budget.models.Article;

import java.util.stream.Collectors;

public class ArticleForm extends FormLayout {

    Binder<Article> binder = new BeanValidationBinder<>(Article.class);

    IntegerField articleId = new IntegerField("Article id");
    TextField articleName = new TextField("Article name");

    Label nameStatus = new Label();

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Close");

    private Article article;
    private BudgetController budgetController;

    @Autowired
    public ArticleForm(BudgetController budgetController) {
        this.budgetController = budgetController;
        addClassName("article-form");

        binder.bind(articleId, Article::getId, Article::setId);
        binder.forField(articleName)
                .asRequired()
                .withValidator(
                        name -> name.length() <= 50,
                        "The name must be less than 50 characters")
                .withValidator(name -> !(isExists(name)),
                        "Such an article already exists")
                .bind(Article::getName, Article::setName);

        VerticalLayout verticalLayout = new VerticalLayout();
        articleName.setSizeFull();
        articleName.setClearButtonVisible(true);
        articleName.setValueChangeMode(ValueChangeMode.EAGER);
        nameStatus.getStyle().set("color", "red");

        verticalLayout.add(
                articleName,
                nameStatus,
                createButtonsLayout()
        );

        add(verticalLayout);
    }

    private boolean isExists(String name) {
        return budgetController.articles().stream()
                .distinct()
                .map(Article::getName)
                .collect(Collectors.toList()).contains(name);
    }

    public void setArticle(Article article) {
        this.article = article;
        binder.readBean(article);
    }

    private Component createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> validateAndDelete());
        close.addClickListener(event -> fireEvent(new CloseEvent(this)));

        save.addClickShortcut(Key.ENTER);
        close.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(save, delete, close);
    }

    private void validateAndDelete() {
        try {
            fireEvent(new DeleteEvent(this, article));
        } catch (Exception e) {
            nameStatus.setText("Incorrect value");
        }
    }

    private void validateAndSave() {
        try {
            binder.writeBean(article);
            fireEvent(new SaveEvent(this, article));
        } catch (ValidationException e) {
            nameStatus.setText("Incorrect value");
        }

    }

    public static abstract class ArticleFormEvent extends ComponentEvent<ArticleForm> {
        private Article article;

        protected ArticleFormEvent(ArticleForm source, Article article) {
            super(source, false);
            this.article = article;
        }

        public Article getArticle() {
            return article;
        }
    }

    public static class SaveEvent extends ArticleForm.ArticleFormEvent {
        SaveEvent(ArticleForm source, Article article) {
            super(source, article);
        }
    }

    public static class DeleteEvent extends ArticleForm.ArticleFormEvent {
        DeleteEvent(ArticleForm source, Article article) {
            super(source, article);
        }

    }

    public static class CloseEvent extends ArticleForm.ArticleFormEvent {
        CloseEvent(ArticleForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
