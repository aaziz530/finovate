package org.esprit.finovate.controllers;

import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.services.InvestissementService;

import java.sql.SQLException;
import java.util.List;

/**
 * MVC Controller for Investissement operations.
 * Handles user input and delegates to InvestissementService.
 * When a user invests in a project, the investment is added and project.current_amount is updated.
 */
public class InvestissementController {

    private final InvestissementService investissementService = new InvestissementService();

    public void addInvestissement(Long projectId, double amount) throws SQLException {
        Investissement inv = new Investissement(projectId, null, amount);
        investissementService.addInvestissement(inv);
    }

    public List<Investissement> getAllInvestissements() throws SQLException {
        return investissementService.getAllInvestissements();
    }

    public Investissement getInvestissementById(Long id) throws SQLException {
        return investissementService.getInvestissementById(id);
    }

    public List<Investissement> getInvestissementsByProjectId(Long projectId) throws SQLException {
        return investissementService.getInvestissementsByProjectId(projectId);
    }

    public List<Investissement> getInvestissementsByInvestorId(Long investorId) throws SQLException {
        return investissementService.getInvestissementsByInvestorId(investorId);
    }

    public boolean hasInvestments(Long projectId) throws SQLException {
        return investissementService.hasInvestments(projectId);
    }

    public List<Investissement> getPendingInvestmentsForOwner(Long ownerId) throws SQLException {
        return investissementService.getPendingInvestmentsForOwner(ownerId);
    }

    public void acceptInvestissement(Long id) throws SQLException {
        investissementService.acceptInvestissement(id);
    }

    public void declineInvestissement(Long id) throws SQLException {
        investissementService.declineInvestissement(id);
    }

    public void updateInvestissement(Investissement inv) throws SQLException {
        investissementService.updateInvestissement(inv);
    }

    public void deleteInvestissement(Long id) throws SQLException {
        investissementService.deleteInvestissement(id);
    }

    public void addInvestissementAsAdmin(Investissement inv) throws SQLException {
        investissementService.addInvestissementAsAdmin(inv);
    }

    public void updateInvestissementAsAdmin(Investissement inv) throws SQLException {
        investissementService.updateInvestissementAsAdmin(inv);
    }
}
