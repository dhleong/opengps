Building opengps
================

For the most part, Gradle should do all the hard work for you. However, if you want
the moving map to work you'll need Google API keys.

### Google API Keys

Create the file `app/src/debug/res/values/google_maps_api.xml`. To create a release build,
do the same but with `app/src/release` instead of `/debug`. In that file, put:

```xml
<resources>
    <!--
    TODO: Before you run your application, you need a Google Maps API key.

    Follow the directions here:
    https://developers.google.com/maps/documentation/android/start#get-key

    Once you have your key (it starts with "AIza"), replace the "google_maps_key"
    string in this file.
    -->
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
        YOUR_KEY_HERE
    </string>
</resources>
```

Follow the instructions in the comment.

### Setting up the Gradle build system

There are plenty of tutorials out there. The [Android developer website](https://developer.android.com/develop/index.html)
has a ton of information.

### Building with Gradle

Couldn't be simpler. If you plugin your device via USB, you can just run:

    gradle runDebug

to build and install a debug build onto your device. If you just want the
APK for some reason,

    gradle assembleDebug

will build one in `app/build/outputs/apk`.
