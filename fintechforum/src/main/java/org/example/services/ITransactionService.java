package org.example.services;

import org.example.entities.Transaction;
import java.sql.SQLException;
import java.util.List;

public interface ITransactionService {
    void transferMoney(Long senderId, String numeroCarte, String cin, float amount, String description)
            throws SQLException;

    List<Transaction> getTransactionsByUserId(Long userId) throws SQLException;

    float getUserBalance(Long userId) throws SQLException;
}
