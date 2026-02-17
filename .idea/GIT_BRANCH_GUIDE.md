# Guide Git - Télécharger et Gérer les Branches

## 🎯 Télécharger une Branche Spécifique

### Méthode 1 : Clone + Checkout (Recommandé)

```bash
# 1. Cloner le repository
git clone https://github.com/username/repository.git

# 2. Entrer dans le dossier
cd repository

# 3. Voir toutes les branches disponibles
git branch -a

# 4. Télécharger et basculer vers la branche
git checkout nom-de-la-branche
```

### Méthode 2 : Clone d'une Branche Spécifique Directement

```bash
# Cloner uniquement une branche spécifique
git clone -b nom-de-la-branche https://github.com/username/repository.git
```

### Méthode 3 : Si le Repo est Déjà Cloné

```bash
# 1. Mettre à jour les références
git fetch origin

# 2. Voir toutes les branches
git branch -a

# 3. Basculer vers la branche
git checkout nom-de-la-branche

# Ou créer une branche locale qui suit la branche distante
git checkout -b nom-local origin/nom-distant
```

---

## 📋 Commandes Git Essentielles

### Voir les Branches

```bash
# Voir les branches locales
git branch

# Voir toutes les branches (locales + distantes)
git branch -a

# Voir les branches distantes uniquement
git branch -r

# Voir la branche actuelle
git branch --show-current
```

### Créer une Branche

```bash
# Créer une nouvelle branche
git branch nouvelle-branche

# Créer et basculer vers la nouvelle branche
git checkout -b nouvelle-branche

# Ou avec la nouvelle syntaxe
git switch -c nouvelle-branche
```

### Basculer entre les Branches

```bash
# Méthode classique
git checkout nom-branche

# Nouvelle méthode (Git 2.23+)
git switch nom-branche
```

### Mettre à Jour une Branche

```bash
# Récupérer les dernières modifications
git pull origin nom-branche

# Ou en deux étapes
git fetch origin
git merge origin/nom-branche
```

### Supprimer une Branche

```bash
# Supprimer une branche locale
git branch -d nom-branche

# Forcer la suppression
git branch -D nom-branche

# Supprimer une branche distante
git push origin --delete nom-branche
```

---

## 🔄 Workflow Complet

### Scénario 1 : Télécharger et Travailler sur une Branche

```bash
# 1. Cloner le repository
git clone https://github.com/username/fintechforum.git
cd fintechforum

# 2. Voir les branches disponibles
git branch -a

# 3. Télécharger la branche "feature/badges"
git checkout feature/badges

# 4. Vérifier que vous êtes sur la bonne branche
git branch --show-current

# 5. Travailler sur vos fichiers...
# (modifier, ajouter, supprimer des fichiers)

# 6. Voir les modifications
git status

# 7. Ajouter les modifications
git add .

# 8. Commiter
git commit -m "Description des modifications"

# 9. Pousser vers GitHub
git push origin feature/badges
```

### Scénario 2 : Créer une Nouvelle Branche

```bash
# 1. S'assurer d'être sur main/master
git checkout main

# 2. Mettre à jour
git pull origin main

# 3. Créer une nouvelle branche
git checkout -b feature/nouvelle-fonctionnalite

# 4. Travailler...

# 5. Pousser la nouvelle branche
git push -u origin feature/nouvelle-fonctionnalite
```

### Scénario 3 : Fusionner une Branche

```bash
# 1. Aller sur la branche de destination (ex: main)
git checkout main

# 2. Mettre à jour
git pull origin main

# 3. Fusionner la branche feature
git merge feature/badges

# 4. Résoudre les conflits si nécessaire

# 5. Pousser
git push origin main
```

---

## 🎨 Utiliser Git dans IntelliJ IDEA

### Cloner un Repository

1. **VCS** → Get from Version Control
2. **Entrer l'URL** : `https://github.com/username/repository.git`
3. **Choisir le dossier** de destination
4. **Clone**

### Changer de Branche

1. **En bas à droite** : Cliquer sur le nom de la branche actuelle
2. **Sélectionner** la branche dans la liste
3. **Checkout**

Ou :

1. **Git** → Branches
2. **Sélectionner** la branche
3. **Checkout**

### Créer une Nouvelle Branche

1. **Git** → New Branch
2. **Entrer le nom** : `feature/ma-nouvelle-branche`
3. **Create**

### Commit et Push

1. **Ctrl+K** (Commit)
2. **Sélectionner** les fichiers à commiter
3. **Écrire** le message de commit
4. **Commit and Push** (ou juste Commit)

### Voir l'Historique

1. **Git** → Show Git Log
2. **Ou** : Alt+9 (Git tool window)

### Pull (Mettre à Jour)

1. **Git** → Pull
2. **Ou** : Ctrl+T

---

## 🌳 Structure des Branches Recommandée

```
main (ou master)
├── develop
│   ├── feature/badges
│   ├── feature/animations
│   ├── feature/tests
│   └── feature/ai-recommendations
├── hotfix/bug-critique
└── release/v1.0
```

