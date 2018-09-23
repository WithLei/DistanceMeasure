package com.android.renly.distancemeasure;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application{

    public static boolean isFirstIn(Context context){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,MODE_PRIVATE);
        return sp.getBoolean(First_In,true);
    }

    public static void setFirst_In(Context context,boolean flag){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(First_In,flag);
        editor.apply();
    }

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final String SP_NAME = "distanceMeasure";
    public static final String First_In = "first_in";
}
