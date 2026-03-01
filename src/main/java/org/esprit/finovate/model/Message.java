package org.esprit.finovate.model;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long idTicket;
    private String content;
    private LocalDateTime sentAt;
    private String senderRole; // "USER" or "ADMIN"

    public Message() {}

    public Message(Long idTicket, String content, String senderRole) {
        this.idTicket = idTicket;
        this.content = content;
        this.senderRole = senderRole;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdTicket() { return idTicket; }
    public void setIdTicket(Long idTicket) { this.idTicket = idTicket; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    @Override
    public String toString() {
        return "Message #" + id +
                "\n└─ Ticket: #" + idTicket +
                "\n└─ Sender: " + senderRole +
                "\n└─ Content: " + content +
                "\n└─ Sent: " + sentAt;
    }
}