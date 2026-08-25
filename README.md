# Daily Psalms & Proverbs

Daily Psalms & Proverbs is a native, offline-first Android application designed to help users build a daily scripture reading habit[cite: 1]. 

## The Backstory
This project started because of a lecture in my Sources of the Hebrew Bible class. My professor mentioned his habit of reading five Psalms and one Proverb every day, noting that it gave him a daily moment of personal worship and helped him grow wiser. 

I wanted to build that habit for myself, but standard Bible apps and physical books made it difficult. It was a pain to flip back and forth between chapters, and I had nothing to remind me to actually do it. I wanted a linear, distraction-free reading experience. 

I built this app to solve that problem. It calculates the daily chapters for you, tracks your streak, and provides deep study tools right inline[cite: 1]. What started as a personal tool eventually grew into a public Play Store release.

## AI-Augmented Development
This was my very first app, and I could not have built it on my own. I used a heavily AI-augmented workflow, directing the system logic and designing the UI while leveraging AI to write the syntax and walk me through the implementation step by step. It was an incredible crash course in product engineering.

## Technical Overview
Building this required solving several data-wrangling and memory-management challenges, especially when dealing with public domain texts and ancient languages. 

### Data Processing & Native Development
To keep the app completely offline, I wrote custom Python scripts to process the text[cite: 1, 5].
* Scraped and sanitized public domain texts (KJV, WEB, Septuagint, Latin Vulgate, and the Westminster Leningrad Codex)[cite: 6, 9].
* Transformed massive academic datasets into a unified JSON schema and standalone local lexicons[cite: 2, 5].
* Built a declarative UI with Jetpack Compose, optimizing reader views with `LazyColumn` to prevent frame drops on chapters with thousands of interactive string links[cite: 7, 9].
* Managed complex user state, alternative reading tracks, and habit streaks using Jetpack DataStore[cite: 1, 9].
* Built an interactive Android Home Screen Widget to automatically sync reading progress[cite: 1].

## Core Features
* **Zero-Network Offline Model:** All translations and full lexicons run entirely locally on the device with zero ads[cite: 1].
* **Study Tools:** Tap-to-define interlinear lexicons mapped to Strong's numbers for Hebrew (WLC) and Greek (LXX), plus a morphological Latin dictionary (Vulgate)[cite: 3].
* **Customizable Schedules:** Users can follow the standard "Classic" 1-month track (5 Psalms, 1 Proverb) or choose a custom pace, complete with a weekly grace day system to seamlessly roll over missed chapters and protect their streaks[cite: 6, 9].

## Installation
The app is currently available on the Google Play Store:
> *[Insert your Google Play Store Link Here]*

---
*Designed and engineered by [Your Name]*
