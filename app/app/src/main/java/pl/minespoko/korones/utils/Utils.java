package pl.minespoko.korones.utils;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Utils {

    /*
     * Funkcja służąca do informowania o róznych błędach
     * */
    public static void log(String key, String value){
        if(key.equals("error")){
            System.out.println("[Korones-ERROR] "+key+" :=> "+value);
        }
    }

}
