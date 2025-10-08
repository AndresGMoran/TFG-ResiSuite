package com.andresgmoran.apptrabajadores.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.andresgmoran.apptrabajadores.models.ActivityState;
import com.andresgmoran.apptrabajadores.utils.SecurePreferencesUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApiClient {

    private static final String BASE_URL = "http://79.72.62.250:8081";

    // Endpoints admin
    private static final String ENDPOINT_GET_RESIDENCES = "/admin/resi/getAll";
    private static final String ENDPOINT_ADD_RESIDENCE = "/admin/resi/add";
    private static final String ENDPOINT_DELETE_RESIDENCE = "/admin/resi/%d/delete";
    private static final String ENPOINT_GET_USERS_ADMIN = "/admin/resi/user/getAll";
    private static final String ENDPOINT_ADD_USER = "/auth/signup";
    private static final String ENDPOINT_VERIFY_USER = "/auth/verify";
    private static final String ENDPOINT_DELETE_USER = "/admin/resi/%d/user/%d/delete";
    private static final String ENDPOINT_GET_RESIDENTS_ADMIN = "/admin/resi/resident/getAll";
    private static final String ENDPOINT_ADD_RESIDENT_ADMIN = "/admin/resi/%d/resident/add";
    private static final String ENDPOINT_DELETE_RESIDENT = "/admin/resi/%d/resident/%d/delete";
    private static final String ENDPOINT_ADD_GAME = "/admin/resi/juego/add";
    private static final String ENDPOINT_DELETE_GAME = "/admin/resi/juego/%d/delete";
    private static final String ENDPOINT_GET_GAMES_ADMIN = "/admin/resi/juego/getAll";

    // Endpoints no admin
    private static final String ENDPOINT_GET_USERS = "/resi/user/getAll";
    private static final String ENDPOINT_GET_ME = "/resi/user/me";
    private static final String ENDPOINT_RESIDENCE = "/resi/get";
    private static final String ENDPOINT_GET_RESIDENTS = "/resi/resident/getAll";
    private static final String ENDPOINT_GET_RESIDENTS_TAKEN_OUT = "/resi/resident/getAll/bajas";
    private static final String ENDPOINT_ADD_RESIDENT = "/resi/resident/add";
    private static final String ENDPOINT_GET_GAMES = "/resi/juego/getAll";
    private static final String ENDPOINT_GET_STATS = "/resi/registro/getAll";
    private static final String ENDPOINT_GET_ACTIVITIES = "/resi/evento/getAll";
    private static final String ENDPOINT_ADD_EVENT = "/resi/evento/add";
    private static final String ENDPOINT_LOGIN = "/auth/login";
    private static final String ENDPOINT_CHANGE_PASSWORD = "/resi/user/update/changePassword";
    private static final String ENDPOINT_COMMENT_GAMESTAT = "/resi/registro/%d/addComment";
    private static final String ENDPOINT_UPDATE_ACTIVITY_STATE = "/resi/evento/%d/update";
    private static final String ENDPOINT_DELETE_GAMESTAT = "/resi/registro/%d/delete";
    private static final String ENDPOINT_DELETE_ACTIVITY = "/resi/evento/%d/delete";
    private static final String ENDPOINT_ACTIVITY_PARTICIPANTS = "/resi/evento/%d/participante/getAll";
    private static final String ENDPOINT_ADD_PARTICIPANT = "/resi/evento/%d/participante/add";
    private static final String ENDPOINT_DELETE_PARTICIPANT = "/resi/evento/%d/participante/%d/delete";
    private static final String ENDPOINT_UPDATE_PARTICIPANT = "/resi/evento/%d/participante/%d/update";
    private static final String ENDPOINT_ALLOW_PARTICIPANT = "/resi/evento/%d/participante/%d/allow";
    private static final String ENDPOINT_DENY_PARTICIPANT = "/resi/evento/%d/participante/%d/deny";
    private static final String ENDPOINT_TAKE_OUT_RESIDENT = "/resi/resident/%d/baja";

    public interface RawCallback {
        void onSuccess(String jsonText);
        void onError(String error);
    }

    public interface ImageCallback {
        void onSuccess(Bitmap bitmap);
        void onError(String error);
    }
    // ------------------------------------------------------------------ Admin Endpoints -------------------------------------------------

    public static void getResidences(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_RESIDENCES, callback);
    }
    public static void postResidence(Context context, String jsonBody, RawCallback callback) {
        makePostRequest(context, ENDPOINT_ADD_RESIDENCE, jsonBody, callback);
    }
    public static void deleteResidence(Context context, long idResidence, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_RESIDENCE, idResidence), callback);
    }
    public static void getUsersAdmin(Context context, RawCallback callback) {
        makeGetRequest(context, ENPOINT_GET_USERS_ADMIN, callback);
    }
    public static void postUser(Context context, String jsonBody, RawCallback callback) {
        makePostRequest(context, ENDPOINT_ADD_USER, jsonBody, callback);
    }
    public static void patchVerifyUser(Context context, String jsonBody, RawCallback callback) {
        makePostRequest(context, ENDPOINT_VERIFY_USER, jsonBody, callback);
    }
    public static void deleteUser(Context context,long idResi, long idUser, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_USER,idResi, idUser), callback);
    }
    public static void getResidentsAdmin(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_RESIDENTS_ADMIN, callback);
    }
    public static void postResidentAdmin(Context context, long idResi, String jsonBody, RawCallback callback) {
        makePostRequest(context,String.format(ENDPOINT_ADD_RESIDENT_ADMIN, idResi), jsonBody, callback);
    }
    public static void deleteResident(Context context, long idResi, long idResident, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_RESIDENT, idResi, idResident), callback);
    }
    public static void postGame(Context context, String jsonBody, RawCallback callback) {
        makePostRequest(context, ENDPOINT_ADD_GAME, jsonBody, callback);
    }
    public static void deleteGame(Context context, long idGame, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_GAME, idGame), callback);
    }
    public static void getGamesAdmin(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_GAMES_ADMIN, callback);
    }

    // ------------------------------------------------------------------ No Admin Endpoints -------------------------------------------------

    public static void getUsers(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_USERS, callback);
    }

    public static void getActualUser(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_ME, callback);
    }

    public static void getResidence( Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_RESIDENCE, callback);
    }

    public static void getResidents(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_RESIDENTS, callback);
    }

    public static void getResidentsTakenOut(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_RESIDENTS_TAKEN_OUT , callback);
    }

    public static void getGames(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_GAMES, callback);
    }

    public static void getGamesStats(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_STATS, callback);
    }

    public static void getAllActivities(Context context, RawCallback callback) {
        makeGetRequest(context, ENDPOINT_GET_ACTIVITIES, callback);
    }

    public static void getAllParticipants(Context context, Long idActivity, RawCallback callback) {
        makeGetRequest(context, String.format(ENDPOINT_ACTIVITY_PARTICIPANTS, idActivity), callback);
    }

    public static void postLogin(String email, String password, RawCallback callback) {
        String jsonBody = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
        makePostRequest(null, ENDPOINT_LOGIN, jsonBody, callback);
    }
