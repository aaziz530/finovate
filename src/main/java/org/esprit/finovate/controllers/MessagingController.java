package org.esprit.finovate.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.esprit.finovate.dao.TicketDAO;
import org.esprit.finovate.model.Message;
import org.esprit.finovate.model.Ticket;
import org.esprit.finovate.services.MessageService;
import org.esprit.finovate.utils.Session;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MessagingController implements Initializable {

    @FXML private ListView<Ticket> ticketsList;
    @FXML private Label lblTicketTitle;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private TextField searchField;

    private final TicketDAO ticketDAO = new TicketDAO();
    private final MessageService messageService = new MessageService();
    private Ticket selectedTicket;
    private List<Ticket> allTickets = new ArrayList<>();

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTicketsList();
        loadTickets();

        // Listener recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTickets(newVal));

        // Listener selection ticket
        ticketsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedTicket = newVal;
                        lblTicketTitle.setText("#" + newVal.getId() + " - " + newVal.getType());
                        loadMessages();
                    }
                });
    }

    private void setupTicketsList() {
        ticketsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ticket item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    VBox cell = new VBox(2);
                    Label title = new Label("#" + item.getId() + " - " + item.getType());
                    title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
                    Label status = new Label(item.getStatut());
                    status.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
                    cell.getChildren().addAll(title, status);
                    cell.setPadding(new Insets(6, 4, 6, 4));
                    setGraphic(cell);
                    setText(null);
                }
            }
        });
    }

    private void loadTickets() {
        allTickets = ticketDAO.findAll();
        ticketsList.setItems(FXCollections.observableArrayList(allTickets));
    }

    private void filterTickets(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            ticketsList.setItems(FXCollections.observableArrayList(allTickets));
        } else {
            String lower = keyword.trim().toLowerCase();
            List<Ticket> filtered = allTickets.stream()
                    .filter(t -> t.getType() != null &&
                            t.getType().toLowerCase().contains(lower))
                    .toList();
            ticketsList.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        if (selectedTicket == null) return;

        List<Message> messages = messageService.getMessagesForTicket(selectedTicket.getId());

        String currentRole = (Session.currentUser != null && Session.currentUser.getRole() != null)
                ? Session.currentUser.getRole().toUpperCase()
                : "USER";

        if (messages.isEmpty()) {
            Label empty = new Label("Aucun message pour ce ticket. Commencez la conversation !");
            empty.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;");
            HBox emptyRow = new HBox(empty);
            emptyRow.setAlignment(Pos.CENTER);
            emptyRow.setPadding(new Insets(40, 0, 0, 0));
            messagesContainer.getChildren().add(emptyRow);
            return;
        }

        for (Message msg : messages) {
            messagesContainer.getChildren().add(buildMessageBubble(msg, currentRole));
        }

        messagesContainer.layout();
        messagesScrollPane.layout();
        messagesScrollPane.setVvalue(1.0);
    }

    private HBox buildMessageBubble(Message msg, String currentRole) {
        boolean isMe = msg.getSenderRole() != null &&
                msg.getSenderRole().equalsIgnoreCase(currentRole);

        Label bubble = new Label(msg.getContent());
        bubble.setWrapText(true);
        bubble.setMaxWidth(350);
        bubble.setPadding(new Insets(10, 14, 10, 14));

        if (isMe) {
            bubble.setStyle(
                    "-fx-background-color: #2e8b57;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18 18 4 18;" +
                            "-fx-font-size: 13px;"
            );
        } else {
            bubble.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #2D3748;" +
                            "-fx-background-radius: 18 18 18 4;" +
                            "-fx-font-size: 13px;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 4, 0, 0, 1);"
            );
        }

        Label time = new Label(msg.getSentAt() != null ? msg.getSentAt().format(TIME_FMT) : "");
        time.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");

        Label roleTag = new Label(isMe ? "Vous" : msg.getSenderRole());
        roleTag.setStyle("-fx-font-size: 10px; -fx-text-fill: #718096; -fx-font-weight: bold;");

        VBox bubbleBox = new VBox(2);
        bubbleBox.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubbleBox.getChildren().addAll(roleTag, bubble, time);

        HBox row = new HBox(bubbleBox);
        row.setPadding(new Insets(4, 12, 4, 12));
        row.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        return row;
    }

    @FXML
    private void onSendMessage() {
        if (selectedTicket == null) return;
        String text = messageInput.getText();
        if (text == null || text.trim().isEmpty()) return;

        String role = (Session.currentUser != null && Session.currentUser.getRole() != null)
                ? Session.currentUser.getRole().toUpperCase()
                : "USER";

        boolean sent = messageService.sendMessage(selectedTicket.getId(), text.trim(), role);
        if (sent) {
            messageInput.clear();
            loadMessages();
        }
    }
}