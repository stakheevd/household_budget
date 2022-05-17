package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import ru.spbstu.budget.controllers.BudgetController;
import ru.spbstu.budget.models.Article;
import ru.spbstu.budget.models.Operation;
import ru.spbstu.budget.security.SecurityService;

import java.util.Comparator;
import java.util.stream.Collectors;

@Route(value = "articles", layout = GlobalLayout.class)
@PageTitle("Articles")
@AnonymousAllowed
public class ArticleView extends VerticalLayout {

    private final BudgetController budgetController;
    private ArticleForm articleForm;
    private Grid<Article> grid = new Grid<>(Article.class);

    ComboBox<Integer> idComboBox = new ComboBox<>();
    TextField filterText = new TextField();
    ComboBox<Integer> operationIdField = new ComboBox<>();
    NumberField creditField = new NumberField();
    NumberField debitField = new NumberField();

    Button addArticleButton = new Button("Add article");

    IntegerField operationIdLabel = new IntegerField();

    @Autowired
    public ArticleView(BudgetController budgetController) {
        this.budgetController = budgetController;

        addClassName("articles-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(getToolbar(), getContent());

        updateArticlesList();
        closeEditor();
    }

    private void closeEditor() {
        articleForm.setArticle(null);
        articleForm.setVisible(false);
        removeClassName("editing");
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, articleForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, articleForm);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    private Component getToolbar() {

        fillComboBox();


        idComboBox.setAllowCustomValue(true);
        idComboBox.setClearButtonVisible(true);
        idComboBox.setPlaceholder("Find by id...");
        idComboBox.setWidth("8em");
        idComboBox.addValueChangeListener((HasValue.ValueChangeEvent<Integer> event) -> { updateArticlesListById(); });

        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateArticlesList());

        debitField.setPlaceholder("Debit more...");
        debitField.setClearButtonVisible(true);
        debitField.setValueChangeMode(ValueChangeMode.LAZY);
        debitField.addValueChangeListener(e -> updateArticlesListByDebit());


        creditField.setPlaceholder("Credit more...");
        creditField.setClearButtonVisible(true);
        creditField.setValueChangeMode(ValueChangeMode.LAZY);
        creditField.addValueChangeListener(e -> updateArticlesListByCredit());


        operationIdField.setPlaceholder("Find by operation...");
        operationIdField.setClearButtonVisible(true);

        operationIdField.setItems(budgetController.operations().stream()
                .sorted(Comparator.comparing(Operation::getId))
                .distinct()
                .map(Operation::getId)
                .collect(Collectors.toList()));

        operationIdField.addValueChangeListener((HasValue.ValueChangeEvent<Integer> event) -> {
            if (event.getValue() == null) {
                operationIdLabel.setValue(-1);
            }
            else
            {
                operationIdLabel.setValue(event.getValue());
            }

            updateArticlesListByOperationId();
        });

        addArticleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addArticleButton.addClickListener(event -> addArticle());

        HorizontalLayout toolBar;

        if (SecurityService.isAuthenticated()) {
            toolBar = new HorizontalLayout(
                    idComboBox,
                    filterText,
                    debitField,
                    creditField,
                    operationIdField,
                    addArticleButton);
        }
        else
        {
            toolBar = new HorizontalLayout(
                    idComboBox,
                    filterText,
                    debitField,
                    creditField,
                    operationIdField);
        }

        toolBar.addClassName("toolbar");
        return toolBar;
    }

    private void fillComboBox() {
        idComboBox.setItems(budgetController.articles().stream()
                .sorted(Comparator.comparing(Article::getId))
                .distinct()
                .map(Article::getId)
                .collect(Collectors.toList()));
    }

    private void updateArticlesListById() { grid.setItems(budgetController.articleByd(idComboBox.getValue())); }

    private void updateArticlesListByDebit() {
        grid.setItems(budgetController.articlesByDebit(debitField.getValue()));
    }

    private void updateArticlesListByCredit() {
        grid.setItems(budgetController.articlesByCredit(creditField.getValue()));
    }

    private void updateArticlesListByOperationId() {
        grid.setItems(budgetController.searchArticle(operationIdLabel.getValue()));
    }

    private void addArticle() {
        grid.asSingleSelect().clear();
        editArticle(new Article());

        articleForm.articleName.clear();
    }

    private void editArticle(Article article) {
        if (article == null) {
            closeEditor();
        }
        else
        {
            articleForm.setArticle(article);
            articleForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void configureForm() {
        articleForm = new ArticleForm(budgetController);
        articleForm.setWidth("25em");

        articleForm.addListener(ArticleForm.SaveEvent.class, this::saveArticle);
        articleForm.addListener(ArticleForm.DeleteEvent.class, this::deleteArticle);
        articleForm.addListener(ArticleForm.CloseEvent.class, event -> closeEditor());
    }

    private void saveArticle(ArticleForm.SaveEvent event) {
        budgetController.saveArticle(event.getArticle());
        updateArticlesList();
        fillComboBox();
        closeEditor();
    }

    private void deleteArticle(ArticleForm.DeleteEvent event) {
        budgetController.removeArticle(event.getArticle());
        updateArticlesList();
        fillComboBox();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassName("grid");
        grid.setSizeFull();

        grid.setColumns("id", "name");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        if (SecurityService.isAuthenticated()) {
            grid.asSingleSelect().addValueChangeListener(event -> editArticle(event.getValue()));
        }
    }

    private void updateArticlesList() { grid.setItems(budgetController.articlesByName(filterText.getValue())); }
}