public static void patchChangePassword(Context context, String oldPassword, String newPassword, RawCallback callback) {
        String jsonBody = String.format("{\"oldPassword\":\"%s\", \"newPassword\":\"%s\"}", oldPassword, newPassword);
        makePatchRequest(context, ENDPOINT_CHANGE_PASSWORD, jsonBody, callback);
    }

    public static void postResident(Context context, String nombre, String apellido, LocalDate fechaNacimiento,
                                       String documentoIdentidad, String familiar1, String familiar2, int year, int month,
                                       RawCallback callback) {
        String jsonBody = String.format(
                "{\"nombre\":\"%s\", \"apellido\":\"%s\", \"fechaNacimiento\":\"%s\", \"documentoIdentidad\":\"%s\", " +
                        "\"familiar1\":\"%s\", \"familiar2\":\"%s\", \"year\":%d, \"month\":%d}",
                nombre, apellido, fechaNacimiento.toString(), documentoIdentidad, familiar1, familiar2, year, month);
        makePostRequest(context, ENDPOINT_ADD_RESIDENT, jsonBody, callback);
    }

    public static void postEvento(Context context, String nombre, String descripcion, LocalDateTime fecha, RawCallback callback) {
        String jsonBody = String.format("{\"nombre\":\"%s\", \"descripcion\":\"%s\", \"fecha\":\"%s\", \"estado\":\"%s\"}",
                nombre, descripcion, fecha.toString(), ActivityState.ABIERTO);
        makePostRequest(context, ENDPOINT_ADD_EVENT, jsonBody, callback);
    }

    public static void postParticipant(Context context, Long residentId, boolean humanHelp, boolean materialHelp,
                                       String opinionPre, String opinionPost, Long idActivity, RawCallback callback) {
        String jsonBody = String.format(
                "{\"idResidente\":%d, \"recursosHumanos\":%b, \"recursosMateriales\":%b, \"preOpinion\":\"%s\", \"postOpinion\":\"%s\"}",
                residentId, humanHelp, materialHelp, opinionPre, opinionPost);
        makePostRequest(context, String.format(ENDPOINT_ADD_PARTICIPANT, idActivity), jsonBody, callback);
    }

    public static void patchObservation(Context context, String observation, long idGameStat, RawCallback callback) {
        String jsonBody = String.format("{\"observacion\":\"%s\"}", observation);
        makePatchRequest(context, String.format(ENDPOINT_COMMENT_GAMESTAT, idGameStat), jsonBody, callback);
    }

    public static void patchActivityState(Context context, long idActivity, ActivityState state, RawCallback callback) {
        String jsonBody = String.format("{\"estado\":\"%s\"}", state);
        makePatchRequest(context, String.format(ENDPOINT_UPDATE_ACTIVITY_STATE, idActivity), jsonBody, callback);
    }

    public static void patchParticipant(Context context, long idActivity, long idParticipant,
                                        String jsonBody, RawCallback callback) {
        makePatchRequest(context, String.format(ENDPOINT_UPDATE_PARTICIPANT, idActivity, idParticipant), jsonBody, callback);
    }
    public static void allowParticipant(Context context, long idActivity, long idParticipant, RawCallback callback) {
        makePostRequest(context, String.format(ENDPOINT_ALLOW_PARTICIPANT, idActivity, idParticipant), "", callback);
    }
    public static void denyParticipant(Context context, long idActivity, long idParticipant, RawCallback callback) {
        makePostRequest(context, String.format(ENDPOINT_DENY_PARTICIPANT, idActivity, idParticipant), "" ,callback);
    }

    public static void patchTakeOutResident(Context context, long idResident, RawCallback callback) {
        makePatchRequest(context, String.format(ENDPOINT_TAKE_OUT_RESIDENT, idResident), "", callback);
    }

    public static void deleteGameStat(Context context, long idGameStat, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_GAMESTAT, idGameStat), callback);
    }

    public static void deleteActivity(Context context, long idActivity, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_ACTIVITY, idActivity), callback);
    }

    public static void deleteParticipant(Context context, long idActivity, long idParticipant, RawCallback callback) {
        makeDeleteRequest(context, String.format(ENDPOINT_DELETE_PARTICIPANT, idActivity, idParticipant), callback);
    }

    public static void makeGetRequest(Context context, String endpoint, RawCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Android");

                String token = SecurePreferencesUtil.getString(context, "token", null);
                if (token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }

                final String result = response.toString();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Error (" + responseCode + "): " + result);
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Excepción: " + e.getMessage()));
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    public static void makePostRequest(Context context, String endpoint, String jsonBody, RawCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "Android");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (context != null) {
                    String token = SecurePreferencesUtil.getString(context, "token", null);
                    if (token != null) {
                        connection.setRequestProperty("Authorization", "Bearer " + token);
                    }
                }

                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }

                final String result = response.toString();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Error (" + responseCode + "): " + result);
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Excepción: " + e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    public static void makePatchRequest(Context context, String endpoint, String jsonBody, RawCallback callback) {
        // Puedes usar makePostRequest cambiando el método a PATCH si lo prefieres
        // Aquí dejamos separado por claridad
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "Android");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                String token = SecurePreferencesUtil.getString(context, "token", null);
                if (token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }

                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }

                final String result = response.toString();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Error (" + responseCode + "): " + result);
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Excepción: " + e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    public static void makeDeleteRequest(Context context, String endpoint, RawCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "Android");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                String token = SecurePreferencesUtil.getString(context, "token", null);
                if (token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = connection.getResponseCode();
                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }

                final String result = response.toString();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Error (" + responseCode + "): " + result);
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onError("Excepción: " + e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    public static void downloadImage(Context context, String imageUrl, ImageCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Autorización
                MasterKey masterKey = new MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();

                SharedPreferences encryptedPrefs = EncryptedSharedPreferences.create(
                        context,
                        "secure_auth",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                String token = encryptedPrefs.getString("token", null);
                if (token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(bitmap));

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Error al descargar imagen: " + e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

}
