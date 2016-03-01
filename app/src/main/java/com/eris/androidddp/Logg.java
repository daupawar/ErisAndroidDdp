package com.eris.androidddp;

import android.util.Log;

/**
 * Created by Rohan on 16/02/16.
 */
public class Logg {

    public static boolean printLog=true;
    public static void i(Object obj,String message){
        if (printLog) {
            try {
                Log.i(getClassName(obj), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void d(Object obj,String message){
        if (printLog) {
            try {
                Log.d(getClassName(obj), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void v(Object obj,String message){
        if (printLog) {
            try {
                Log.v(getClassName(obj), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getClassName(Object obj){
        if(obj instanceof String){
            return (String)obj;
        }
        else{
            return obj.getClass().getSimpleName();
        }
    }
}
