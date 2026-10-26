# Daily Psalms

Daily Psalms is a native, offline Android application designed to help users build a daily scripture reading habit. 

## The Backstory
This project started because of a lecture in my Sources of the Hebrew Bible class. My professor mentioned his habit of reading five Psalms and one Proverb every day, noting that it gave him a daily moment of personal worship and helped him grow wiser. 

I wanted to build that habit for myself, but standard Bible apps and physical books made it difficult. It was a pain to flip back and forth between chapters, and I had nothing to remind me to actually do it. I wanted a linear, distraction-free reading experience. 

I built this app to solve that problem. It calculates the daily chapters for you, tracks your streak, and provides deep study tools right inline. What started as a personal tool eventually grew into a public Play Store release.

## In the App

<p align="center">
  <img src="screenshots/FeatureGraphic.png" alt="Daily Psalms feature graphic" width="100%">
</p>

<table>
  <tr>
    <td align="center"><img src="screenshots/Showcase1.png" alt="Daily reading plan and streak progress" width="250"></td>
    <td align="center"><img src="screenshots/Showcase2.png" alt="Offline Psalm reading view" width="250"></td>
    <td align="center"><img src="screenshots/Showcase4.png" alt="Hebrew lexicon word study" width="250"></td>
  </tr>
  <tr>
    <td align="center"><sub>Daily reading plan</sub></td>
    <td align="center"><sub>Distraction-free offline reading</sub></td>
    <td align="center"><sub>Original-language study tools</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="screenshots/Showcase5.png" alt="Translation selector with modern, historical, and original-language options" width="250"></td>
    <td align="center"><img src="screenshots/Showcase6.png" alt="Customizable reading schedule settings" width="250"></td>
    <td align="center"><img src="screenshots/Showcase7.png" alt="Daily Psalms home screen widget" width="250"></td>
  </tr>
  <tr>
    <td align="center"><sub>Multiple translations</sub></td>
    <td align="center"><sub>Flexible reading schedules</sub></td>
    <td align="center"><sub>Home screen widget</sub></td>
  </tr>
</table>

## AI-Augmented Development
This was my very first app, and I could not have built it on my own. I used a heavily AI-augmented workflow. I directed the system logic and designed the UI, and I leveraged AI to write the syntax and walk me through the implementation step by step.

## Technical Overview
Building this required solving several data-wrangling and memory-management challenges, especially when dealing with public domain texts and ancient languages. 

### Data Processing & Native Development
To keep the app completely offline, I wrote custom Python scripts to process the text.
* Scraped and sanitized public domain texts (KJV, WEB, Septuagint, Latin Vulgate, Westminster Leningrad Codex, etc.).
* Transformed massive academic datasets into a unified JSON schema and standalone local lexicons.
* Built a declarative UI with Jetpack Compose, optimizing reader views with `LazyColumn` to prevent frame drops on chapters with thousands of interactive string links.
* Managed complex user state, alternative reading tracks, and habit streaks using Jetpack DataStore.
* Built an interactive Android Home Screen Widget to automatically sync reading progress.

## Core Features
* **Zero-Network Offline Model:** All 9 translations and full lexicons run entirely locally on the device with zero ads.
* **Study Tools:** Tap-to-define interlinear lexicons mapped to Strong's numbers for Hebrew (WLC) and Greek (LXX), plus a morphological Latin dictionary (Vulgate). It also includes a visual grammar color-coding guide.
* **Customizable Schedules:** Users can follow the standard Classic 1-month track (5 Psalms, 1 Proverb) or choose an alternative pace like the 5-month track. It also features a customizable weekly grace day system to seamlessly roll over missed chapters and protect streaks.

## Installation
The app is currently available on the Google Play Store. You can download it [here](https://play.google.com/store/apps/details?id=com.peter.dailypsalms):

---
*Designed and engineered by Peter Hill*
