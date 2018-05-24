package com.mayunfei.downloadmanager.download;

import android.util.Log;

class L {
     private static boolean isDebug = true;
     public static void e(String tag,String msg){
         if (isDebug) {
             Log.e(tag,msg);
         }
     }
    public static void i(String tag,String msg){
        if (isDebug) {
            Log.i(tag,msg);
        }
    }
}
