package org.esprit.finovate.controllers;

import org.esprit.finovate.entities.Project;
import org.esprit.finovate.services.ProjectService;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * MVC Controller for Project operations.
 * Handles user input and delegates to ProjectService.
 */
public class ProjectController {

    private final ProjectService projectService = new ProjectService();

    public void addProject(String title, String description, double goalAmount, Date deadline) throws SQLException {
        Project p = new Project();
        p.setTitle(title);
        p.setDescription(description);
        p.setGoal_amount(goalAmount);
        p.setCurrent_amount(0);
        p.setCreated_at(new Date());
        p.setDeadline(deadline);
        p.setStatus("OPEN");
        projectService.addProject(p);
    }

    public List<Project> getAllProjects() throws SQLException {
        return projectService.getAllProjects();
    }

    public List<Project> getProjectsByOwnerId(Long ownerId) throws SQLException {
        return projectService.getProjectsByOwnerId(ownerId);
    }

    public Project getProjectById(Long id) throws SQLException {
        return projectService.getProjectById(id);
    }

    public void updateProject(Project p) throws SQLException {
        projectService.updateProject(p);
    }

    public void deleteProject(Long id) throws SQLException {
        projectService.deleteProject(id);
    }
}
