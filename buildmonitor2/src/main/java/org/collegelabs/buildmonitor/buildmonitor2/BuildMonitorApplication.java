package org.collegelabs.buildmonitor.buildmonitor2;

import android.app.Application;
import com.facebook.stetho.Stetho;
import org.collegelabs.buildmonitor.buildmonitor2.storage.Database;
import timber.log.Timber;

/**
 */
public class BuildMonitorApplication extends Application {

    // TODO DI this instead
    public static Database Db;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        }

        Db = new Database(this.getApplicationContext(), false);
    }
}
