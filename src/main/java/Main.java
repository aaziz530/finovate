package org.esprit.finovate;

import org.esprit.finovate.controllers.InvestissementController;
import org.esprit.finovate.controllers.ProjectController;
import org.esprit.finovate.models.Investissement;
import org.esprit.finovate.models.Project;
import org.esprit.finovate.services.UserService;
import org.esprit.finovate.utils.Session;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserService userService = new UserService();
        ProjectController projectController = new ProjectController();
        InvestissementController investissementController = new InvestissementController();

        try {
            // -------- LOGIN --------
            System.out.println("Email:");
            String email = sc.nextLine();
            System.out.println("Password:");
            String password = sc.nextLine();

            if (userService.login(email, password) == null) {
                System.out.println("❌ Login failed");
                return;
            }

            System.out.println("✅ Welcome " + Session.currentUser.getFirstName());

            // -------- MENU --------
            while (true) {
                System.out.println("\n--- Finovate Menu ---");
                System.out.println("1. Add Project");
                System.out.println("2. List Projects");
                System.out.println("3. Invest in a Project");
                System.out.println("4. My Investments");
                System.out.println("0. Exit");
                System.out.print("Choice: ");

                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> {
                        System.out.print("Project title: ");
                        String title = sc.nextLine();
                        System.out.print("Description: ");
                        String desc = sc.nextLine();
                        System.out.print("Goal amount: ");
                        double goalAmount = Double.parseDouble(sc.nextLine());
                        projectController.addProject(title, desc, goalAmount, new Date());
                    }
                    case "2" -> {
                        List<Project> projects = projectController.getAllProjects();
                        if (projects.isEmpty()) {
                            System.out.println("No projects yet.");
                        } else {
                            for (Project p : projects) {
                                System.out.printf("[%d] %s - %.2f / %.2f - %s%n",
                                        p.getProject_id(), p.getTitle(), p.getCurrent_amount(), p.getGoal_amount(), p.getStatus());
                            }
                        }
                    }
                    case "3" -> {
                        List<Project> projects = projectController.getAllProjects();
                        if (projects.isEmpty()) {
                            System.out.println("No projects to invest in.");
                            break;
                        }
                        for (Project p : projects) {
                            System.out.printf("[%d] %s - %.2f / %.2f%n", p.getProject_id(), p.getTitle(), p.getCurrent_amount(), p.getGoal_amount());
                        }
                        System.out.print("Project ID to invest in: ");
                        long projectId = Long.parseLong(sc.nextLine());
                        System.out.print("Amount: ");
                        double amount = Double.parseDouble(sc.nextLine());
                        investissementController.addInvestissement(projectId, amount);
                    }
                    case "4" -> {
                        List<Investissement> invs = investissementController.getInvestissementsByInvestorId(Session.currentUser.getId());
                        if (invs.isEmpty()) {
                            System.out.println("You have no investments.");
                        } else {
                            for (Investissement inv : invs) {
                                System.out.printf("[%d] Project %d - %.2f - %s%n",
                                        inv.getInvestissement_id(), inv.getProject_id(), inv.getAmount(), inv.getStatus());
                            }
                        }
                    }
                    case "0" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}