### Conventions de Nommage

- `main` ou `master` : Branche principale (production)
- `develop` : Branche de développement
- `feature/nom` : Nouvelles fonctionnalités
- `bugfix/nom` : Corrections de bugs
- `hotfix/nom` : Corrections urgentes
- `release/version` : Préparation de release

---

## 🔍 Commandes Utiles

### Voir les Différences

```bash
# Différences non commitées
git diff

# Différences entre branches
git diff main feature/badges

# Différences d'un fichier spécifique
git diff main feature/badges -- src/main/java/ForumDAO.java
```

### Annuler des Modifications

```bash
# Annuler les modifications d'un fichier (non commité)
git checkout -- fichier.java

# Annuler le dernier commit (garder les modifications)
git reset --soft HEAD~1

# Annuler le dernier commit (supprimer les modifications)
git reset --hard HEAD~1

# Annuler un commit spécifique
git revert <commit-hash>
```

### Stash (Mettre de Côté)

```bash
# Mettre de côté les modifications
git stash

# Voir les stash
git stash list

# Récupérer le dernier stash
git stash pop

# Récupérer un stash spécifique
git stash apply stash@{0}

# Supprimer un stash
git stash drop stash@{0}
```

### Historique

```bash
# Voir l'historique
git log

# Historique compact
git log --oneline

# Historique graphique
git log --graph --oneline --all

# Historique d'un fichier
git log -- fichier.java
```

---

## 🚨 Résolution de Conflits

### Quand un Conflit Survient

```bash
# 1. Git vous informe du conflit
Auto-merging fichier.java
CONFLICT (content): Merge conflict in fichier.java

# 2. Voir les fichiers en conflit
git status

# 3. Ouvrir le fichier et chercher
<<<<<<< HEAD
Votre code
=======
Code de la branche à fusionner
>>>>>>> feature/badges

# 4. Résoudre manuellement (garder ce que vous voulez)

# 5. Marquer comme résolu
git add fichier.java

# 6. Finaliser le merge
git commit -m "Résolution des conflits"
```

### Dans IntelliJ IDEA

1. **VCS** → Git → Resolve Conflicts
2. **Sélectionner** le fichier
3. **Merge** (outil visuel)
4. **Accept Yours** / **Accept Theirs** / **Merge Manually**
5. **Apply**

---

## 📦 Exemples Pratiques

### Exemple 1 : Télécharger la Branche "feature/badges"

```bash
# Si le repo n'est pas encore cloné
git clone https://github.com/username/fintechforum.git
cd fintechforum
git checkout feature/badges

# Si le repo est déjà cloné
cd fintechforum
git fetch origin
git checkout feature/badges
git pull origin feature/badges
```

### Exemple 2 : Créer une Branche pour les Tests

```bash
cd fintechforum
git checkout main
git pull origin main
git checkout -b feature/unit-tests
# Travailler sur les tests...
git add .
git commit -m "Ajout des tests unitaires pour ForumDAO"
git push -u origin feature/unit-tests
```

### Exemple 3 : Fusionner les Badges dans Main

```bash
cd fintechforum
git checkout main
git pull origin main
git merge feature/badges
# Résoudre les conflits si nécessaire
git push origin main
```

---

## 🎓 Commandes PowerShell pour Windows

### Vérifier si Git est Installé

```powershell
git --version
```

Si Git n'est pas installé :
1. Télécharger : https://git-scm.com/download/win
2. Installer avec les options par défaut
3. Redémarrer PowerShell

### Naviguer dans les Dossiers

```powershell
# Voir le dossier actuel
pwd

# Lister les fichiers
ls

# Changer de dossier
cd C:\Users\MSI\IdeaProjects\fintechforum

# Remonter d'un niveau
cd ..

# Aller au dossier utilisateur
cd ~
```

---

## 🔐 Configuration Git (Première Fois)

```bash
# Configurer votre nom
git config --global user.name "Votre Nom"

# Configurer votre email
git config --global user.email "votre.email@example.com"

# Voir la configuration
git config --list

# Configurer l'éditeur par défaut
git config --global core.editor "code"  # VS Code
git config --global core.editor "notepad"  # Notepad
```

---

## 📚 Ressources

- **Documentation Git** : https://git-scm.com/doc
- **GitHub Guides** : https://guides.github.com/
- **Git Cheat Sheet** : https://education.github.com/git-cheat-sheet-education.pdf
- **Visualiser Git** : https://git-school.github.io/visualizing-git/

---

## ✅ Checklist Rapide

Pour télécharger une branche :

- [ ] Git est installé (`git --version`)
- [ ] Repository cloné (`git clone URL`)
- [ ] Voir les branches (`git branch -a`)
- [ ] Télécharger la branche (`git checkout nom-branche`)
- [ ] Vérifier (`git branch --show-current`)

Bon travail avec Git ! 🚀
