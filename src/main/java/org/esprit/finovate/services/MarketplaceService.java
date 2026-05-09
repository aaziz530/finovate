package org.esprit.finovate.services;

import org.esprit.finovate.entities.Ad;
import org.esprit.finovate.entities.Product;
import org.esprit.finovate.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;

public class MarketplaceService {

    // --- PARTIE FRONT OFFICE (Lecture & Transaction) ---

    // Récupérer les points (Table 'user')
    public int getUserPoints(Long userId) {
        String query = "SELECT points FROM user WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setLong(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt("points");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Récupérer tous les produits
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("price_points"),
                        rs.getString("image"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return products;
    }

    // Acheter un produit
    public boolean buyProduct(Long userId, Product product) {
        int currentPoints = getUserPoints(userId);
        if (currentPoints >= product.getPricePoints()) {
            // Transaction : Débiter points User ET Décrémenter Stock Produit
            String updateUser = "UPDATE user SET points = points - ? WHERE id = ?";

            try (Connection conn = MyDataBase.getInstance().getConnection();
                 PreparedStatement pst = conn.prepareStatement(updateUser)) {
                pst.setInt(1, product.getPricePoints());
                pst.setLong(2, userId);
                return pst.executeUpdate() > 0;
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    public Ad getRandomAd() {
        String query = "SELECT * FROM ad ORDER BY RAND() LIMIT 1";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return new Ad(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("image_path"),
                        rs.getInt("duration"),
                        rs.getInt("reward_points")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // --- PARTIE BACK OFFICE (CRUD ADMIN) ---

    // CREATE : Ajouter un produit
    public void addProduct(Product p) {
        String query = "INSERT INTO product (name, description, price_points, image, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, p.getName());
            pst.setString(2, p.getDescription());
            pst.setInt(3, p.getPricePoints());
            pst.setString(4, p.getImage());
            pst.setInt(5, p.getStock());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // UPDATE : Modifier un produit
    public void updateProduct(Product p) {
        String query = "UPDATE product SET name=?, description=?, price_points=?, image=?, stock=? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, p.getName());
            pst.setString(2, p.getDescription());
            pst.setInt(3, p.getPricePoints());
            pst.setString(4, p.getImage());
            pst.setInt(5, p.getStock());
            pst.setInt(6, p.getId());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // DELETE : Supprimer un produit
    public void deleteProduct(int id) {
        String query = "DELETE FROM product WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    // --- NOUVELLE VERSION DE watchAd ---
    public void watchAd(Long userId, Ad ad) {
        // 1. Ajouter les points à l'utilisateur
        String queryPoints = "UPDATE user SET points = points + ? WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(queryPoints)) {
            pst.setInt(1, ad.getRewardPoints());
            pst.setLong(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Enregistrer le clic dans l'historique pour entraîner l'IA (NOUVEAU)
        String queryClick = "INSERT INTO user_ad_click (user_id, ad_id, clicked_at) VALUES (?, ?, NOW())";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(queryClick)) {
            pst.setLong(1, userId);
            pst.setInt(2, ad.getId());
            pst.executeUpdate();
            System.out.println("Clic enregistré dans l'historique pour l'IA !");
        } catch (SQLException e) { e.printStackTrace(); }
    }
    // --- GESTION DES ADS (ADMIN) ---

    // READ : Récupérer toutes les pubs pour la liste
    public List<Ad> getAllAds() {
        List<Ad> ads = new ArrayList<>();
        String query = "SELECT * FROM ad";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                ads.add(new Ad(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("image_path"),
                        rs.getInt("duration"),
                        rs.getInt("reward_points")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ads;
    }

    // CREATE : Ajouter une pub
    public void addAd(Ad ad) {
        String query = "INSERT INTO ad (title, image_path, duration, reward_points) VALUES (?, ?, ?, ?)";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, ad.getTitle());
            pst.setString(2, ad.getImagePath());
            pst.setInt(3, ad.getDuration());
            pst.setInt(4, ad.getRewardPoints());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // UPDATE : Modifier une pub
    public void updateAd(Ad ad) {
        String query = "UPDATE ad SET title=?, image_path=?, duration=?, reward_points=? WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, ad.getTitle());
            pst.setString(2, ad.getImagePath());
            pst.setInt(3, ad.getDuration());
            pst.setInt(4, ad.getRewardPoints());
            pst.setInt(5, ad.getId()); // ID pour le WHERE
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // DELETE : Supprimer une pub
    public void deleteAd(int id) {
        String query = "DELETE FROM ad WHERE id=?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Nouvelle méthode 1 : Récupérer une pub spécifique par son ID
    public Ad getAdById(int id) {
        String query = "SELECT * FROM ad WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Ad(
                        rs.getInt("id"), rs.getString("title"), rs.getString("image_path"),
                        rs.getInt("duration"), rs.getInt("reward_points")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return getRandomAd(); // Sécurité : si la pub n'existe pas, on prend au hasard
    }

    // Nouvelle méthode 2 : La communication avec l'IA Python
    public Ad getSmartAdForUser(Long userId) {
        double solde = 0;
        int points = 0;
        int age = 25; // Âge par défaut

        // 1. Récupérer le profil financier complet de l'utilisateur (Table 'user')
        String userQuery = "SELECT solde, points, birthdate FROM user WHERE id = ?";
        try (Connection conn = MyDataBase.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(userQuery)) {
            pst.setLong(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                solde = rs.getDouble("solde");
                points = rs.getInt("points");
                Date birthdate = rs.getDate("birthdate");
                if (birthdate != null) {
                    // Calcul de l'âge automatique
                    age = Period.between(birthdate.toLocalDate(), LocalDate.now()).getYears();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // 2. Préparer le message JSON pour l'IA
        // Format voulu : {"solde": 500.0, "points": 100, "age": 25}
        String jsonInput = String.format(java.util.Locale.US,
                "{\"solde\": %.2f, \"points\": %d, \"age\": %d}", solde, points, age);

        // 3. Envoyer la requête HTTP (POST) à Flask
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/predict_ad"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            // Attendre la réponse de Python
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body(); // Ex: {"ad_id": 2}

            // 4. Lire l'ID de la pub renvoyée (Extraction simple du JSON)
            if (responseBody.contains("\"ad_id\"")) {
                String idStr = responseBody.replaceAll("[^0-9]", ""); // Garde uniquement le chiffre
                int predictedAdId = Integer.parseInt(idStr);

                System.out.println("L'IA recommande la pub ID : " + predictedAdId);
                return getAdById(predictedAdId);
            }

        } catch (Exception e) {
            System.out.println("API Flask éteinte ou erreur réseau. On utilise le hasard. " + e.getMessage());
        }

        // Si l'IA plante (ou que Flask est éteint), on utilise la méthode basique en secours
        return getRandomAd();
    }
}
