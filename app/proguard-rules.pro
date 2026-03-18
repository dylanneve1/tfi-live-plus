# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.tfigo.app.data.model.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
