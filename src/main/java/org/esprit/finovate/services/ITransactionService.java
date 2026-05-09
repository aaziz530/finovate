package org.esprit.finovate.services;

import org.esprit.finovate.entities.Transaction;
import java.sql.SQLException;
import java.util.List;

public interface ITransactionService {
    void transferMoney(Long senderId, Long numeroCarte, String cin, float amount, String description)
            throws SQLException;

    List<Transaction> getTransactionsByUserId(Long userId) throws SQLException;

    float getUserBalance(Long userId) throws SQLException;

    void logTopUp(Long userId, float amount) throws SQLException;

    float getDailyTransferTotal(Long userId) throws SQLException;
}
