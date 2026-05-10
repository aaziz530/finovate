# Finovate - Plateforme de Gestion Financière

Finovate est une application de gestion financière personnelle développée en JavaFX avec une interface moderne et des fonctionnalités avancées incluant l'intelligence artificielle.

## Fonctionnalités Principales

### Gestion des Utilisateurs
- Inscription et authentification sécurisées
- Réinitialisation du mot de passe par email
- Authentification OAuth (Google, GitHub)
- Authentification par reconnaissance faciale
- Profils utilisateurs avec image et informations personnelles

### Gestion Financière
- **Solde et Carte** : Consultation du solde, numéro de carte
- **Transferts d'argent** : Envoi d'argent vers d'autres comptes avec notification SMS
- **Paiement de factures** : Paiement via référence et montant
- **Alimentation de carte** : Recharge via Stripe (paiement sécurisé)
- **Objectifs d'épargne (Goals)** : Création et suivi d'objectifs financiers

### Marketplace
- Consultation des produits disponibles
- Gestion des produits (côté admin)
- Gestion des publicités (ads)

### Forums et Communauté
- Création de forums de discussion
- Publication de posts et commentaires
- Système de votes
- Modération de contenu

### Projets et Investissements
- Création de projets avec objectif de financement
- Investissement dans des projets
- Suivi des revenus quotidiens
- Statistiques d'investisseur

### Intelligence Artificielle
- **Assistant IA** : Chatbot pour conseils financiers
- **Génération de posts** : Création automatique de contenu via IA
- **Génération d'images** : Intégration HuggingFace/Pixabay
- **Résumé de contenu** : Service de summarization
- **Traduction** : Service de traduction automatique
- **Recommandations** : Moteur de recommandation personnalisé

### Notifications et Alertes
- Centre de notifications
- Alertes personnalisées
- Notifications SMS (Twilio)

### Export et Documents
- Export PDF des relevés et rapports
- Génération de documents financiers

## Architecture du Projet

```
src/main/java/org/esprit/finovate/
├── Main.java                 # Point d'entrée de l'application
├── api/                      # Services API externes
├── controllers/              # Contrôleurs JavaFX (47 fichiers)
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── TransferController.java
│   ├── TopUpController.java
│   ├── BillController.java
│   ├── GoalsController.java
│   ├── MarketplaceController.java
│   ├── ForumsController.java
│   ├── PostsController.java
│   └── ...
├── entities/                 # Entités JPA (14 classes)
│   ├── User.java
│   ├── Transaction.java
│   ├── Goal.java
│   ├── Bill.java
│   ├── Product.java
│   ├── Ad.java
│   ├── Post.java
│   ├── Comment.java
│   ├── Forum.java
│   ├── Project.java
│   ├── Investissement.java
│   └── ...
├── services/                 # Services métier (38 fichiers)
│   ├── UserService.java
│   ├── TransactionService.java
│   ├── StripeService.java
│   ├── GoalService.java
│   ├── BillService.java
│   ├── EmailService.java
│   ├── TwilioSmsService.java
│   ├── AIAssistantService.java
│   ├── PDFExportService.java
│   └── ...
├── utils/                    # Utilitaires
│   ├── MyDataBase.java
│   ├── Session.java
│   ├── ImageUtils.java
│   └── ...
└── tests/                    # Tests unitaires
```

## Technologies Utilisées

| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 17 | Langage principal |
| JavaFX | 21.0.2 | Interface graphique |
| MySQL | 8.0.33 | Base de données |
| Stripe | 24.0.0 | Paiements en ligne |
| Twilio | 10.1.0 | Notifications SMS |
| iText / PDFBox | 7.2.5 / 3.0.3 | Génération PDF |
| Jakarta Mail | 2.0.1 | Envoi d'emails |
| Gson / Jackson | 2.11.0 / 2.17.0 | Sérialisation JSON |
| dotenv-java | 3.0.0 | Configuration environnement |
| JUnit | 5.10.1 | Tests unitaires |

## Prérequis

- **Java JDK 17** ou supérieur
- **Maven 3.6+**
- **MySQL 8.0+**
- Compte **Stripe** (pour les paiements)
- Compte **Twilio** (pour les SMS)
- Configuration **OAuth** (Google, GitHub) - optionnel

## Installation et Configuration

### 1. Cloner le projet

```bash
git clone <repository-url>
cd finovate
```

### 2. Base de données

Créez la base de données MySQL et importez le schéma :

```sql
CREATE DATABASE finovate;
```

Configurez la connexion dans `src/main/java/org/esprit/finovate/utils/MyDataBase.java` :

```java
private static final String URL = "jdbc:mysql://localhost:3306/finovate";
private static final String USER = "votre_utilisateur";
private static final String PSR = "votre_mot_de_passe";
```

### 3. Fichier .env

Créez un fichier `.env` à la racine du projet avec les variables suivantes :

```env
# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...

# Twilio
TWILIO_ACCOUNT_SID=...
TWILIO_AUTH_TOKEN=...
TWILIO_PHONE_NUMBER=...

# OAuth Google (optionnel)
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...

# OAuth GitHub (optionnel)
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

### 4. Configuration Email

Pour la réinitialisation du mot de passe, configurez `EmailService.java` avec vos identifiants SMTP :

```java
private final String username = "votre_email@gmail.com";
private final String password = "votre_app_password";
```

### 5. Compiler et Lancer

```bash
mvn clean install
mvn javafx:run
```

## Utilisation

### Premier démarrage

1. Lancez l'application avec `mvn javafx:run`
2. Créez un compte utilisateur
3. Connectez-vous avec vos identifiants

### Fonctionnalités principales

- **Tableau de bord** : Vue d'ensemble de vos finances
- **Transfert** : Envoyez de l'argent à d'autres utilisateurs
- **Alimentation** : Rechargez votre carte via Stripe
- **Factures** : Payez vos factures en ligne
- **Objectifs** : Créez des goals d'épargne
- **Marketplace** : Parcourez les produits disponibles
- **Forums** : Participez aux discussions
- **Projets** : Créez ou investissez dans des projets

## Structure de la Base de Données

### Tables principales

- **user** : Comptes utilisateurs
- **transaction** : Historique des transactions
- **goal** : Objectifs d'épargne
- **bill** : Factures payées
- **product** : Produits du marketplace
- **ad** : Publicités
- **post** : Publications des forums
- **comment** : Commentaires sur les posts
- **forum** : Forums de discussion
- **project** : Projets de financement
- **investissement** : Investissements des utilisateurs

## API et Services Externes

### Stripe (Paiements)
- Création de sessions de paiement
- Vérification des paiements
- Webhooks pour confirmation

### Twilio (SMS)
- Notifications de transfert
- Alertes de sécurité

### Services IA
- **Groq API** : Chatbot et génération de texte
- **HuggingFace** : Génération d'images
- **Pixabay** : Recherche d'images

## Sécurité

- Mots de passe hashés avec SHA-256
- Validation des entrées utilisateur
- Gestion des sessions
- Authentification OAuth sécurisée
- Tokens Stripe pour paiements

## Tests

Exécuter les tests unitaires :

```bash
mvn test
```

## Dépannage

### Erreurs courantes

| Erreur | Solution |
|--------|----------|
| `Database connection is null` | Vérifiez la configuration MySQL |
| `Column 'id' doesn't have a default value` | Les IDs sont générés manuellement |
| `Stripe not initialized` | Vérifiez le fichier `.env` |
| `Email not sent` | Configurez correctement SMTP |



## Licence

Ce projet est à des fins éducatives.