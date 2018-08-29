package ru.skyderby.wings.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

/**
 * Created by EvSmirnov on 25.11.2017.
 */

public class PreferenceSave {

    public static final String APP_PREFERENCES = "face_settings";
    private static  SharedPreferences sharedPref;
    private static Context mContext;
    private static Bitmap faceBitmap;
    private static  int mEvent;

    private static volatile  PreferenceSave instance;

    private  PreferenceSave(){

    }

    public static PreferenceSave getInstance(Context context){
        if (instance==null){
            synchronized (PreferenceSave.class){
                if (instance==null) {
                    instance = new PreferenceSave();
                    sharedPref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                    mContext= context;
                }
            }
        }
        return instance;
    }


    public  void setPinCode(String newPinCode){

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.saved_pin), newPinCode);
        editor.commit();

    }

    public  String  getPinCode(){

        String defaultValue = mContext.getResources().getString(R.string.saved_pin_code_default);
        String savedValue = sharedPref.getString(mContext.getString(R.string.saved_pin), defaultValue);
        return savedValue;
    }

    public  void setUrlPath(String urlPath){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.saved_url), urlPath);
        editor.commit();
    }


    public  String  getUrlPath(){

        String defaultValue = mContext.getResources().getString(R.string.main_url);
        String savedValue = sharedPref.getString(mContext.getString(R.string.saved_url), defaultValue);
        return savedValue;
    }

    public Bitmap getFaceBitmap(){

        return faceBitmap;
    }

    public void  setFaceBitmap(Bitmap bitmap){

        faceBitmap=bitmap;
    }

    public  void setPlace(String place){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.saved_curr_place), place);
        editor.commit();
    }
    public  String  getPlace(){

        String defaultValue = "1";
        String savedValue = sharedPref.getString(mContext.getString(R.string.saved_curr_place), defaultValue);
        return savedValue;
    }

    public void setSubdivisions(String  subdivisions){

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.saved_subdivisions), subdivisions);
        editor.commit();
    }

    public String getSubdivisions(){

        String defaultValue = "";
        String savedValue = sharedPref.getString(mContext.getString(R.string.saved_subdivisions), defaultValue);
        return savedValue;
    }

    public  void setLogin(String login){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.saved_login), login);
        editor.commit();
    }

    public  String  getLogin(){

        String defaultValue = "";
        String savedValue = sharedPref.getString(mContext.getString(R.string.saved_login), defaultValue);
        return savedValue;
    }


    public  void setPassword(String password){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.saved_password),password);
        editor.commit();
    }


    public  String  getPassword(){

        String defaultValue = mContext.getString(R.string.password);
        String savedValue = sharedPref.getString(mContext.getString(R.string.saved_password), defaultValue);
        return savedValue;
    }


    public void setEvent(int event){
        mEvent = event;
    }

    public int getEvent(){

        return mEvent;
    }


    public  void setTimeout(int timeout){

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(mContext.getString(R.string.saved_timeout_result),timeout);
        editor.commit();
    }

    public int getTimeout(){

        int defaultValue = 5;
        int savedValue = sharedPref.getInt(mContext.getString(R.string.saved_timeout_result), defaultValue);
        return savedValue;
    }

}