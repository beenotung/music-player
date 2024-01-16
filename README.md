# music-player

revamp using hybrid app approach.

The previous native app is forced to be updated due to app store policy.
However the app is too outdated, so it cannot simplify bump up the version.

This revamp will use hybrid app approach.
The UI will be implemented using DOM and Typescript, then wrapped as an app using capacitor.

I try to avoid using ionic UI components and any complex framework to keep it simple.

Android Setup:
```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
