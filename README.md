# Skylink Android SDK - `connectToRoom` API

### How to wait for Skylink room connection in a background thread using `Kotlin coroutines`

``` kotlin
override fun onResume() {

    mTV.text = "Connecting to room..." // UI update

    // Connect to Skylink room
    mSkylinkConnection.connectToRoom("Skylink Key ID comes here",
        "Skylink Key secret comes here",
        "Room name comes here",
        "Username of connecting peer comes here",
        object : SkylinkCallback {
            override fun onError(skylinkError: SkylinkError, hashMap: HashMap<String, Any>) {
                // Handle Skylink room connection error
            }
        }
    )

    // Create a coroutine to move the execution off the main thread to an IO thread
    CoroutineScope(Dispatchers.IO).launch {
        // Wait until connected to Skylink room
        while (mSkylinkConnection.skylinkState != SkylinkConnection.SkylinkState.CONNECTED) {
            delay(500)
        }
        val skylinkState = mSkylinkConnection.skylinkState.toString()

        // New coroutine to update UI from the main thread
        CoroutineScope(Dispatchers.Main).launch {
            mTV.text = skylinkState // UI update to displays "CONNECTED"
        }
    }

}
```

## Sample app

This repository contains an Android Studio (2020.3.1 Patch 4) project of a simple app that demonstrate how to accurately wait for Skylink room connection in a background thread using Kotlin coroutines.

Before building and running app, please set your Skylink Key ID and secret in `app/src/main/res/values/skylink_config.xml`.

``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="skylink_key_id">Your Skylink Key ID should come here</string>
    <string name="skylink_key_secret">Your Skylink Key secret should come here</string>
    ...
</resources>
```

Screenshots of running app.

![Sample app screenshot 1](figures/Screenshot_1644302094.png)
![Sample app screenshot 1](figures/Screenshot_1644302098.png)