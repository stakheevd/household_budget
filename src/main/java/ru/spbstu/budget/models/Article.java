package ru.spbstu.budget.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
public class Article {

    @Setter
    @Getter
    @NotEmpty
    private int id;

    @Setter
    @Getter
    @NotEmpty
    private String name;

    public Article() {
    }

    @Override
    public String toString() {
        return  "id: " + id +
                "; name: " + name + "\n";
    }

    public String getLabel() {
        return name + " (" + id + ")";
    }
}
