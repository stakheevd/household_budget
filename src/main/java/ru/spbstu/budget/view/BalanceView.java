package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import ru.spbstu.budget.controllers.BudgetController;
import ru.spbstu.budget.models.Article;
import ru.spbstu.budget.models.Balance;
import ru.spbstu.budget.security.SecurityService;

import java.util.Comparator;
import java.util.stream.Collectors;

@Route(value = "balances", layout = GlobalLayout.class)
@PageTitle("Balances")
@AnonymousAllowed
public class BalanceView extends VerticalLayout {
    private final BudgetController budgetController;
    private BalanceForm balanceForm;
    private Grid<Balance> grid = new Grid<>(Balance.class);

    ComboBox<Integer> filterText = new ComboBox<>();
    ComboBox<Article> filterTextByArticle = new ComboBox<>();

    Label articleLabel = new Label();

    @Autowired
    public BalanceView(BudgetController budgetController) {
        this.budgetController = budgetController;

        addClassName("budget-view");
        setSizeFull();

        configureGrid();
        configureForm();

        add(getToolbar(), getContent());

        updateBalancesList();
        closeEditor();
    }

    private Component getContent() {
        HorizontalLayout content = new HorizontalLayout(grid, balanceForm);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, balanceForm);
        content.addClassName("content");
        content.setSizeFull();

        return content;
    }

    private Component getToolbar() {

        fillIdCombobox();

        filterText.setAllowCustomValue(true);
        filterText.setClearButtonVisible(true);
        filterText.setPlaceholder("Find by id...");
        filterText.setWidth("8em");
        filterText.addValueChangeListener((HasValue.ValueChangeEvent<Integer> event) -> { updateBalancesList(); });

        filterTextByArticle.setItems(budgetController.articles().stream().
                distinct().
                sorted(Comparator.comparing(Article::getId)).
                collect(Collectors.toList()));

        filterTextByArticle.setItemLabelGenerator(Article::getLabel);
        filterTextByArticle.setAllowCustomValue(true);
        filterTextByArticle.setClearButtonVisible(true);
        filterTextByArticle.setPlaceholder("Filter by article name...");
        filterTextByArticle.setWidth("13em");
        filterTextByArticle.addValueChangeListener((HasValue.ValueChangeEvent<Article> event) -> {
            if (event.getValue() == null) {
                articleLabel.setText("");
            }
            else
            {
                articleLabel.setText(event.getValue().getName());
            }

            updateBalancesListByArticle();
        });


        Button addBalanceButton = new Button("Add balance");
        addBalanceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBalanceButton.addClickListener(event -> addBalance());

        Button profitableBalanceButton = new Button("Most profitable");
        profitableBalanceButton.addClickListener(event -> showProfitableBalance());

        Button unprofitableBalanceButton = new Button("Most unprofitable");
        unprofitableBalanceButton.addClickListener(event -> showUnprofitableBalance());

        Button lastBalanceButton = new Button("Last");
        lastBalanceButton.addClickListener(event -> showLastBalance());

        HorizontalLayout toolBar;

        if (SecurityService.isAuthenticated()) {
            toolBar = new HorizontalLayout(
                    filterText,
                    filterTextByArticle,
                    profitableBalanceButton,
                    unprofitableBalanceButton,
                    lastBalanceButton,
                    addBalanceButton);
        }
        else
        {
            toolBar = new HorizontalLayout(
                    filterText,
                    filterTextByArticle,
                    profitableBalanceButton,
                    unprofitableBalanceButton,
                    lastBalanceButton);
        }



        toolBar.addClassName("toolbar");
        return toolBar;
    }

    private void fillIdCombobox() {
        filterText.setItems(budgetController.balances().stream()
                .sorted(Comparator.comparing(Balance::getId))
                .distinct()
                .map(Balance::getId)
                .collect(Collectors.toList()));
    }

    private void updateBalancesListByArticle() {
        grid.setItems(budgetController.balancesByArticle(articleLabel.getText()));
    }

    private void showLastBalance() {
        filterText.setValue(budgetController.lastBalance().get(0).getId());
        updateBalancesList();
    }

    private void showUnprofitableBalance() {
        filterText.setValue(budgetController.unprofitableBalance().get(0).getId());
        updateBalancesList();
    }

    private void showProfitableBalance() {
        filterText.setValue(budgetController.profitableBalance().get(0).getId());
        updateBalancesList();
    }

    private void addBalance() {
        grid.asSingleSelect().clear();
        editBalance(new Balance());

        balanceForm.balanceDateTimePicker.clear();
        balanceForm.balanceDebit.clear();
        balanceForm.balanceCredit.clear();
    }

    private void configureGrid() {
        grid.addClassName("balance-grid");
        grid.setSizeFull();

        grid.setColumns("id", "create_date", "debit", "credit", "amount");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        if (SecurityService.isAuthenticated()) {
            grid.asSingleSelect().addValueChangeListener(event -> editBalance(event.getValue()));
        }
    }

    private void editBalance(Balance balance) {
        if (balance == null) {
            closeEditor();
        }
        else
        {
            balanceForm.setBalance(balance);
            balanceForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void closeEditor() {
        balanceForm.setBalance(null);
        balanceForm.setVisible(false);
        removeClassName("editing");
    }

    private void configureForm() {
        balanceForm = new BalanceForm(budgetController);
        balanceForm.setWidth("25em");

        balanceForm.addListener(BalanceForm.SaveEvent.class, this::saveBalance);
        balanceForm.addListener(BalanceForm.DeleteEvent.class, this::deleteBalance);
        balanceForm.addListener(BalanceForm.CloseEvent.class, event -> closeEditor());
    }

    private void saveBalance(BalanceForm.SaveEvent event) {
        budgetController.saveBalance(event.getBalance());
        fillIdCombobox();
        updateBalancesList();
        closeEditor();
    }

    private void deleteBalance(BalanceForm.DeleteEvent event) {
        budgetController.removeBalance(event.getBalance());
        updateBalancesList();
        fillIdCombobox();
        closeEditor();
    }

    private void updateBalancesList() { grid.setItems(budgetController.balancesById(filterText.getValue())); }
}
