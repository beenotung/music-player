# music-player

revamp using hybrid app approach.

The previous native app is forced to be updated due to app store policy.
However the app is too outdated, so it cannot simplify bump up the version.

This revamp will use hybrid app approach.
The UI will be implemented using DOM and Typescript, then wrapped as an app using capacitor.

I try to avoid using ionic UI components and any complex framework to keep it simple.

`AndroidManifest.xml`:

```
<manifest
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" tools:ignore="ScopedStorage" />

</manifest>
```

`MainActivity.java`:

```
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // ok
            } else {
                Intent newIntent = new Intent();
                newIntent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                newIntent.setData(uri);
                startActivity(newIntent);
            }
        }
    }
```
