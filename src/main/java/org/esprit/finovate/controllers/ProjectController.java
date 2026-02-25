package org.esprit.finovate.controllers;

import org.esprit.finovate.models.Project;
import org.esprit.finovate.services.ProjectService;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ProjectController {

    private final ProjectService projectService = new ProjectService();

    public void addProject(String title, String description, double goalAmount, Date deadline) throws SQLException {
        addProject(title, description, goalAmount, deadline, null);
    }

    public void addProject(String title, String description, double goalAmount, Date deadline, String imagePath) throws SQLException {
        addProject(title, description, goalAmount, deadline, imagePath, null, null, null);
    }

    public void addProject(String title, String description, double goalAmount, Date deadline,
                           String imagePath, Double latitude, Double longitude, String category) throws SQLException {
        Project p = new Project();
        p.setTitle(title);
        p.setDescription(description);
        p.setGoal_amount(goalAmount);
        p.setCurrent_amount(0);
        p.setCreated_at(new Date());
        p.setDeadline(deadline);
        p.setStatus("OPEN");
        p.setImagePath(imagePath);
        p.setLatitude(latitude);
        p.setLongitude(longitude);
        p.setCategory(category);
        projectService.addProject(p);
    }

    public List<Project> getSimilarProjects(Long projectId, int limit) throws SQLException {
        return projectService.getSimilarProjects(projectId, limit);
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

    public void addProjectAsAdmin(Project p) throws SQLException {
        projectService.addProjectAsAdmin(p);
    }
}
