# Exécuter les Tests dans IntelliJ IDEA (Sans Maven CLI)

## 🎯 Méthode 1 : Exécuter les Tests via l'Interface IntelliJ

### Option A : Exécuter TOUS les Tests

1. **Ouvrir la vue Project** (Alt+1)
2. **Naviguer vers** `src/test/java`
3. **Clic droit** sur le dossier `java`
4. **Sélectionner** "Run 'All Tests'"
5. **Ou utiliser le raccourci** : Ctrl+Shift+F10

### Option B : Exécuter une Classe de Test

1. **Ouvrir** `ForumDAOTest.java` ou `BadgeManagerTest.java`
2. **Clic droit** n'importe où dans le fichier
3. **Sélectionner** "Run 'ForumDAOTest'"
4. **Ou cliquer** sur l'icône verte ▶️ à côté du nom de la classe
5. **Ou utiliser** : Ctrl+Shift+F10

### Option C : Exécuter un Test Spécifique

1. **Ouvrir** la classe de test
2. **Trouver** la méthode de test (ex: `createForum_WithValidData_ShouldReturnForumId`)
3. **Cliquer** sur l'icône verte ▶️ à côté de `@Test`
4. **Ou** : Ctrl+Shift+F10 avec le curseur sur la méthode

### Option D : Exécuter avec Couverture de Code

1. **Clic droit** sur `src/test/java` ou une classe de test
2. **Sélectionner** "Run 'All Tests' with Coverage"
3. **Ou utiliser** : Ctrl+Shift+F10 puis Alt+Shift+F6

---

## 🎯 Méthode 2 : Via le Menu Run

1. **Menu** → Run → Run...
2. **Sélectionner** le test à exécuter
3. **Ou** : Alt+Shift+F10

---

## 🎯 Méthode 3 : Via la Fenêtre Maven (Si Maven est configuré dans IntelliJ)

1. **Ouvrir la vue Maven** (View → Tool Windows → Maven)
2. **Développer** votre projet → Lifecycle
3. **Double-cliquer** sur "test"

---

## 📊 Voir les Résultats

### Fenêtre de Résultats

Après l'exécution, une fenêtre s'ouvre en bas avec :
- ✅ Tests réussis (vert)
- ❌ Tests échoués (rouge)
- ⚠️ Tests ignorés (jaune)
- ⏱️ Temps d'exécution

### Exemple de Résultat

```
ForumDAOTest
  ✓ createForum_WithValidData_ShouldReturnForumId (123ms)
  ✓ createForum_WithInvalidCreatorId_ShouldThrowException (45ms)
  ✓ getForumById_WithValidId_ShouldReturnForum (67ms)
  ... (13 more tests)

BadgeManagerTest
  ✓ countUserVotesInForum_ShouldReturnCorrectCount (89ms)
  ✓ checkVoteBadges_After5Votes_ShouldAwardFanBadge (112ms)
  ... (8 more tests)

Tests passed: 26 of 26 tests - 3.456s
```

---

## 🐛 Debugging des Tests

### Exécuter en Mode Debug

1. **Clic droit** sur le test
2. **Sélectionner** "Debug 'ForumDAOTest'"
3. **Ou cliquer** sur l'icône debug 🐛 à côté du test
4. **Ou utiliser** : Ctrl+Shift+F9

### Ajouter des Breakpoints

1. **Cliquer** dans la marge gauche à côté d'une ligne
2. **Un point rouge** apparaît
3. **Exécuter en mode debug**
4. **Le programme s'arrête** au breakpoint

---

## ⚙️ Configuration IntelliJ pour les Tests

### Vérifier que JUnit est Configuré

1. **File** → Project Structure (Ctrl+Alt+Shift+S)
2. **Modules** → Votre module → Dependencies
3. **Vérifier** que JUnit 5 est présent
4. **Si absent** : Clic sur + → Library → From Maven
5. **Rechercher** : `org.junit.jupiter:junit-jupiter:5.10.0`

### Configurer le Test Runner

1. **File** → Settings (Ctrl+Alt+S)
2. **Build, Execution, Deployment** → Build Tools → Maven → Runner
3. **Cocher** "Delegate IDE build/run actions to Maven"
4. **Ou** : Utiliser IntelliJ IDEA (plus rapide)

---

## 📈 Rapport de Couverture

### Générer le Rapport

1. **Exécuter** les tests avec couverture (voir Option D ci-dessus)
2. **Une fenêtre** s'ouvre avec les statistiques
3. **Voir** :
   - % de lignes couvertes
   - % de méthodes couvertes
   - % de classes couvertes

### Visualiser la Couverture

- **Lignes vertes** = couvertes par les tests
- **Lignes rouges** = non couvertes
- **Lignes jaunes** = partiellement couvertes

---

