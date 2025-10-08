package com.andresgmoran.apptrabajadores.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SecurePreferencesUtil {

    private static final String PREF_NAME = "secure_auth";
    private static SharedPreferences encryptedPrefs = null;

    public static SharedPreferences getEncryptedPrefs(Context context) {
        if (encryptedPrefs == null) {
            try {
                MasterKey masterKey = new MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();

                encryptedPrefs = EncryptedSharedPreferences.create(
                        context,
                        PREF_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar SharedPreferences encriptadas", e);
            }
        }
        return encryptedPrefs;
    }

    public static String getString(Context context, String key, String defaultValue) {
        return getEncryptedPrefs(context).getString(key, defaultValue);
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getEncryptedPrefs(context).getBoolean(key, defaultValue);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getEncryptedPrefs(context).getLong(key, defaultValue);
    }

    public static void edit(Context context, SharedPreferences.Editor editor) {
        editor.apply();
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getEncryptedPrefs(context).edit();
        editor.clear();
        edit(context, editor);
    }
}
