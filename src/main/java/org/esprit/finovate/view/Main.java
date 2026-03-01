package org.esprit.finovate.view;

import org.esprit.finovate.dao.MessageDAO;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.database.DatabaseConnection;
import org.esprit.finovate.model.Message;
import org.esprit.finovate.model.Ticket;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static TicketDAO ticketDAO = new TicketDAO();
    private static MessageDAO messageDAO = new MessageDAO();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("=== APPLICATION GESTION TICKETS & MESSAGES ===\n");

        // Tester la connexion à la base
        DatabaseConnection.testConnection();

        while (true) {
            showMainMenu();
            int choice = readInt("Votre choix: ");

            switch (choice) {
                case 1: ticketMenu(); break;
                case 2: messageMenu(); break;
                case 3:
                    System.out.println("\n👋 Au revoir!");
                    return;
                default:
                    System.out.println("❌ Choix invalide! Réessayez.");
            }
        }
    }

    // Méthode utilitaire pour lire un entier depuis le Scanner
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("❌ Veuillez entrer un nombre valide !");
            scanner.next(); // Ignore l'entrée invalide
            System.out.print(prompt);
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // Consomme le retour à la ligne restant
        return value;
    }

    // Menu principal
    private static void showMainMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║       MENU PRINCIPAL               ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. 🎫 Gestion des Tickets");
        System.out.println("2. 💬 Gestion des Messages");
        System.out.println("3. 🚪 Quitter");
        System.out.println("────────────────────────────────────");
    }

    // ================== TICKET ==================
    private static void ticketMenu() {
        while (true) {
            showTicketMenu();
            int choice = readInt("Votre choix: ");

            switch (choice) {
                case 1: createTicket(); break;
                case 2: viewAllTickets(); break;
                case 3: viewTicketById(); break;
                case 4: updateTicket(); break;
                case 5: deleteTicket(); break;
                case 6: return;
                default:
                    System.out.println("❌ Choix invalide! Réessayez.");
            }
        }
    }

    private static void showTicketMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   GESTION DES TICKETS              ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. ➕ Créer un ticket");
        System.out.println("2. 📋 Afficher tous les tickets");
        System.out.println("3. 🔍 Rechercher par ID");
        System.out.println("4. ✏️ Modifier un ticket");
        System.out.println("5. 🗑️ Supprimer un ticket");
        System.out.println("6. ⬅️ Retour au menu principal");
    }

    // Exemple de méthodes pour ticket (tu peux compléter avec DAO)
    private static void createTicket() {
        System.out.println("\n💡 Fonction de création de ticket à implémenter");
    }

    private static void viewAllTickets() {
        System.out.println("\n💡 Fonction d'affichage de tous les tickets à implémenter");
    }

    private static void viewTicketById() {
        System.out.println("\n💡 Fonction de recherche par ID à implémenter");
    }

    private static void updateTicket() {
        System.out.println("\n💡 Fonction de modification de ticket à implémenter");
    }

    private static void deleteTicket() {
        System.out.println("\n💡 Fonction de suppression de ticket à implémenter");
    }

    // ================== MESSAGE ==================
    private static void messageMenu() {
        while (true) {
            showMessageMenu();
            int choice = readInt("Votre choix: ");

            switch (choice) {
                case 1: createMessage(); break;
                case 2: viewAllMessages(); break;
                case 3: viewMessageById(); break;
                case 4: updateMessage(); break;
                case 5: deleteMessage(); break;
                case 6: return;
                default:
                    System.out.println("❌ Choix invalide! Réessayez.");
            }
        }
    }

    private static void showMessageMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║   GESTION DES MESSAGES             ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. ➕ Créer un message");
        System.out.println("2. 📋 Afficher tous les messages");
        System.out.println("3. 🔍 Rechercher par ID");
        System.out.println("4. ✏️ Modifier un message");
        System.out.println("5. 🗑️ Supprimer un message");
        System.out.println("6. ⬅️ Retour au menu principal");
    }

    // Exemple de méthodes pour messages (à compléter)
    private static void createMessage() {
        System.out.println("\n💡 Fonction de création de message à implémenter");
    }

    private static void viewAllMessages() {
        System.out.println("\n💡 Fonction d'affichage de tous les messages à implémenter");
    }

    private static void viewMessageById() {
        System.out.println("\n💡 Fonction de recherche de message par ID à implémenter");
    }

    private static void updateMessage() {
        System.out.println("\n💡 Fonction de modification de message à implémenter");
    }

    private static void deleteMessage() {
        System.out.println("\n💡 Fonction de suppression de message à implémenter");
    }
}