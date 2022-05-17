package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import ru.spbstu.budget.controllers.BudgetController;
import ru.spbstu.budget.models.Article;
import ru.spbstu.budget.models.Balance;
import ru.spbstu.budget.models.Operation;
import ru.spbstu.budget.security.SecurityService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Route(value = "", layout = GlobalLayout.class)
@PageTitle("Home")
@AnonymousAllowed
public class MainView extends VerticalLayout {
    private final BudgetController budgetController;
    private OperationForm operationForm;
    private Grid<Operation> grid = new Grid<>(Operation.class);

    ComboBox<Integer> filterText = new ComboBox<>();
    ComboBox<Balance> filterTextBalanceId = new ComboBox<>();
    ComboBox<Article> names = new ComboBox<>();
    Label articleLabel = new Label("");
    IntegerField balanceLabel = new IntegerField("");
    ComboBox<Article> deleteField = new ComboBox<>();
    Button deleteOperationsByNameButton = new Button(new Icon(VaadinIcon.TRASH));

    @Autowired
    public MainView(BudgetController budgetController) {
        this.budgetController = budgetController;

        addClassName("list-view");
        setSizeFull();

        configureGrid();
        configureForm();
        
        add(getToolbar(), getContent());

        updateOperationsList();
        closeEditor();
    }

    private void closeEditor() {
        operationForm.setOperation(null);
        operationForm.setVisible(false);
        removeClassName("editing");
    }

    private void configureForm() {
        operationForm = new OperationForm(budgetController.articles().stream()
                .sorted(Comparator.comparing(Article::getId))
                .distinct()
                .map(Article::getId)
                .collect(Collectors.toList()),
                budgetController.balances()
                        .stream()
                        .sorted(Comparator.comparing(Balance::getId))
                        .distinct()
                        .map(Balance::getId)
                        .collect(Collectors.toList()),
                budgetController);
        operationForm.setWidth("25em");

        operationForm.addListener(OperationForm.SaveEvent.class, this::saveOperation);
        operationForm.addListener(OperationForm.DeleteEvent.class, this::deleteOperation);
        operationForm.addListener(OperationForm.CloseEvent.class, event -> closeEditor());
    }

    private void saveOperation(OperationForm.SaveEvent event) {
        budgetController.saveOperation(event.getOperation());
        updateOperationsList();
        fillIdComboBox();
        fillDeleteComboBox();
        closeEditor();
    }

    private void deleteOperation(OperationForm.DeleteEvent event) {
        budgetController.removeOperation(event.getOperation());
        updateOperationsList();
        fillIdComboBox();
        fillDeleteComboBox();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, operationForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, operationForm);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    private Component getToolbar() {

        fillIdComboBox();

        filterText.setAllowCustomValue(true);
        filterText.setClearButtonVisible(true);
        filterText.setPlaceholder("Find by id...");
        filterText.setWidth("8em");
        filterText.addValueChangeListener((HasValue.ValueChangeEvent<Integer> event) -> { updateOperationsList(); });

        filterTextBalanceId.setItems(budgetController.balances().stream().
                distinct().
                sorted(Comparator.comparing(Balance::getId)).
                collect(Collectors.toList()));

        filterTextBalanceId.setItemLabelGenerator(Balance::getLabel);
        filterTextBalanceId.setAllowCustomValue(true);
        filterTextBalanceId.setClearButtonVisible(true);
        filterTextBalanceId.setPlaceholder("Find by balance...");
        filterTextBalanceId.setWidth("13em");
        filterTextBalanceId.addValueChangeListener((HasValue.ValueChangeEvent<Balance> event) -> {
            if (event.getValue() == null) {
                balanceLabel.setValue(-1);
            }
            else
            {
                balanceLabel.setValue(event.getValue().getId());
            }

            updateOperationsListByBalanceId();
        });


        names.setItems(budgetController.articles().stream().
                distinct().
                sorted(Comparator.comparing(Article::getId)).
                collect(Collectors.toList()));

        names.setItemLabelGenerator(Article::getLabel);
        names.setAllowCustomValue(true);
        names.setClearButtonVisible(true);
        names.setPlaceholder("Filter by article name...");
        names.setWidth("13em");
        names.addValueChangeListener((HasValue.ValueChangeEvent<Article> event) -> {
            if (event.getValue() == null) {
                articleLabel.setText("");
            }
            else
            {
                articleLabel.setText(event.getValue().getName());
            }

            updateOperationsListByArticleName();
        });

        Button addOperationButton = new Button("Add operation");
        addOperationButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addOperationButton.addClickListener(event -> addOperation());

        Button profitableOperationButton = new Button("Most profitable");
        profitableOperationButton.addClickListener(event -> showProfitableOperation());

        Button unprofitableOperationButton = new Button("Most unprofitable");
        unprofitableOperationButton.addClickListener(event -> showUnprofitableOperation());

        Button lastOperationButton = new Button("Last");
        lastOperationButton.addClickListener(event -> showLastOperation());

        fillDeleteComboBox();

        deleteField.setItemLabelGenerator(Article::getLabel);
        deleteField.setAllowCustomValue(true);
        deleteField.setClearButtonVisible(true);
        deleteField.setHelperText("Delete operations by name");
        deleteField.setPlaceholder("Name...");

        deleteOperationsByNameButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteOperationsByNameButton.addClickListener(e -> deleteOperationsByName());

        HorizontalLayout toolBar;

        if (SecurityService.isAuthenticated()) {
            toolBar = new HorizontalLayout(
                    filterText,
                    filterTextBalanceId,
                    names,
                    profitableOperationButton,
                    unprofitableOperationButton,
                    lastOperationButton,
                    addOperationButton,
                    deleteField,
                    deleteOperationsByNameButton);
        }
        else
        {
            toolBar = new HorizontalLayout(
                    filterText,
                    filterTextBalanceId,
                    names,
                    profitableOperationButton,
                    unprofitableOperationButton,
                    lastOperationButton);
        }

        toolBar.addClassName("toolbar");
        return toolBar;
    }

