package ru.spbstu.budget.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import org.springframework.beans.factory.annotation.Autowired;
import ru.spbstu.budget.controllers.BudgetController;
import ru.spbstu.budget.models.Balance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class BalanceForm extends FormLayout {
    Binder<Balance> binder = new BeanValidationBinder<>(Balance.class);

    IntegerField balanceId = new IntegerField("Balance id");
    DateTimePicker balanceDateTimePicker = new DateTimePicker("Create date");
    NumberField balanceDebit = new NumberField("Debit");
    NumberField balanceCredit = new NumberField("Credit");
    NumberField balanceAmount = new NumberField("Amount");
    Label nameStatus = new Label();

    Button save = new Button("Save");
    Button delete = new Button("Delete");
    Button close = new Button("Close");

    private Balance balance;
    private BudgetController budgetController;

    @Autowired
    public BalanceForm(BudgetController budgetController) {
        this.budgetController = budgetController;
        addClassName("balance-form");

        binder.bind(balanceId, Balance::getId, Balance::setId);
        binder.bind(balanceAmount, Balance::getAmount, Balance::setAmount);

        binder.forField(balanceDateTimePicker)
                .asRequired()
                .bind(Balance::getCreate_date, Balance::setCreate_date);

        binder.forField(balanceDebit)
                .asRequired()
                .withValidator(v -> (v >= 0),
                        "Debit must be non-negative")
                .withValidator(v -> (v <= 9999999999999999.99),
                        "Debit is too large")
                .bind(Balance::getDebit, Balance::setDebit);

        binder.forField(balanceCredit)
                .asRequired()
                .withValidator(v -> (v >= 0),
                        "Credit must be non-negative")
                .withValidator(v -> (v <= 9999999999999999.99),
                        "Credit is too large")
                .bind(Balance::getCredit, Balance::setCredit);

        VerticalLayout verticalLayout = new VerticalLayout();

        balanceDateTimePicker.setSizeFull();
        balanceDateTimePicker.setStep(Duration.ofMillis(1));
        balanceDateTimePicker.setHelperText("HH:MM:SS.FFF");
        nameStatus.getStyle().set("color", "red");

        balanceDebit.setSizeFull();
        balanceCredit.setSizeFull();

        verticalLayout.add(
                balanceDateTimePicker,
                balanceDebit,
                balanceCredit,
                nameStatus,
                createButtonsLayout()
        );

        add(verticalLayout);
    }

    public void setBalance(Balance balance) {
        this.balance = balance;
        binder.readBean(balance);
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
            fireEvent(new DeleteEvent(this, balance));
        } catch (Exception e) {
            nameStatus.setText("Incorrect value");
        }
    }

    private void validateAndSave() {
        try {
            binder.writeBean(balance);
            fireEvent(new SaveEvent(this, balance));
        } catch (ValidationException e) {
            nameStatus.setText("Incorrect value");
        }
    }

    public static abstract class BalanceFormEvent extends ComponentEvent<BalanceForm> {
        private Balance balance;

        protected BalanceFormEvent(BalanceForm source, Balance balance) {
            super(source, false);
            this.balance = balance;
        }

        public Balance getBalance() {
            return balance;
        }
    }

    public static class SaveEvent extends BalanceForm.BalanceFormEvent {
        SaveEvent(BalanceForm source, Balance balance) {
            super(source, balance);
        }
    }

    public static class DeleteEvent extends BalanceForm.BalanceFormEvent {
        DeleteEvent(BalanceForm source, Balance balance) {
            super(source, balance);
        }

    }

    public static class CloseEvent extends BalanceForm.BalanceFormEvent {
        CloseEvent(BalanceForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
