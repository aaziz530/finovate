# Guide d'Exécution des Tests Unitaires

## 🚀 Exécution Rapide

### Via Maven (Ligne de Commande)

```bash
# Exécuter tous les tests
mvn test

# Exécuter avec affichage détaillé
mvn test -X

# Exécuter une classe de test spécifique
mvn test -Dtest=ForumDAOTest

# Exécuter une méthode de test spécifique
mvn test -Dtest=ForumDAOTest#createForum_WithValidData_ShouldReturnForumId

# Exécuter plusieurs classes
mvn test -Dtest=ForumDAOTest,BadgeManagerTest

# Ignorer les échecs et continuer
mvn test -Dmaven.test.failure.ignore=true

# Exécuter en mode silencieux
mvn test -q
```

### Via IntelliJ IDEA

1. **Exécuter tous les tests** :
   - Clic droit sur `src/test/java` → Run 'All Tests'
   - Ou : Ctrl+Shift+F10

2. **Exécuter une classe de test** :
   - Ouvrir la classe (ex: ForumDAOTest.java)
   - Clic droit → Run 'ForumDAOTest'
   - Ou : Ctrl+Shift+F10

3. **Exécuter une méthode de test** :
   - Cliquer sur l'icône verte à côté de @Test
   - Ou : Ctrl+Shift+F10 avec le curseur sur la méthode

4. **Exécuter avec couverture** :
   - Clic droit → Run 'ForumDAOTest' with Coverage
   - Ou : Ctrl+Shift+F10 + Alt+Shift+F6

### Via Eclipse

1. Clic droit sur le projet → Run As → JUnit Test
2. Ou : Alt+Shift+X, T

## 📊 Résultats des Tests

### Format Console

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.example.dao.ForumDAOTest
[INFO] Tests run: 16, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.345 s
[INFO] Running org.example.badge.BadgeManagerTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.234 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Rapports HTML

Après `mvn test`, consultez :
- `target/surefire-reports/index.html` - Rapport principal
- `target/surefire-reports/*.xml` - Rapports XML
- `target/surefire-reports/*.txt` - Rapports texte

## 🎯 Tests Créés

### 1. ForumDAOTest (16 tests)

**Tests CREATE (3 tests)** :
- ✅ createForum_WithValidData_ShouldReturnForumId
- ✅ createForum_WithInvalidCreatorId_ShouldThrowException
- ✅ createForum_MultipleForums_ShouldReturnDifferentIds

**Tests READ (5 tests)** :
- ✅ getForumById_WithValidId_ShouldReturnForum
- ✅ getForumById_WithInvalidId_ShouldReturnNull
- ✅ getAllForums_ShouldReturnAllForums
- ✅ getAllForums_WhenNoForums_ShouldReturnEmptyList
- ✅ getForumsByCreator_ShouldReturnCreatorForums

**Tests UPDATE (2 tests)** :
- ✅ updateForum_WithValidData_ShouldReturnTrue
- ✅ updateForum_WithInvalidId_ShouldReturnFalse

**Tests DELETE (3 tests)** :
- ✅ deleteForum_WithValidId_ShouldReturnTrue
- ✅ deleteForum_WithWrongCreator_ShouldReturnFalse
- ✅ deleteForum_WithInvalidId_ShouldReturnFalse

**Tests Utilitaires (3 tests)** :
- ✅ forumExists_WithValidId_ShouldReturnTrue
- ✅ getMemberCount_ShouldReturnCorrectCount
- ✅ getMemberCount_WithNoMembers_ShouldReturnZero

### 2. BadgeManagerTest (10 tests)

**Tests Vote Badges (3 tests)** :
- ✅ countUserVotesInForum_ShouldReturnCorrectCount
- ✅ checkVoteBadges_After5Votes_ShouldAwardFanBadge
- ✅ checkVoteBadges_WithLessThan5Votes_ShouldNotAwardBadge

**Tests Post/Comment/Share (3 tests)** :
- ✅ countUserPosts_ShouldReturnCorrectCount
- ✅ countUserComments_ShouldReturnCorrectCount
- ✅ countUserShares_ShouldReturnCorrectCount

**Tests Attribution (4 tests)** :
- ✅ userHasBadge_ShouldReturnCorrectStatus
- ✅ awardBadge_Twice_ShouldNotDuplicate
- ✅ getUserBadges_ShouldReturnAllBadges
- ✅ badges_ShouldBeDifferentPerForum

## 📈 Couverture de Code

### Générer le Rapport de Couverture

```bash
# Avec JaCoCo
mvn clean test jacoco:report

# Consulter le rapport
# Ouvrir : target/site/jacoco/index.html
```

### Objectifs de Couverture

- **ForumDAO** : 90%+ (code critique)
- **BadgeManager** : 80%+ (logique métier)
- **Controllers** : 60%+ (UI logic)

## 🐛 Debugging des Tests

