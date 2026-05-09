package org.esprit.finovate.entities;

import java.util.Date;

public class Transaction {
    private Long id;
    private Long sender_id;
    private Long receiver_id; // Can be null for bills or fees
    private String amount;
    private String type; // TRANSFER, BILL, GOAL_FUNDING, etc.
    private String description;
    private Date date;
    private String senderName;
    private String receiverName;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Transaction() {
    }

    public Transaction(Long sender_id, Long receiver_id, String amount, String type, String description) {
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public Transaction(Long id, Long sender_id, Long receiver_id, String amount, String type, String description,
            Date date) {
        this.id = id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return sender_id;
    }

    public void setSenderId(Long sender_id) {
        this.sender_id = sender_id;
    }

    public Long getReceiverId() {
        return receiver_id;
    }

    public void setReceiverId(Long receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
