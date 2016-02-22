package ca.dmdev.petritrebs.wom;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by Doug on 2016-02-22.
 */
public class WordOfMouth extends Application {

    //this is the base class for the entire app
    //all user data will be stored here
    //requests for updated reviews too?

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
