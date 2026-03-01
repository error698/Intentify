# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities
-keep class com.distractblock.data.** { *; }

# Keep accessibility service
-keep class com.distractblock.service.BlockerAccessibilityService { *; }