    private void fillDeleteComboBox() {
        Set<Integer> operationsByArticlesId =
                budgetController.operations().stream().
                        distinct()
                        .map(Operation::getArticle_id)
                        .collect(Collectors.toSet());

        List<Article> listOutput =
                budgetController.articles().stream()
                        .filter(e -> operationsByArticlesId.contains(e.getId()))
                        .distinct()
                        .sorted(Comparator.comparing(Article::getId))
                        .collect(Collectors.toList());

        deleteField.setItems(listOutput);
    }

    private void fillIdComboBox() {
        filterText.setItems(budgetController.operations().stream()
                .sorted(Comparator.comparing(Operation::getId))
                .distinct()
                .map(Operation::getId)
                .collect(Collectors.toList()));
    }

    private void updateOperationsListByArticleName() {
        grid.setItems(budgetController.showOperationsByArticleName(articleLabel.getText()));
    }

    private void updateOperationsListByBalanceId() {
        grid.setItems(budgetController.showOperationsByBalance(balanceLabel.getValue()));
    }

    private void showLastOperation() {
        filterText.setValue(budgetController.lastOperation().get(0).getId());
        updateOperationsList();
    }

    private void showUnprofitableOperation() {
        filterText.setValue(budgetController.unprofitableOperation().get(0).getId());
        updateOperationsList();
    }

    private void showProfitableOperation() {
        filterText.setValue(budgetController.profitableOperation().get(0).getId());
        updateOperationsList();
    }


    private void addOperation() {
        grid.asSingleSelect().clear();
        editOperation(new Operation());

        operationForm.operationArticleId.clear();
        operationForm.operationDebit.clear();
        operationForm.operationCredit.clear();
        operationForm.operationBalanceId.clear();
        operationForm.operationDateTimePicker.clear();
    }


    private void configureGrid() {
        grid.addClassName("operation-grid");
        grid.setSizeFull();

        grid.setColumns("id", "article_id", "debit", "credit", "create_date", "balance_id");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        if (SecurityService.isAuthenticated()) {
            grid.asSingleSelect().addValueChangeListener(event -> editOperation(event.getValue()));
        }
    }

    private void editOperation(Operation operation) {
        if(operation == null) {
            closeEditor();
        }
        else
        {
            operationForm.setOperation(operation);
            operationForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void updateOperationsList() {
        grid.setItems(budgetController.operationsById(filterText.getValue()));
    }

    private void deleteOperationsByName() {
        if (!deleteField.isEmpty()) {
            budgetController.deleteOperationsByArticle(deleteField.getValue().getName());
            updateOperationsList();
            fillIdComboBox();
            fillDeleteComboBox();
        }
    }

}
