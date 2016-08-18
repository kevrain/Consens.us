package com.kevrain.consensus;

import android.app.Application;

import com.parse.Parse;
import com.parse.interceptors.ParseLogInterceptor;

/**
 * Created by kfarst on 8/17/16.
 */
public class ConsensusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.PARSE_APPLICATION_ID) // should correspond to APP_ID env variable
                .clientKey(BuildConfig.PARSE_CLIENT_KEY)  // set explicitly unless clientKey is explicitly configured on Parse server
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server(BuildConfig.PARSE_SERVER_URL).build());
    }
}
