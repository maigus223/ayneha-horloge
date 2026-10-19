# Horloge AYNEHA - Application Android + Widget (version 1.1.0)

Application Android native qui reprend l'horloge et le calendrier AYNEHA (déjà
publiés en PWA sur `https://maigus223.github.io/AYNEHA-CALENDRIER/`), avec en
plus un **widget d'écran d'accueil** (Horloge).

## Contenu du projet

- **Application** (`MainActivity`) : affiche `assets/index.html` (la même app
  que la PWA, embarquée hors-ligne dans l'APK) via une WebView.
- **Widget Horloge** (`HorlogeWidgetProvider`) : heure en direct (heures :
  minutes), glyphes AYNEHA.
- **Bouton « i »** (coin haut-gauche de l'application) : ouvre la page de
  confidentialité (`PrivacyActivity`).

## ⚠️ Limite technique à connaître : pas de secondes en temps réel sur le widget

Android **n'autorise pas** un widget d'écran d'accueil à s'actualiser chaque
seconde (contrainte système, pour la batterie). Le widget Horloge se
rafraîchit donc **à la minute pile**, via une alarme exacte qui se
replanifie elle-même (`AlarmManager.setExactAndAllowWhileIdle`) - c'est la
même technique utilisée par les widgets d'horloge du système Android.

Seule **l'application** (en l'ouvrant) affiche les secondes qui défilent en
direct, comme sur le site.

## Compiler le projet

1. Ouvrir le dossier `AynehaHorloge/` dans Android Studio (Sync Gradle
   automatique au premier lancement - nécessite une connexion internet pour
   télécharger Gradle et les dépendances).
2. `Build > Generate Signed Bundle / APK` pour produire l'APK signé à
   publier sur Naabiga (ou `Build > Build APK(s)` pour un APK de test non
   signé).
3. `applicationId` actuel : `com.maigus.aynehahorloge` - à adapter si vous
   voulez un identifiant différent.

## Icône

L'icône de lancement (`ic_launcher` / `ic_launcher_round`) reprend l'icône
Icons8 déjà utilisée pour la PWA, déclinée dans toutes les résolutions
standard (mdpi → xxxhdpi). Vous pouvez la remplacer en écrasant les fichiers
dans `app/src/main/res/mipmap-*/`.

## Ajouter les widgets sur l'écran d'accueil (une fois l'app installée)

Appui long sur l'écran d'accueil → Widgets → chercher « Horloge AYNEHA » →
glisser le widget Horloge.

## Dépannage (environnements avec JDK 25 embarqué, ex. Android Studio récent)

Ce projet est déjà configuré pour éviter les erreurs de version rencontrées
lors des premiers tests :
- `gradle-wrapper.properties` → Gradle 8.13 (compatible avec un JDK récent
  utilisé pour exécuter Gradle lui-même).
- `app/build.gradle.kts` → `kotlin { jvmToolchain(17) }` force la
  compilation du code avec JDK 17, indépendamment du JDK utilisé par l'IDE.
- Plugin Kotlin → 2.0.21 (au lieu de 1.9.24, trop ancien pour bien
  fonctionner avec un JDK très récent).
- `configurations.all { resolutionStrategy { force(...) } }` verrouille
  `androidx.core` sur la version 1.13.1, pour éviter qu'une dépendance
  transitive n'en réclame une trop récente pour `compileSdk 34`.

Si malgré tout Android Studio propose encore de changer le JDK ou la
version de Gradle au premier sync, c'est sans danger : acceptez la version
la plus haute qu'il propose.

## Version 1.1.0

- Migration des glyphes AYNEHA vers le bloc plan 16 (U+10FF7C - U+10FFDD),
  avec la police de transition `ayneha_pro.ttf` (ancienne et nouvelle plage).
  Le code est sûr pour les paires de substitution UTF-16 (inversion du texte
  du widget par points de code).
- Ajout du bouton « i » et de la page de confidentialité.
- Kit de licences commun aux applications AYNEHA.
- `versionCode` 2 / `versionName` 1.1.

## Licences

- **Code** : GNU GPL v3 ou ultérieure (fichier `LICENSE`), en-têtes SPDX
  `GPL-3.0-or-later`. Copyright (C) 2026 MAIGUS.
- **Police AYNEHA** : SIL Open Font License 1.1 (fichier `OFL.txt`), nom
  réservé « AYNEHA ».
- **Logos, images et textes** : CC BY-SA 4.0. Le nom et le logo AYNEHA sont
  réservés.
- Développé par MAIGUS avec l'aide d'outils d'IA (Claude, Anthropic).

Logiciel libre (GPL v3). Code source : github.com/maigus223/ayneha-horloge

Contact : gwokmt2q@duck.com

© 2026 MAIGUS - Mahamadou Issiaka MAÏGA
