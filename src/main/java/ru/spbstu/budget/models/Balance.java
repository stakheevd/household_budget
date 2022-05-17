package ru.spbstu.budget.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

public class Balance {

    public Balance(int id, LocalDateTime create_date, double debit, double credit) {
        this.id = id;
        this.create_date = create_date;
        this.debit = debit;
        this.credit = credit;
        this.amount = Math.round((debit - credit) * 100.0) / 100.0 ;
    }

    @Setter
    @Getter
    private int id;

    @Setter
    @Getter
    private LocalDateTime create_date;

    @Setter
    @Getter
    private double debit;

    @Setter
    @Getter
    private double credit;

    @Setter
    @Getter
    private double amount;

    public Balance() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Balance balance = (Balance) o;
        return id == balance.id && debit == balance.debit && credit == balance.credit && amount == balance.amount && Objects.equals(create_date, balance.create_date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return  "id: " + id +
                "; dateCreate: " + create_date +
                "; debit: " + debit +
                "; credit: " + credit +
                "; amount: " + amount +
                "\n";
    }

    public String getLabel() {
        return create_date + " (" + id + ")";
    }
}
