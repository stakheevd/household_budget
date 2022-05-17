package ru.spbstu.budget.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
public class Operation {
    @Setter
    @Getter
    @NotEmpty
    private int id;

    @Setter
    @Getter
    @NotEmpty
    private int article_id;

    @Setter
    @Getter
    @NotEmpty
    private double debit;

    @Setter
    @Getter
    @NotEmpty
    private double credit;

    @Setter
    @Getter
    @NotNull
    private LocalDateTime create_date;

    @Setter
    @Getter
    @NotEmpty
    private int balance_id;

    public Operation() {
    }

    @Override
    public String toString() {
        return  "id: " + id +
                "; articleId: " + article_id +
                "; debit: " + debit +
                "; credit: " + credit +
                "; dateCreate: " + create_date +
                "; balanceId: " + balance_id +
                "\n";
    }
}
