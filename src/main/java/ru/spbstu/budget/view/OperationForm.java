package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import ru.spbstu.budget.controllers.BudgetController;
import ru.spbstu.budget.models.Operation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OperationForm extends FormLayout {

    Binder<Operation> binder = new BeanValidationBinder<>(Operation.class);

    IntegerField operationId = new IntegerField("Operation id");
    ComboBox<Integer> operationArticleId = new ComboBox<>("Article id");
    NumberField operationDebit = new NumberField("Debit");
    NumberField operationCredit = new NumberField("Credit");

    DateTimePicker operationDateTimePicker = new DateTimePicker("Create date");
    ComboBox<Integer> operationBalanceId = new ComboBox<>("Balance id");
    Label nameStatus = new Label();

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Close");
    private Operation operation;
    private BudgetController budgetController;


    public OperationForm(List<Integer> articles, List<Integer> balances, BudgetController budgetController) {
        this.budgetController = budgetController;
        addClassName("operation-form");

        binder.bind(operationId, Operation::getId, Operation::setId);
        binder.forField(operationArticleId).asRequired().bind(Operation::getArticle_id, Operation::setArticle_id);

        binder.forField(operationDebit)
                .asRequired()
                .withValidator(v -> (v >= 0),
                        "Debit must be non-negative")
                .withValidator(v -> (v <= 9999999999999999.99),
                        "Debit is too large")
                .bind(Operation::getDebit, Operation::setDebit);

        binder.forField(operationCredit)
                .asRequired()
                .withValidator(v -> (v >= 0),
                        "Credit must be non-negative")
                .withValidator(v -> (v <= 9999999999999999.99),
                        "Credit is too large")
                .bind(Operation::getCredit, Operation::setCredit);

        binder.forField(operationDateTimePicker).asRequired().bind(Operation::getCreate_date, Operation::setCreate_date);
        binder.forField(operationBalanceId).asRequired().bind(Operation::getBalance_id, Operation::setBalance_id);

        operationArticleId.setItems(articles);
        operationBalanceId.setItems(balances);

        operationBalanceId.addValueChangeListener(v -> setMinDate(v.getValue()));

        VerticalLayout verticalLayout = new VerticalLayout();

        operationArticleId.setSizeFull();
        operationDebit.setSizeFull();
        operationCredit.setSizeFull();
        operationDateTimePicker.setSizeFull();
        operationDateTimePicker.setStep(Duration.ofMillis(1));
        operationDateTimePicker.setHelperText("HH:MM:SS.FFF");
        operationBalanceId.setSizeFull();
        nameStatus.getStyle().set("color", "red");

        verticalLayout.add(
                operationArticleId,
                operationDebit,
                operationCredit,
                operationBalanceId,
                operationDateTimePicker,
                nameStatus,
                createButtonsLayout()
        );

        add(verticalLayout);
    }

    private void setMinDate(Integer value) {
        if (value != null && value != 0) {
            LocalDateTime dateCreateBalance = budgetController.balancesById(value).get(0).getCreate_date();
            operationDateTimePicker.setMin(dateCreateBalance);
        }
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
        binder.readBean(operation);
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
            fireEvent(new DeleteEvent(this, operation));
        } catch (Exception e) {
            nameStatus.setText("Incorrect value");
        }
    }

    private void validateAndSave() {
        try {
            binder.writeBean(operation);
            fireEvent(new SaveEvent(this, operation));
        } catch (ValidationException e) {
            nameStatus.setText("Incorrect value");
        }
    }

    public static abstract class OperationFormEvent extends ComponentEvent<OperationForm> {
        private Operation operation;

        protected OperationFormEvent(OperationForm source, Operation operation) {
            super(source, false);
            this.operation = operation;
        }

        public Operation getOperation() {
            return operation;
        }
    }

    public static class SaveEvent extends OperationFormEvent {
        SaveEvent(OperationForm source, Operation operation) {
            super(source, operation);
        }
    }

    public static class DeleteEvent extends OperationFormEvent {
        DeleteEvent(OperationForm source, Operation operation) {
            super(source, operation);
        }

    }

    public static class CloseEvent extends OperationFormEvent {
        CloseEvent(OperationForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
