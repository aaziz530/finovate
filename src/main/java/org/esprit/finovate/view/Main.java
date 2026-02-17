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

        // Test connexion
        DatabaseConnection.testConnection();

        // Menu principal
        while (true) {
            showMainMenu();
            int choice = getIntInput("Votre choix: ");

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

    private static void showMainMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║       MENU PRINCIPAL               ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.println("1. 🎫 Gestion des Tickets");
        System.out.println("2. 💬 Gestion des Messages");
        System.out.println("3. 🚪 Quitter");
        System.out.println("────────────────────────────────────");
    }

    // ==================== TICKET MENU ====================

    private static void ticketMenu() {
        while (true) {
            showTicketMenu();
            int choice = getIntInput("Votre choix: ");

            switch (choice) {
                case 1: createTicket(); break;
                case 2: viewAllTickets(); break;
                case 3: viewTicketById(); break;
                case 4: updateTicket(); break;
                case 5: deleteTicket(); break;
                case 6: return; // Retour au menu principal
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
        System.out.println("────────────────────────────────────");
    }

    private static void createTicket() {
        System.out.println("\n=== CRÉER UN TICKET ===");

        System.out.print("Type: ");
        String type = scanner.nextLine();

        System.out.print("Description: ");
        String description = scanner.nextLine();

        System.out.print("Priorité (HAUTE/MOYENNE/BASSE): ");
        String priorite = scanner.nextLine().toUpperCase();

        System.out.print("Statut (NOUVEAU/EN_COURS/RESOLU/FERME): ");
        String statut = scanner.nextLine().toUpperCase();

        Ticket t = new Ticket(type, description, priorite, statut);

        if (ticketDAO.create(t)) {
            System.out.println("✅ Ticket créé avec succès!");
        } else {
            System.out.println("❌ Erreur lors de la création");
        }
    }

    private static void viewAllTickets() {
        System.out.println("\n=== LISTE DES TICKETS ===");

        List<Ticket> tickets = ticketDAO.findAll();

        if (tickets.isEmpty()) {
            System.out.println("📭 Aucun ticket trouvé.");
        } else {
            System.out.println("Nombre total: " + tickets.size());
            System.out.println("────────────────────────────────────");
            for (Ticket t : tickets) {
                System.out.println(t);
                System.out.println("────────────────────────────────────");
            }
        }
    }

    private static void viewTicketById() {
        Long id = getLongInput("\n🔍 ID du ticket: ");

        Ticket t = ticketDAO.findById(id);
        if (t != null) {
            System.out.println("\n" + t);

            // Afficher les messages du ticket
            List<Message> messages = messageDAO.findByTicketId(id);
            if (!messages.isEmpty()) {
                System.out.println("\n💬 Messages associés (" + messages.size() + "):");
                System.out.println("────────────────────────────────────");
                for (Message m : messages) {
                    System.out.println(m);
                    System.out.println("────────────────────────────────────");
                }
            }
        } else {
            System.out.println("❌ Ticket non trouvé.");
        }
    }

    private static void updateTicket() {
        Long id = getLongInput("\n✏️ ID du ticket à modifier: ");

        Ticket t = ticketDAO.findById(id);
        if (t == null) {
            System.out.println("❌ Ticket non trouvé.");
            return;
        }

        System.out.println("Ticket actuel:\n" + t);
        System.out.println("\n(Appuyez sur Entrée pour garder la valeur actuelle)");

        System.out.print("Nouveau type [" + t.getType() + "]: ");
        String type = scanner.nextLine();
        if (!type.isEmpty()) t.setType(type);

        System.out.print("Nouvelle description [" + t.getDescription() + "]: ");
        String desc = scanner.nextLine();
        if (!desc.isEmpty()) t.setDescription(desc);

        System.out.print("Nouvelle priorité [" + t.getPriorite() + "]: ");
        String prio = scanner.nextLine().toUpperCase();
        if (!prio.isEmpty()) t.setPriorite(prio);

        System.out.print("Nouveau statut [" + t.getStatut() + "]: ");
        String stat = scanner.nextLine().toUpperCase();
        if (!stat.isEmpty()) t.setStatut(stat);

        if (ticketDAO.update(t)) {
            System.out.println("✅ Ticket modifié avec succès!");
        } else {
            System.out.println("❌ Erreur lors de la modification");
        }
    }

    private static void deleteTicket() {
        Long id = getLongInput("\n🗑️ ID du ticket à supprimer: ");

        Ticket t = ticketDAO.findById(id);
        if (t == null) {
            System.out.println("❌ Ticket non trouvé.");
            return;
        }

        System.out.println("Ticket à supprimer:\n" + t);
        System.out.print("Confirmer la suppression? (oui/non): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("oui")) {
            if (ticketDAO.delete(id)) {
                System.out.println("✅ Ticket supprimé avec succès!");
            } else {
                System.out.println("❌ Erreur lors de la suppression");
            }
        } else {
            System.out.println("⚠️ Suppression annulée.");
        }
    }

    // ==================== MESSAGE MENU ====================

    private static void messageMenu() {
        while (true) {
            showMessageMenu();
            int choice = getIntInput("Votre choix: ");

            switch (choice) {
                case 1: createMessage(); break;
                case 2: viewAllMessages(); break;
                case 3: viewMessagesByTicket(); break;
                case 4: viewMessageById(); break;
                case 5: updateMessage(); break;
                case 6: deleteMessage(); break;
                case 7: return; // Retour au menu principal
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
        System.out.println("3. 🎫 Messages par ticket");
        System.out.println("4. 🔍 Rechercher par ID");
        System.out.println("5. ✏️ Modifier un message");
        System.out.println("6. 🗑️ Supprimer un message");
        System.out.println("7. ⬅️ Retour au menu principal");
        System.out.println("────────────────────────────────────");
    }

    private static void createMessage() {
        System.out.println("\n=== CRÉER UN MESSAGE ===");

        Long idTicket = getLongInput("ID du ticket: ");

        // Vérifier que le ticket existe
        Ticket t = ticketDAO.findById(idTicket);
        if (t == null) {
            System.out.println("❌ Ticket non trouvé!");
            return;
        }

        System.out.println("✅ Ticket: " + t.getType() + " - " + t.getDescription());

        System.out.print("Contenu du message: ");
        String content = scanner.nextLine();

        Message m = new Message(idTicket, content);

        if (messageDAO.create(m)) {
            System.out.println("✅ Message ajouté au ticket!");
        } else {
            System.out.println("❌ Erreur lors de la création");
        }
    }

    private static void viewAllMessages() {
        System.out.println("\n=== LISTE DES MESSAGES ===");

        List<Message> messages = messageDAO.findAll();

        if (messages.isEmpty()) {
            System.out.println("📭 Aucun message trouvé.");
        } else {
            System.out.println("Nombre total: " + messages.size());
            System.out.println("────────────────────────────────────");
            for (Message m : messages) {
                System.out.println(m);
                System.out.println("────────────────────────────────────");
            }
        }
    }

    private static void viewMessagesByTicket() {
        Long idTicket = getLongInput("\n🎫 ID du ticket: ");

        List<Message> messages = messageDAO.findByTicketId(idTicket);

        if (messages.isEmpty()) {
            System.out.println("📭 Aucun message trouvé pour ce ticket.");
        } else {
            System.out.println("\n💬 Messages du ticket #" + idTicket + " (" + messages.size() + "):");
            System.out.println("────────────────────────────────────");
            for (Message m : messages) {
                System.out.println(m);
                System.out.println("────────────────────────────────────");
            }
        }
    }

    private static void viewMessageById() {
        Long id = getLongInput("\n🔍 ID du message: ");

        Message m = messageDAO.findById(id);
        if (m != null) {
            System.out.println("\n" + m);
        } else {
            System.out.println("❌ Message non trouvé.");
        }
    }

    private static void updateMessage() {
        Long id = getLongInput("\n✏️ ID du message à modifier: ");

        Message m = messageDAO.findById(id);
        if (m == null) {
            System.out.println("❌ Message non trouvé.");
            return;
        }

        System.out.println("Message actuel:\n" + m);
        System.out.println("\n(Appuyez sur Entrée pour garder le contenu actuel)");

        System.out.print("Nouveau contenu [" + m.getContent() + "]: ");
        String content = scanner.nextLine();
        if (!content.isEmpty()) m.setContent(content);

        if (messageDAO.update(m)) {
            System.out.println("✅ Message modifié avec succès!");
        } else {
            System.out.println("❌ Erreur lors de la modification");
        }
    }

    private static void deleteMessage() {
        Long id = getLongInput("\n🗑️ ID du message à supprimer: ");

        Message m = messageDAO.findById(id);
        if (m == null) {
            System.out.println("❌ Message non trouvé.");
            return;
        }

        System.out.println("Message à supprimer:\n" + m);
        System.out.print("Confirmer la suppression? (oui/non): ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("oui")) {
            if (messageDAO.delete(id)) {
                System.out.println("✅ Message supprimé avec succès!");
            } else {
                System.out.println("❌ Erreur lors de la suppression");
            }
        } else {
            System.out.println("⚠️ Suppression annulée.");
        }
    }

    // ===== INPUT HELPERS =====

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("❌ Veuillez entrer un nombre: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }

    private static Long getLongInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextLong()) {
            System.out.println("❌ Veuillez entrer un nombre: ");
            scanner.next();
        }
        Long value = scanner.nextLong();
        scanner.nextLine(); // consume newline
        return value;
    }
}