### Afficher les Logs

```java
@Test
void myTest() {
    System.out.println("Debug: Starting test");
    // ... test code ...
    System.out.println("Debug: Value = " + value);
}
```

### Utiliser @Disabled

```java
@Test
@Disabled("Test temporairement désactivé - bug #123")
void problematicTest() {
    // ...
}
```

### Timeout

```java
@Test
@Timeout(5) // 5 secondes max
void slowTest() {
    // ...
}
```

### Répéter un Test

```java
@RepeatedTest(10)
void flakeyTest() {
    // Exécuté 10 fois
}
```

## 🔧 Configuration Maven

### Ajouter les Dépendances

Créez ou modifiez `pom.xml` à la racine du projet :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>fintechforum</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- JUnit 5 -->
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

        <!-- AssertJ -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.24.2</version>
            <scope>test</scope>
        </dependency>

        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
            <scope>test</scope>
        </dependency>

        <!-- MySQL Connector (production) -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>17.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>17.0.2</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Surefire Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>

            <!-- JaCoCo Plugin -->
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
        </plugins>
    </build>
</project>
```

## ✅ Checklist Avant de Commiter

- [ ] Tous les tests passent (`mvn test`)
- [ ] Couverture de code > 70% (`mvn jacoco:report`)
- [ ] Pas de tests @Disabled sans raison
- [ ] Pas de System.out.println() dans les tests
- [ ] Noms de tests descriptifs
- [ ] Tests indépendants (pas d'ordre requis)

## 🎯 Bonnes Pratiques

### ✅ À Faire

1. **Tester les cas limites** (null, vide, négatif)
2. **Un test = une assertion principale**
3. **Noms descriptifs** (methodName_StateUnderTest_ExpectedBehavior)
4. **Tests rapides** (< 1 seconde)
5. **Nettoyer après chaque test** (@AfterEach)
6. **Utiliser AssertJ** pour des assertions lisibles

### ❌ À Éviter

1. Tests qui dépendent d'autres tests
2. Tests qui modifient la DB de production
3. Tests avec logique complexe
4. Tests sans assertions
5. Tests qui testent le framework
6. Ignorer les tests qui échouent

## 📚 Exemples d'Assertions

### AssertJ (Recommandé)

```java
// Égalité
assertThat(actual).isEqualTo(expected);
assertThat(actual).isNotEqualTo(unexpected);

// Null
assertThat(object).isNull();
assertThat(object).isNotNull();

// Booléens
assertThat(condition).isTrue();
assertThat(condition).isFalse();

// Nombres
assertThat(number).isGreaterThan(10);
assertThat(number).isLessThan(100);
assertThat(number).isBetween(10, 100);

// Strings
assertThat(string).startsWith("prefix");
assertThat(string).endsWith("suffix");
assertThat(string).contains("substring");
assertThat(string).isEmpty();

// Collections
assertThat(list).hasSize(5);
assertThat(list).contains("item");
assertThat(list).containsExactly("a", "b", "c");
assertThat(list).isEmpty();

// Exceptions
assertThatThrownBy(() -> method())
    .isInstanceOf(SQLException.class)
    .hasMessageContaining("error");
```

## 🚨 Problèmes Courants

### Problème 1 : Tests ne s'exécutent pas

**Solution** :
```bash
# Vérifier que Maven trouve les tests
mvn test -X

# Vérifier la structure
# Les tests doivent être dans src/test/java
# Les noms doivent finir par Test (ex: ForumDAOTest)
```

### Problème 2 : H2 Database erreur

**Solution** :
```java
// Vérifier l'URL de connexion
"jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL"

// MODE=MySQL pour compatibilité MySQL
```

### Problème 3 : Tests échouent aléatoirement

**Cause** : Tests dépendants ou données non nettoyées

**Solution** :
```java
@BeforeEach
void setUp() {
    cleanDatabase(); // Nettoyer avant chaque test
    insertTestData(); // Données fraîches
}
```

## 📊 Rapport de Test Exemple

```
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0

ForumDAOTest
  ✓ createForum_WithValidData_ShouldReturnForumId (0.123s)
  ✓ createForum_WithInvalidCreatorId_ShouldThrowException (0.045s)
  ✓ getForumById_WithValidId_ShouldReturnForum (0.067s)
  ... (13 more tests)

BadgeManagerTest
  ✓ countUserVotesInForum_ShouldReturnCorrectCount (0.089s)
  ✓ checkVoteBadges_After5Votes_ShouldAwardFanBadge (0.112s)
  ... (8 more tests)

Total time: 3.456s
```

## 🎓 Prochaines Étapes

1. Exécuter les tests : `mvn test`
2. Consulter les rapports : `target/surefire-reports/`
3. Vérifier la couverture : `mvn jacoco:report`
4. Ajouter plus de tests pour PostDAO, CommentDAO, etc.

Bon test ! 🚀
