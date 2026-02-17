# Guide des Tests Unitaires - CRUD Operations

## 📚 Introduction aux Tests Unitaires

Les tests unitaires vérifient que chaque partie de votre code fonctionne correctement de manière isolée.

### Avantages :
- ✅ Détection précoce des bugs
- ✅ Documentation du code
- ✅ Facilite la refactorisation
- ✅ Améliore la qualité du code
- ✅ Confiance lors des modifications

## 🛠️ Configuration Requise

### Dépendances Maven (pom.xml)

```xml
<dependencies>
    <!-- JUnit 5 (Jupiter) -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito pour les mocks -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.5.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>5.5.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- H2 Database pour tests -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ pour assertions fluides -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Maven Surefire Plugin pour exécuter les tests -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
        </plugin>
    </plugins>
</build>
```

## 📁 Structure des Tests

```
src/test/java/
└── org/example/
    ├── dao/              # Tests des DAO (Data Access Objects)
    │   ├── ForumDAOTest.java
    │   ├── PostDAOTest.java
    │   ├── UserDAOTest.java
    │   └── CommentDAOTest.java
    ├── service/          # Tests des services
    │   ├── ForumServiceTest.java
    │   └── PostServiceTest.java
    ├── badge/            # Tests du système de badges
    │   └── BadgeManagerTest.java
    ├── ai/               # Tests du moteur AI
    │   └── RecommendationEngineTest.java
    └── util/             # Tests des utilitaires
        └── AnimationUtilsTest.java
```

## 🎯 Types de Tests

### 1. Tests Unitaires Purs
Testent une méthode isolée sans dépendances externes.

### 2. Tests d'Intégration
Testent l'interaction avec la base de données.

### 3. Tests de Mocking
Utilisent des objets simulés (mocks) pour isoler le code testé.

## 📝 Conventions de Nommage

### Méthodes de Test
```java
@Test
void methodName_StateUnderTest_ExpectedBehavior()

// Exemples :
void createForum_WithValidData_ShouldReturnForumId()
void deleteForum_WithInvalidId_ShouldThrowException()
void getForum_WhenNotExists_ShouldReturnNull()
```

### Classes de Test
```java
// Classe à tester : ForumDAO
// Classe de test : ForumDAOTest

// Classe à tester : BadgeManager
// Classe de test : BadgeManagerTest
```

## 🧪 Patterns de Test

### Pattern AAA (Arrange-Act-Assert)

```java
@Test
void createPost_WithValidData_ShouldReturnPostId() {
    // Arrange (Préparer)
    String title = "Test Post";
    String content = "Test Content";
    int authorId = 1;
    int forumId = 1;
    
    // Act (Agir)
    int postId = postDAO.createPost(title, content, authorId, forumId);
    
    // Assert (Vérifier)
    assertThat(postId).isGreaterThan(0);
}
```

### Pattern Given-When-Then (BDD)

```java
@Test
void shouldAwardBadgeWhenUserVotes5Times() {
    // Given (Étant donné)
    int userId = 1;
    int forumId = 1;
    voteOnPosts(userId, forumId, 4); // 4 votes déjà faits
    
    // When (Quand)
    voteOnPost(userId, forumId, 5); // 5ème vote
    
    // Then (Alors)
    assertThat(badgeManager.getUserBadges(userId))
        .extracting("name")
        .contains("Fan du Forum");
}
```

## 🔧 Annotations JUnit 5

```java
@Test                    // Marque une méthode de test
@BeforeEach             // Exécuté avant chaque test
@AfterEach              // Exécuté après chaque test
@BeforeAll              // Exécuté une fois avant tous les tests
@AfterAll               // Exécuté une fois après tous les tests
@Disabled               // Désactive un test
@DisplayName("...")     // Nom personnalisé du test
@Timeout(5)             // Timeout en secondes
@RepeatedTest(10)       // Répète le test 10 fois
@ParameterizedTest      // Test avec paramètres
```

## 📊 Assertions Courantes

### JUnit 5
```java
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);
assertThrows(Exception.class, () -> method());
assertTimeout(Duration.ofSeconds(1), () -> method());
```

