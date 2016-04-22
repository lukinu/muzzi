package org.noname.muzzi.model;

import android.app.Application;
import android.util.Log;

import org.noname.muzzi.utils.BitmapLoader;

/*
*   A class to store some static variables,
*   namely, image loader object BitmapLoader
*
* */
public class App extends Application {

    private static final String LOG_TAG = "APP";
    public static BitmapLoader mBitmapLoader;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Application onCreate");
    }
}