## 🔧 Résolution de Problèmes

### Problème 1 : "Cannot resolve symbol JUnit"

**Solution** :
1. File → Project Structure → Libraries
2. Ajouter JUnit 5 :
   - Clic sur + → From Maven
   - Rechercher : `org.junit.jupiter:junit-jupiter-api:5.10.0`
   - Rechercher : `org.junit.jupiter:junit-jupiter-engine:5.10.0`

### Problème 2 : "No tests found"

**Solution** :
1. Vérifier que la classe se termine par `Test` (ex: `ForumDAOTest`)
2. Vérifier que les méthodes ont `@Test`
3. Vérifier que la classe est dans `src/test/java`
4. File → Invalidate Caches → Invalidate and Restart

### Problème 3 : "H2 Database not found"

**Solution** :
1. File → Project Structure → Libraries
2. Ajouter H2 :
   - Clic sur + → From Maven
   - Rechercher : `com.h2database:h2:2.2.224`

### Problème 4 : "AssertJ not found"

**Solution** :
1. File → Project Structure → Libraries
2. Ajouter AssertJ :
   - Clic sur + → From Maven
   - Rechercher : `org.assertj:assertj-core:3.24.2`

---

## 📦 Ajouter les Dépendances Manuellement

Si vous n'avez pas de `pom.xml`, ajoutez les JARs manuellement :

### Télécharger les JARs

1. **JUnit 5** :
   - https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-api/5.10.0/
   - https://repo1.maven.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.10.0/

2. **H2 Database** :
   - https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/

3. **AssertJ** :
   - https://repo1.maven.org/maven2/org/assertj/assertj-core/3.24.2/

### Ajouter à IntelliJ

1. **File** → Project Structure (Ctrl+Alt+Shift+S)
2. **Libraries** → + → Java
3. **Sélectionner** les JARs téléchargés
4. **Appliquer** → OK

---

## 🎯 Raccourcis Clavier Utiles

| Action | Raccourci |
|--------|-----------|
| Exécuter le test | Ctrl+Shift+F10 |
| Débugger le test | Ctrl+Shift+F9 |
| Réexécuter le dernier test | Shift+F10 |
| Réexécuter en debug | Shift+F9 |
| Arrêter l'exécution | Ctrl+F2 |
| Voir les résultats | Alt+4 |

---

## 📊 Exemple de Session de Test

### Étape 1 : Ouvrir ForumDAOTest.java

```
src/test/java/org/example/dao/ForumDAOTest.java
```

### Étape 2 : Clic droit → Run 'ForumDAOTest'

### Étape 3 : Voir les Résultats

```
✓ All 16 tests passed in 2.345s

CREATE Tests (3/3 passed)
  ✓ createForum_WithValidData_ShouldReturnForumId
  ✓ createForum_WithInvalidCreatorId_ShouldThrowException
  ✓ createForum_MultipleForums_ShouldReturnDifferentIds

READ Tests (5/5 passed)
  ✓ getForumById_WithValidId_ShouldReturnForum
  ✓ getForumById_WithInvalidId_ShouldReturnNull
  ✓ getAllForums_ShouldReturnAllForums
  ✓ getAllForums_WhenNoForums_ShouldReturnEmptyList
  ✓ getForumsByCreator_ShouldReturnCreatorForums

UPDATE Tests (2/2 passed)
  ✓ updateForum_WithValidData_ShouldReturnTrue
  ✓ updateForum_WithInvalidId_ShouldReturnFalse

DELETE Tests (3/3 passed)
  ✓ deleteForum_WithValidId_ShouldReturnTrue
  ✓ deleteForum_WithWrongCreator_ShouldReturnFalse
  ✓ deleteForum_WithInvalidId_ShouldReturnFalse

Utility Tests (3/3 passed)
  ✓ forumExists_WithValidId_ShouldReturnTrue
  ✓ getMemberCount_ShouldReturnCorrectCount
  ✓ getMemberCount_WithNoMembers_ShouldReturnZero
```

---

## 🎓 Prochaines Étapes

1. ✅ Ouvrir IntelliJ IDEA
2. ✅ Naviguer vers `src/test/java/org/example/dao/ForumDAOTest.java`
3. ✅ Clic droit → Run 'ForumDAOTest'
4. ✅ Voir les résultats dans la fenêtre en bas
5. ✅ Répéter pour `BadgeManagerTest.java`

**Pas besoin de Maven en ligne de commande !** IntelliJ gère tout pour vous. 🚀

---

## 💡 Astuce Pro

Pour exécuter automatiquement les tests à chaque modification :

1. **Run** → Edit Configurations
2. **+** → JUnit
3. **Cocher** "Repeat: Until failure"
4. **Ou** : Utiliser "Toggle auto-test" dans la fenêtre de tests

Bon test ! 🎉
