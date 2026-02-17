# Comment lancer l'application JavaFX

## Solution 1 : Utiliser le plugin Maven JavaFX (Recommandé)

Dans IntelliJ IDEA :
1. Ouvrez le panneau Maven (View → Tool Windows → Maven)
2. Développez `Plugins` → `javafx` → `javafx:run`
3. Double-cliquez sur `javafx:run`

## Solution 2 : Configuration de lancement IntelliJ

1. Allez dans `Run` → `Edit Configurations...`
2. Cliquez sur `+` → `Application`
3. Configurez :
   - **Name**: App
   - **Main class**: `org.esprit.finovate.view.App`
   - **VM options**: Copiez-collez ce qui suit (remplacez `C:\Users\21620` par votre chemin utilisateur si différent) :

```
--module-path "C:\Users\21620\.m2\repository\org\openjfx\javafx-controls\17.0.2\javafx-controls-17.0.2.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-controls\17.0.2\javafx-controls-17.0.2-win.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-fxml\17.0.2\javafx-fxml-17.0.2.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-fxml\17.0.2\javafx-fxml-17.0.2-win.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-graphics\17.0.2\javafx-graphics-17.0.2.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-graphics\17.0.2\javafx-graphics-17.0.2-win.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-base\17.0.2\javafx-base-17.0.2.jar;C:\Users\21620\.m2\repository\org\openjfx\javafx-base\17.0.2\javafx-base-17.0.2-win.jar" --add-modules javafx.controls,javafx.fxml
```

4. Cliquez sur `OK` et lancez l'application

## Solution 3 : Ligne de commande Maven

```bash
mvn javafx:run
```
