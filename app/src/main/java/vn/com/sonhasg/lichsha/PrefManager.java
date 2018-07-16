package vn.com.sonhasg.lichsha;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nguyenphuoc on 04/19/2017.
 */

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "vn.com.sonhasg.lichsha";

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_LOGGED_IN = "idLoggedIn";
    private static final String KEY_CITY = "city";
    private static final String KEY_PHONGBAN1 = "phongban1";
    private static final String KEY_PHONGBAN2 = "phongban2";
    private static final String KEY_DOMAIN = "domain";

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, 0);
        editor = pref.edit();
    }

    public void setUser(String id) {
        editor.putString(KEY_USER_LOGGED_IN, id);
        editor.commit();
    }

    public String getUser() {
        return pref.getString(KEY_USER_LOGGED_IN, "0");
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getDomain() {
        return pref.getString(KEY_DOMAIN, "");
    }

    public void setDomain(String domain) {
        editor.putString(KEY_DOMAIN, domain);
        editor.commit();
    }

    public String getPhongban1() {
        return pref.getString(KEY_PHONGBAN1, "0");
    }

    public void setPhongban1(String phongban1) {
        editor.putString(KEY_PHONGBAN1, phongban1);
        editor.commit();
    }

    public String getPhongban2() {
        return pref.getString(KEY_PHONGBAN2, "0");
    }

    public void setPhongban2(String phongban2) {
        editor.putString(KEY_PHONGBAN2, phongban2);
        editor.commit();
    }

    public String getCity() {
        return pref.getString(KEY_CITY, "0");
    }

    public void setCity(String region) {
        editor.putString(KEY_CITY, region);
        editor.commit();
    }
}
