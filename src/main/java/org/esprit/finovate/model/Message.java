package org.esprit.finovate.model;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long idTicket;
    private String content;
    private LocalDateTime sentAt;

    // Constructors
    public Message() {}

    public Message(Long idTicket, String content) {
        this.idTicket = idTicket;
        this.content = content;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdTicket() {
        return idTicket;
    }

    public void setIdTicket(Long idTicket) {
        this.idTicket = idTicket;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "Message #" + id +
                "\n└─ Ticket: #" + idTicket +
                "\n└─ Content: " + content +
                "\n└─ Sent: " + sentAt;
    }
}