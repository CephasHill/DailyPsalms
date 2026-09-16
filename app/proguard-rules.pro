-keep class com.peter.dailypsalms.** { *; }
# Keep AndroidX WorkManager and Room Database classes intact
-keep class androidx.work.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.** { *; }
-keep class androidx.startup.** { *; }