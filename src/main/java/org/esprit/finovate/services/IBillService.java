package org.esprit.finovate.services;

import org.esprit.finovate.entities.Bill;
import java.sql.SQLException;
import java.util.List;

public interface IBillService {
    void payBill(Long userId, String reference, double amount) throws SQLException;

    List<Bill> getBillsByUserId(Long userId) throws SQLException;
}
