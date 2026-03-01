package org.esprit.finovate.services;

import java.util.List;

/**
 * Service d'assistant IA pour conversations
 */
public class AIAssistantService {
    
    public static class Message {
        public String role; // "user" or "assistant"
        public String content;
        public long timestamp;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Envoyer un message à l'assistant IA
     * @param userMessage Message de l'utilisateur
     * @param conversationHistory Historique de la conversation
     * @return Réponse de l'IA
     */
    public static String chat(String userMessage, List<Message> conversationHistory) {
        // Construire le contexte avec l'historique
        StringBuilder context = new StringBuilder();
        context.append("Tu es un assistant IA pour un forum fintech. ");
        context.append("Tu aides les utilisateurs avec leurs questions sur le forum, la finance, la blockchain, etc.\n\n");
        
        // Ajouter l'historique récent (5 derniers messages)
        int startIndex = Math.max(0, conversationHistory.size() - 5);
        for (int i = startIndex; i < conversationHistory.size(); i++) {
            Message msg = conversationHistory.get(i);
            context.append(msg.role.equals("user") ? "Utilisateur: " : "Assistant: ");
            context.append(msg.content).append("\n");
        }
        
        // Ajouter le nouveau message
        context.append("Utilisateur: ").append(userMessage).append("\n");
        context.append("Assistant: ");
        
        // Appeler Groq
        return GroqService.callGroq(context.toString());
    }
    
    /**
     * Obtenir une réponse rapide sans historique
     * @param question Question de l'utilisateur
     * @return Réponse de l'IA
     */
    public static String quickAnswer(String question) {
        String prompt = "Tu es un assistant IA pour un forum fintech. Réponds de manière concise et utile.\n\n" +
                "Question: " + question + "\n" +
                "Réponse: ";
        
        return GroqService.callGroq(prompt);
    }
}
