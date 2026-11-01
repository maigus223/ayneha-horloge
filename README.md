# AYNEHA Horloge - Application Android + Widgets (version 2.0)

Application Android qui affiche l'horloge et le double calendrier AYNEHA
(grégorien + lunaire songhay), entièrement en écriture AYNEHA et en vrai RTL,
avec deux **widgets d'écran d'accueil**. La page `assets/index.html` est la
même que la version web du site.

## Contenu du projet

- **Application** (`MainActivity`) : affiche `assets/index.html` (la même app
  que la PWA, embarquée hors-ligne dans l'APK) via une WebView.
- **Petit widget** (`HorlogeWidgetProvider`) : guuru + heure (heures : minutes).
- **Grand widget** (`GrandWidgetProvider`) : heure, jour, hãdo (lune), date
  lunaire songhay et jiiri.
- **Calendrier lunaire** (`SonghayCalendar.kt`, et la même chose dans
  `index.html`) : calendrier lunaire arithmétique (cycle de 30 ans),
  année = année lunaire + 2577, jour lunaire qui change au coucher du soleil
  (calculé pour Gao), réglage +/- 2 jours.
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
   publier sur ayneha.org et Naabiga (ou `Build > Build APK(s)` pour un APK de test non
   signé).
3. `applicationId` actuel : `com.maigus.aynehahorloge` - à adapter si vous
   voulez un identifiant différent.

## Icône

L'icône de lancement (`ic_launcher` / `ic_launcher_round`) reprend l'icône
Icons8 déjà utilisée pour la PWA, déclinée dans toutes les résolutions
standard (mdpi → xxxhdpi). Vous pouvez la remplacer en écrasant les fichiers
dans `app/src/main/res/mipmap-*/`.

## Ajouter les widgets sur l'écran d'accueil (une fois l'app installée)

Appui long sur l'écran d'accueil → Widgets → chercher « AYNEHA Horloge » →
glisser « AYNEHA - Horloge » (petit) ou « AYNEHA - Horloge et lune » (grand).

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

## Version 2.0

- Vrai RTL partout : le texte reste dans l'ordre de lecture, chaque glyphe
  est placé de droite à gauche (page web et widgets). Plus aucune inversion
  de chaîne.
- Double calendrier : grégorien + lunaire songhay dans la même grille, mois
  lunaires qui chevauchent le mois grégorien, début de chaque mois lunaire
  marqué. Semaine qui commence le samedi.
- Nouveautés : phase de la lune (hãdo), détail d'un jour au toucher, bouton
  « aujourd'hui », glissement du doigt entre les mois, mode veille de nuit,
  partage de la date du jour en image, jours fériés (pastille), cadran à
  aiguilles, réglage du début de mois lunaire.
- Widgets fiables : relance après redémarrage, changement d'heure ou de
  fuseau, mise à jour de l'appli ; repli si les alarmes exactes sont
  refusées ; bouton batterie dans l'appli ; image à la taille réelle du
  widget.
- Palette bleu nuit / orange du site. Mises à jour web : `version.txt` +
  page servie « réseau d'abord » par le service worker.
- `versionCode` 3 / `versionName` 2.0.

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