### AssertJ (Recommandé)
```java
assertThat(actual).isEqualTo(expected);
assertThat(actual).isNotNull();
assertThat(list).hasSize(5);
assertThat(list).contains("item");
assertThat(string).startsWith("prefix");
assertThat(number).isGreaterThan(10);
```

## 🎭 Mocking avec Mockito

### Créer un Mock
```java
@Mock
private Connection mockConnection;

@Mock
private PreparedStatement mockStatement;

@Mock
private ResultSet mockResultSet;
```

### Définir le Comportement
```java
when(mockResultSet.next()).thenReturn(true, false);
when(mockResultSet.getInt("id")).thenReturn(1);
when(mockResultSet.getString("name")).thenReturn("Test Forum");
```

### Vérifier les Appels
```java
verify(mockStatement).setInt(1, forumId);
verify(mockStatement).executeUpdate();
verify(mockStatement, times(1)).close();
verify(mockStatement, never()).executeQuery();
```

## 🗄️ Base de Données de Test (H2)

### Configuration
```java
@BeforeEach
void setUp() throws SQLException {
    // Créer une base de données H2 en mémoire
    connection = DriverManager.getConnection(
        "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "sa",
        ""
    );
    
    // Créer les tables
    createTables();
    
    // Insérer des données de test
    insertTestData();
}

@AfterEach
void tearDown() throws SQLException {
    if (connection != null) {
        connection.close();
    }
}
```

## 🚀 Exécution des Tests

### Via Maven
```bash
# Exécuter tous les tests
mvn test

# Exécuter une classe de test spécifique
mvn test -Dtest=ForumDAOTest

# Exécuter une méthode de test spécifique
mvn test -Dtest=ForumDAOTest#createForum_WithValidData_ShouldReturnForumId

# Exécuter avec rapport de couverture
mvn test jacoco:report
```

### Via IDE
- IntelliJ IDEA : Clic droit sur la classe/méthode → Run Test
- Eclipse : Clic droit → Run As → JUnit Test

## 📈 Couverture de Code

### JaCoCo Plugin
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Objectifs de Couverture
- ✅ 80%+ pour le code critique (DAO, Services)
- ✅ 60%+ pour les contrôleurs
- ✅ 40%+ pour l'UI

## 🎯 Bonnes Pratiques

### ✅ À Faire
1. **Un test = une assertion principale**
2. **Tests indépendants** (pas d'ordre d'exécution)
3. **Noms descriptifs** (on doit comprendre sans lire le code)
4. **Tests rapides** (< 1 seconde par test)
5. **Données de test isolées** (pas de dépendance à la DB réelle)
6. **Nettoyer après chaque test** (@AfterEach)

### ❌ À Éviter
1. Tests qui dépendent d'autres tests
2. Tests qui modifient la base de données de production
3. Tests avec logique complexe
4. Tests qui testent le framework (JUnit, JavaFX)
5. Tests qui dépendent de l'ordre d'exécution
6. Tests sans assertions

## 📚 Exemples Complets

Voir les fichiers de test créés :
- `ForumDAOTest.java` - Tests CRUD des forums
- `PostDAOTest.java` - Tests CRUD des posts
- `BadgeManagerTest.java` - Tests du système de badges
- `RecommendationEngineTest.java` - Tests du moteur AI

## 🔍 Debugging des Tests

### Afficher les Logs
```java
@Test
void testWithLogs() {
    System.out.println("Debug: Starting test");
    // ... test code ...
    System.out.println("Debug: Test completed");
}
```

### Utiliser @DisplayName
```java
@Test
@DisplayName("Devrait créer un forum avec des données valides")
void createForum_WithValidData_ShouldReturnForumId() {
    // ...
}
```

## 📊 Rapport de Test

Après `mvn test`, consultez :
- `target/surefire-reports/` - Rapports XML et TXT
- `target/site/jacoco/` - Rapport de couverture HTML

## 🎓 Ressources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [H2 Database](http://www.h2database.com/)

---

**Prochaine étape** : Consultez les fichiers de test créés pour voir des exemples concrets !
