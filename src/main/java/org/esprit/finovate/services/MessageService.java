
package org.esprit.finovate.services;

import org.esprit.finovate.dao.MessageDAO;
import org.esprit.finovate.model.Message;

import java.util.List;

public class MessageService {

    private final MessageDAO messageDAO = new MessageDAO();

    public boolean sendMessage(Long ticketId, String content, String senderRole) {
        if (content == null || content.trim().isEmpty()) return false;
        Message m = new Message(ticketId, content.trim(), senderRole);
        return messageDAO.create(m);
    }

    public List<Message> getMessagesForTicket(Long ticketId) {
        return messageDAO.findByTicketId(ticketId);
    }
}