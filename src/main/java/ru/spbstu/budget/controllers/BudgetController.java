package ru.spbstu.budget.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbstu.budget.dao.BudgetDao;
import ru.spbstu.budget.models.Article;
import ru.spbstu.budget.models.Balance;
import ru.spbstu.budget.models.Operation;

import java.util.List;

@RestController
public class BudgetController {
    private BudgetDao budgetDao;

    @Autowired
    public BudgetController(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    @Autowired
    public void setBudgetDao(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    public List<Article> articles() {
        return budgetDao.showAllArticles();
    }

    public List<Article> articlesByName(String name) {
        if (name == null || name.isEmpty()) {
            return articles();
        }
        else {
            return budgetDao.searchArticlesByName(name);
        }
    }

    public void saveArticle(Article article) {
        if(article.getId() == 0) {
            budgetDao.addArticle(article);
        }
        else
        {
            budgetDao.updateArticle(article);
        }
    }

    public void removeArticle(Article article) {
        budgetDao.deleteOperationsByArticle(article.getName());
        budgetDao.deleteArticle(article.getId());
    }

    public List<Balance> balances() {
        return budgetDao.showAllBalances();
    }

    public List<Balance> balancesById(Integer id) {
        if (id == null)
        {
            return balances();
        }
        else
        {
            return budgetDao.showBalance(id);
        }
    }

    public void saveBalance(Balance balance) {
        if (balance.getId() == 0) {
            budgetDao.addBalance(balance);
        }
        else
        {
            budgetDao.updateBalance(balance);
        }
    }

    public void removeBalance(Balance balance) {
        budgetDao.deleteBalance(balance.getId());
    }

    public void saveOperation(Operation operation) {
        if (operation.getId() == 0) {
            budgetDao.addOperation(operation);
        }
        else
        {
            budgetDao.updateOperation(operation);
        }
    }

    public List<Operation> operations() {
        return budgetDao.showAllOperations();
    }

    public List<Operation> operationsById(Integer id) {
        if (id == null)
        {
            return operations();
        }
        else
        {
            return budgetDao.showOperation(id);
        }
    }

    public void removeOperation(Operation operation) {
        budgetDao.deleteOperation(operation.getId());
    }

    public List<Balance> profitableBalance() {
        return budgetDao.showProfitableBalance();
    }

    public List<Balance> unprofitableBalance() {
        return budgetDao.showUnprofitableBalance();
    }

    public List<Balance> lastBalance() {
        return budgetDao.showLastBalance();
    }

    public List<Balance> balancesByArticle(String name) {
        if (name == null || name.isEmpty()) {
            return budgetDao.showAllBalances();
        }
        else
        {
            return budgetDao.showBalancesByArticle(name);
        }
    }

    public List<Operation> profitableOperation() {
        return budgetDao.showProfitableOperation();
    }


    public List<Operation> unprofitableOperation() {
        return budgetDao.showUnprofitableOperation();
    }


    public List<Operation> lastOperation() {
        return budgetDao.showLastOperation();
    }

    public List<Operation> showOperationsByBalance(Integer id) {
        if (id == null || id == -1){
            return operations();
        }
        else
        {
            return budgetDao.showOperationsByBalance(id);
        }
    }

    public List<Operation> showOperationsByArticleName(String name) {
        if (name == null || name.isEmpty()) {
            return operations();
        }
        else
        {
            return budgetDao.showOperationsByArticle(name);
        }
    }

    public void deleteOperationsByArticle(String name) {
        budgetDao.deleteOperationsByArticle(name);
    }

    public List<Article> searchArticle(Integer id) {
        if (id == null || id == -1){
            return articles();
        }
        else
        {
            return budgetDao.searchArticlesByOperationId(id);
        }

    }

    public List<Article> articlesByCredit(Double size) {
        if (size == null) {
            return articles();
        }
        else
        {
            return budgetDao.showArticlesByCredit(size);
        }
    }

    public List<Article> articlesByDebit(Double size) {
        if (size == null) {
            return articles();
        }
        else
        {
            return budgetDao.showArticlesByDebit(size);
        }
    }

    public List<Article> articleByd(Integer id) {
        if (id == null) {
            return articles();
        }
        else
        {
            return budgetDao.articleById(id);
        }
    }
}