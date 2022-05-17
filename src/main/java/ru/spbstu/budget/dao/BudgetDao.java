package ru.spbstu.budget.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.spbstu.budget.models.Article;
import ru.spbstu.budget.models.Balance;
import ru.spbstu.budget.models.Operation;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BudgetDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    //1 Add article
    public void addArticle(Article article)
    {
        this.jdbcTemplate.update("INSERT INTO ARTICLES(NAME) VALUES (?)",
                article.getName());
    }

    //2 Show all articles
    public List<Article> showAllArticles()
    {
        List<Article> articles = this.jdbcTemplate.query("SELECT * FROM articles",
                (resultSet, rawNum) -> {
                    return new Article(resultSet.getInt("id"),
                            resultSet.getString("name"));
                } );

        return articles;
    }

    //3 Show article by id
    public List<Article> articleById(Integer id)
    {
        List<Article> articles = this.jdbcTemplate.query("SELECT * FROM articles WHERE id = ?",
                (resultSet, rawNum) -> {
                    return new Article(resultSet.getInt("id"),
                            resultSet.getString("name"));
                }, id );

        return articles;
    }

    //4 Show article by operation id
    public List<Article> searchArticlesByOperationId(int id) {
        List<Article> articles = this.jdbcTemplate.query("SELECT articles.id, articles.name FROM articles " +
                        "JOIN operations ON articles.id = operations.article_id WHERE operations.id = ?",
                (resultSet, rawNum) -> {
                    return new Article(resultSet.getInt("id"),
                            resultSet.getString("name"));
                }, id );

        return articles;
    }

    //4 Delete article by id
    public void deleteArticle(int id){
        this.jdbcTemplate.update("DELETE FROM articles WHERE id = ?",
                id);
    }

    //5 Update article by id
    public void updateArticle(Article article)
    {
        this.jdbcTemplate.update("UPDATE articles SET name = ? WHERE id = ?",
                article.getName(), article.getId());
    }

    //6 Add balance
    public void addBalance(Balance balance)
    {
        this.jdbcTemplate.update("INSERT INTO balance(create_date, debit, credit, amount) VALUES (?, ?, ?, ?)",
                balance.getCreate_date(),
                balance.getDebit(),
                balance.getCredit(),
                balance.getAmount());
    }

    //7 Show all balances
    public List<Balance> showAllBalances() {
        List<Balance> balances = this.jdbcTemplate.query("SELECT * FROM balance",
                (resultSet, rawNum) -> {
                    return new Balance(resultSet.getInt("id"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"));
                } );

        return balances;
    }

    //8 Delete balance by id
    public void deleteBalance(int id) {
        this.jdbcTemplate.update("DELETE FROM operations WHERE balance_id = ?",
                id);

        this.jdbcTemplate.update("DELETE FROM balance WHERE id = ?", id);
    }

    //9 Update balance by id
    public void updateBalance(Balance balance) {
        this.jdbcTemplate.update("UPDATE balance set debit = ?, credit = ?" +
                        "WHERE id = ?",
                balance.getDebit(),
                balance.getCredit(),
                balance.getId());
    }

    //10 Add operation
    public void addOperation(Operation operation) {
        this.jdbcTemplate.update("INSERT INTO operations(article_id, debit, credit, create_date, balance_id) " +
                "VALUES (?, ?, ?, ?, ?)",
                operation.getArticle_id(),
                operation.getDebit(),
                operation.getCredit(),
                operation.getCreate_date(),
                operation.getBalance_id());

        changeBalance(operation);
    }

    //11 Show all operations
    public List<Operation> showAllOperations() {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } );

        return operations;
    }

    //12 Delete operation by id
    public void deleteOperation(int id) {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations WHERE id = ?",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                }, id );

        restoreBalance(operations.get(0));

        this.jdbcTemplate.update("DELETE FROM operations WHERE id = ?", id);

    }

    //13 Update operation by id
    public void updateOperation(Operation operation) {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations WHERE id = ?",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                }, operation.getId() );

        restoreBalance(operations.get(0));

        operations.get(0).setArticle_id(operation.getArticle_id());
        operations.get(0).setDebit(operation.getDebit());
        operations.get(0).setCredit(operation.getCredit());
        operations.get(0).setCreate_date(operation.getCreate_date());
        operations.get(0).setBalance_id(operation.getBalance_id());

        this.jdbcTemplate.update("UPDATE operations set article_id = ?, debit = ?, credit = ?, create_date = ?, balance_id = ? " +
                        "WHERE id = ?",
                operation.getArticle_id(),
                operation.getDebit(),
                operation.getCredit(),
                operation.getCreate_date(),
                operation.getBalance_id(),
                operation.getId());

        changeBalance(operations.get(0));
    }

    //14 Show the most profitable balance
    public List<Balance> showProfitableBalance() {
        List<Balance> balances = this.jdbcTemplate.query("SELECT * FROM balance",
                (resultSet, rawNum) -> {
                    return new Balance(resultSet.getInt("id"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"));
                } );

        List<Balance> best_balance = new ArrayList<>();

        Balance best = balances.get(0);

        for (Balance it: balances) {
            if (it.getAmount() > best.getAmount()) {
                best = it;
            }
        }

        best_balance.add(best);

        return best_balance;
    }

    //15 Show the most profitable operation
    public List<Operation> showProfitableOperation() {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } );

        List<Operation> best_operation = new ArrayList<>();

        Operation best = operations.get(0);

        for (Operation it: operations) {
            if ((it.getDebit() - it.getCredit()) > (best.getDebit() - best.getCredit())) {
                best = it;
            }
        }

        best_operation.add(best);

        return best_operation;
    }

    //16 Show the most unprofitable balance
    public List<Balance> showUnprofitableBalance() {
        List<Balance> balances = this.jdbcTemplate.query("SELECT * FROM balance",
                (resultSet, rawNum) -> {
                    return new Balance(resultSet.getInt("id"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"));
                } );

        List<Balance> worst_balance = new ArrayList<>();

        Balance worst = balances.get(0);

        for (Balance it: balances) {
            if (it.getAmount() < worst.getAmount()) {
                worst = it;
            }
        }

        worst_balance.add(worst);

        return worst_balance;
    }

    //17 Show the most unprofitable operation
    public List<Operation> showUnprofitableOperation() {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } );

        List<Operation> worst_operation = new ArrayList<>();

        Operation worst = operations.get(0);

        for (Operation it: operations) {
            if ((it.getDebit() - it.getCredit()) < (worst.getDebit() - worst.getCredit())) {
                worst = it;
            }
        }

        worst_operation.add(worst);

        return worst_operation;
    }

    //18 Show the last balance
    public List<Balance> showLastBalance() {
        List<Balance> balances = this.jdbcTemplate.query("SELECT * FROM balance",
                (resultSet, rawNum) -> {
                    return new Balance(resultSet.getInt("id"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"));
                } );

        List<Balance> last_balance = new ArrayList<>();

        Balance last = balances.get(0);

        for (Balance it: balances) {
            if (it.getCreate_date().compareTo(last.getCreate_date()) > 0) {
                last = it;
            }
        }

        last_balance.add(last);

        return last_balance;
    }

    //19 Show the last operation
    public List<Operation> showLastOperation() {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } );

        List<Operation> last_operation = new ArrayList<>();

        Operation last = operations.get(0);

        for (Operation it: operations) {
            if (it.getCreate_date().compareTo(last.getCreate_date()) > 0) {
                last = it;
            }
        }

        last_operation.add(last);

        return last_operation;
    }

    //20 Show all operations by balance
    public List<Operation> showOperationsByBalance(int id) {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations WHERE balance_id = ?",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } , id);

        return operations;
    }

    //21 Show balance by id
    public List<Balance> showBalance(int id) {
        List<Balance> balances = this.jdbcTemplate.query("SELECT * FROM balance WHERE id = ?",
                (resultSet, rawNum) -> {
                    return new Balance(resultSet.getInt("id"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"));
                }, id );

        return balances;
    }

    //22 Show balances by article name
    public List<Balance> showBalancesByArticle(String name) {
        List<Balance> balances = this.jdbcTemplate.query("SELECT DISTINCT balance.id, balance.create_date, balance.debit, " +
                        "balance.credit, balance.amount FROM balance JOIN operations " +
                        "ON balance.id = operations.balance_id JOIN articles " +
                        "ON operations.article_id = articles.id WHERE articles.name = ?;",
                (resultSet, rawNum) -> {
                    return new Balance(resultSet.getInt("id"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"));
                }, name );

        return balances;
    }

    //23 Delete operations by article name
    public void deleteOperationsByArticle(String name) {
        List<Operation> operations = showOperationsByArticle(name);

        for (Operation op: operations) {
            restoreBalance(op);
        }

        this.jdbcTemplate.update("DELETE FROM operations o USING articles a WHERE o.article_id = a.id AND a.name = ?;", name);
    }

    //24 Show operations by article name
    public List<Operation> showOperationsByArticle(String name) {
        List<Operation> operations = this.jdbcTemplate.query("SELECT operations.id, operations.article_id, operations.debit," +
                        " operations.credit, operations.create_date, operations.balance_id FROM operations JOIN articles" +
                        " ON operations.article_id = articles.id WHERE name = ?;",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } , name);

        return operations;
    }

    //25 Show articles by credit
    public List<Article> showArticlesByCredit(Double size) {
        List<Article> articles = this.jdbcTemplate.query("SELECT DISTINCT articles.id, articles.name " +
                        "FROM articles JOIN operations ON articles.id = operations.article_id " +
                        "WHERE operations.credit >= ?;",
                (resultSet, rawNum) -> {
                    return new Article(resultSet.getInt("id"),
                            resultSet.getString("name"));
                }, size );

        return articles;
    }

    //26 Show articles by debit
    public List<Article> showArticlesByDebit(Double size) {
        List<Article> articles = this.jdbcTemplate.query("SELECT DISTINCT articles.id, articles.name " +
                        "FROM articles JOIN operations ON articles.id = operations.article_id " +
                        "WHERE operations.debit >= ?;",
                (resultSet, rawNum) -> {
                    return new Article(resultSet.getInt("id"),
                            resultSet.getString("name"));
                }, size );
        //Посчитать количество операций для определённого типа
        return articles;
    }

    //27 Show operation by id
    public List<Operation> showOperation(int id) {
        List<Operation> operations = this.jdbcTemplate.query("SELECT * FROM operations WHERE id = ?;",
                (resultSet, rawNum) -> {
                    return new Operation(resultSet.getInt("id"),
                            resultSet.getInt("article_id"),
                            resultSet.getDouble("debit"),
                            resultSet.getDouble("credit"),
                            resultSet.getTimestamp("create_date").toLocalDateTime(),
                            resultSet.getInt("balance_id"));
                } , id);

        return operations;
    }

    //28 Change balance by operation
    public void changeBalance(Operation operation) {
        this.jdbcTemplate.update("UPDATE balance SET debit = debit + ?, credit = credit + ?, amount = amount + ? - ? WHERE id = ?",
                operation.getDebit(),
                operation.getCredit(),
                operation.getDebit(),
                operation.getCredit(),
                operation.getBalance_id());
    }

    //29 Restore balance by operation
    public void restoreBalance(Operation operation) {
        this.jdbcTemplate.update("UPDATE balance SET debit = debit - ?, credit = credit - ?, amount = amount - ? + ? WHERE id = ?",
                operation.getDebit(),
                operation.getCredit(),
                operation.getDebit(),
                operation.getCredit(),
                operation.getBalance_id());
    }

    // 30 Show articles by name
    public List<Article> searchArticlesByName(String name) {
        List<Article> articles = this.jdbcTemplate.query("SELECT id, name FROM articles" +
                        " WHERE LOWER(articles.name) LIKE LOWER(CONCAT('%', ?, '%'));",
                (resultSet, rawNum) -> {
                    return new Article(resultSet.getInt("id"),
                            resultSet.getString("name"));
                } , name);

        return articles;
    }
}
