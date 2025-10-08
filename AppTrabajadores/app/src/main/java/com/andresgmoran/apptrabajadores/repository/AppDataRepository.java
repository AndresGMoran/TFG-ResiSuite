package com.andresgmoran.apptrabajadores.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Residence;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.User;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;
import com.andresgmoran.apptrabajadores.models.parsers.ActivityParser;
import com.andresgmoran.apptrabajadores.models.parsers.ActivityResidentParser;
import com.andresgmoran.apptrabajadores.models.parsers.GameParser;
import com.andresgmoran.apptrabajadores.models.parsers.GameStatParser;
import com.andresgmoran.apptrabajadores.models.parsers.ResidenceParser;
import com.andresgmoran.apptrabajadores.models.parsers.ResidentParser;
import com.andresgmoran.apptrabajadores.models.parsers.UserParser;
import com.andresgmoran.apptrabajadores.network.ApiClient;
import com.andresgmoran.apptrabajadores.utils.SecurePreferencesUtil;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppDataRepository {

    private static final AppDataRepository instance = new AppDataRepository();

    private User actualUser;
    private Bitmap actualUserImage;
    private List<User> users = new ArrayList<>();
    private Residence residence = null;
    private List<Residence> residences = new ArrayList<>();
    private Bitmap residentImage;
    private List<Resident> residents = new ArrayList<>();
    private List<Resident> residentsTakenOut = new ArrayList<>();
    private List<Game> games = new ArrayList<>();
    private List<GameStat> gameStats = new ArrayList<>();
    private List<Activity> activities = new ArrayList<>();
    private List<ActivityResident> activityResidents = new ArrayList<>();

    private AppDataRepository() {}

    public static AppDataRepository getInstance() {
        return instance;
    }

    // -------------------- Getters/Setters Básicos --------------------
    public User getActualUser() { return actualUser; }
    public void setActualUser(User user) { this.actualUser = user; }

    public Bitmap getActualUserImage() { return actualUserImage; }
    public void setActualUserImage(Bitmap image) { this.actualUserImage = image; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> list) { this.users = new ArrayList<>(list); }

    public Residence getResidence() { return residence; }
    public void setResidence(Residence residence) { this.residence = residence; }

    public List<Residence> getResidences() { return residences; }
    public void setResidences(List<Residence> list) { this.residences = new ArrayList<>(list); }

    public List<Resident> getResidents() { return residents; }
    public void setResidents(List<Resident> list) { this.residents = new ArrayList<>(list); }

    public List<Resident> getResidentsTakenOut() { return residentsTakenOut; }
    public void setResidentsTakenOut(List<Resident> list) { this.residentsTakenOut = new ArrayList<>(list); }

    public List<Game> getGames() { return games; }
    public void setGames(List<Game> list) { this.games = new ArrayList<>(list); }

    public List<GameStat> getGameStats() { return gameStats; }
    public void setGameStats(List<GameStat> list) { this.gameStats = new ArrayList<>(list); }

    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> list) { this.activities = new ArrayList<>(list); }

    public List<ActivityResident> getActivityResidents() { return activityResidents; }
    public void setActivityResidents(List<ActivityResident> list) { this.activityResidents = new ArrayList<>(list); }

    // -------------------- API Calls con ADMIN --------------------
    public void fetchAllResidencesAdmin(Context context, Runnable onSuccess, Runnable onError) {
        ApiClient.getResidences(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                try {
                    residences = ResidenceParser.parseResidences(jsonText);
                    fetchAllUsersAdmin(context, onSuccess, onError);
                } catch (Exception e) {
                    Log.e("ResidenceParser", "Error al parsear residencias: " + e.getMessage());
                    onError.run();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residencias: " + error);
                onError.run();
            }
        });
    }

    public void fetchAllUsersAdmin(Context context, Runnable onSuccess, Runnable onError) {
        ApiClient.getUsersAdmin(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                try {
                    users = UserParser.parseUsers(jsonText);
                    fetchAllResidentsAdmin(context, onSuccess, onError);
                } catch (Exception e) {
                    Log.e("UserParser", "Error al parsear usuarios: " + e.getMessage());
                    onError.run();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener usuarios: " + error);
                onError.run();
            }
        });
    }

    public void fetchAllResidentsAdmin(Context context, Runnable onSuccess, Runnable onError) {
        ApiClient.getResidentsAdmin(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                try {
                    residents = ResidentParser.parseResidents(jsonText);
                    fetchAllGamesAdmin(context, onSuccess, onError);
                } catch (Exception e) {
                    Log.e("ResidentParser", "Error al parsear residentes: " + e.getMessage());
                    onError.run();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residentes: " + error);
                onError.run();
            }
        });
    }

    public void fetchAllGamesAdmin(Context context, Runnable onSuccess, Runnable onError) {
        ApiClient.getGamesAdmin(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                try {
                    games = GameParser.parseGames(jsonText);
                    onSuccess.run();
                } catch (Exception e) {
                    Log.e("GameParser", "Error al parsear juegos: " + e.getMessage());
                    onError.run();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener juegos: " + error);
                onError.run();
            }
        });
    }

    // -------------------- API Calls no ADMIN--------------------

    public void fetchActualUser(Context context, Runnable onSuccess, Runnable onError) {
        ApiClient.getActualUser(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                try {
                    actualUser = UserParser.parseUser(jsonText);
                    fetchUserImage(context, actualUser.getAccountImage(), onSuccess);
                } catch (Exception e) {
                    Log.e("UserParser", "Error al parsear usuario: " + e.getMessage());
                    onError.run();
                }
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener usuario: " + error);
                onError.run();
            }
        });
    }

    private void fetchUserImage(Context context, String imageUrl, Runnable onFinish) {
        ApiClient.downloadImage(context, imageUrl, new ApiClient.ImageCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                actualUserImage = bitmap;
                fetchUsers(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener imagen de usuario: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchUsers(Context context, Runnable onFinish) {
        ApiClient.getUsers(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                users = UserParser.parseUsers(jsonText);
                fetchResidence(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener usuarios: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchResidence( Context context, Runnable onFinish) {
        ApiClient.getResidence(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                residence = ResidenceParser.parseResidence(jsonText);
                fetchResidents(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residencia: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchResidents(Context context, Runnable onFinish) {
        ApiClient.getResidents(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                residents = ResidentParser.parseResidents(jsonText);
                fetchResidentsTakenOut(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residentes: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchResidentsTakenOut(Context context, Runnable onFinish) {
        ApiClient.getResidentsTakenOut(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                residentsTakenOut = ResidentParser.parseResidents(jsonText);
                fetchGames(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residentes dados de baja: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchGames(Context context, Runnable onFinish) {
        ApiClient.getGames(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                games = GameParser.parseGames(jsonText);
                fetchGameStats(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener juegos: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchGameStats(Context context, Runnable onFinish) {
        ApiClient.getGamesStats(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                gameStats = GameStatParser.parseStats(jsonText);
                fetchActivities(context, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener partidas: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchActivities(Context context, Runnable onFinish) {
        ApiClient.getAllActivities(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                activities = ActivityParser.parseActivities(jsonText);
                if (activities.isEmpty()) {
                    onFinish.run();
                } else {
                    fetchAllParticipants(context, onFinish);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener actividades: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchAllParticipants(Context context, Runnable onFinish) {
        activityResidents.clear();
        final int total = activities.size();
        final int[] completed = {0};

        for (Activity activity : activities) {
            ApiClient.getAllParticipants(context, activity.getId(), new ApiClient.RawCallback() {
                @Override
                public void onSuccess(String jsonText) {
                    List<ActivityResident> parsed = ActivityResidentParser.parseActivityResidents(jsonText);
                    synchronized (activityResidents) {
                        activityResidents.addAll(parsed);
                    }
                    checkDone();
                }

                @Override
                public void onError(String error) {
                    Log.e("API", "Error al obtener participantes: " + error);
                    checkDone();
                }

                private void checkDone() {
                    synchronized (completed) {
                        completed[0]++;
                        if (completed[0] == total) {
                            onFinish.run();
                        }
                    }
                }
            });
        }
    }

    public void fetchActualUserOnly(Context context, Runnable onFinish) {
        ApiClient.getActualUser(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                actualUser = UserParser.parseUser(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener usuario: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchUserImageOnly(Context context, String imageUrl, Runnable onFinish) {
        ApiClient.downloadImage(context, imageUrl, new ApiClient.ImageCallback() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                actualUserImage = bitmap;
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener imagen de usuario: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchUsersOnly(Context context, Runnable onFinish) {
        ApiClient.getUsers(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                users = UserParser.parseUsers(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener usuarios: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchResidenceOnly( Context context, Runnable onFinish) {
        ApiClient.getResidence(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                residence = ResidenceParser.parseResidence(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residencia: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchResidentsOnly(Context context, Runnable onFinish) {
        ApiClient.getResidents(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                residents = ResidentParser.parseResidents(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residentes: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchResidentsTakenOutOnly(Context context, Runnable onFinish) {
        ApiClient.getResidentsTakenOut(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                residentsTakenOut = ResidentParser.parseResidents(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener residentes dados de baja: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchGamesOnly(Context context, Runnable onFinish) {
        ApiClient.getGames(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                games = GameParser.parseGames(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener juegos: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchGameStatsOnly(Context context, Runnable onFinish) {
        ApiClient.getGamesStats(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                gameStats = GameStatParser.parseStats(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener partidas: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchActivitiesOnly(Context context, Runnable onFinish) {
        ApiClient.getAllActivities(context, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                activities = ActivityParser.parseActivities(jsonText);
                onFinish.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al obtener actividades: " + error);
                onFinish.run();
            }
        });
    }

    public void fetchParticipantsOnly(Context context, Runnable onSuccess) {
        activityResidents.clear();
        final int total = activities.size();
        final int[] completed = {0};

        if (total == 0) {
            onSuccess.run();
            return;
        }

        for (Activity activity : activities) {
            ApiClient.getAllParticipants(context, activity.getId(), new ApiClient.RawCallback() {
                @Override
                public void onSuccess(String jsonText) {
                    List<ActivityResident> parsed = ActivityResidentParser.parseActivityResidents(jsonText);
                    synchronized (activityResidents) {
                        activityResidents.addAll(parsed);
                    }
                    checkDone();
                }

                @Override
                public void onError(String error) {
                    Log.e("API", "Error al obtener participantes: " + error);
                    checkDone();
                }

                private void checkDone() {
                    synchronized (completed) {
                        completed[0]++;
                        if (completed[0] == total) {
                            onSuccess.run();
                        }
                    }
                }
            });
        }
    }

    // -------------------- Acciones API ADMIN --------------------
    public void deleteResidence(Context context, long id, Runnable onSuccess, Runnable onError) {
        ApiClient.deleteResidence(context, id, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllResidencesAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al eliminar residencia: " + error);
                onError.run();
            }
        });
    }
    public void deleteResident(Context context,long idResi, long idResident, Runnable onSuccess, Runnable onError) {
        ApiClient.deleteResident(context, idResi, idResident, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllResidentsAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al eliminar residente: " + error);
                onError.run();
            }
        });
    }
    public void deleteGame(Context context, long id, Runnable onSuccess, Runnable onError) {
        ApiClient.deleteGame(context, id, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllGamesAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al eliminar juego: " + error);
                onError.run();
            }
        });
    }

    public void deleteUser(Context context, long idResi, long idUser, Runnable onSuccess, Runnable onError) {
        ApiClient.deleteUser(context, idResi, idUser, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllUsersAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al eliminar usuario: " + error);
                onError.run();
            }
        });
    }

    public void addUser(Context context, long idResi, String name, String surnames, String email, String password, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"nombre\": \"" + name + "\", \"apellido\": \"" + surnames + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"idResidencia\": " + idResi + "}";
        ApiClient.postUser(context,jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllUsersAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al añadir usuario: " + error);
                onError.run();
            }
        });
    }
    public void verifyUser(Context context, String email, String code, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"email\": \"" + email + "\", \"verificationCode\": \"" + code + "\"}";
        ApiClient.patchVerifyUser(context, jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllUsersAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al verificar usuario: " + error);
                onError.run();
            }
        });
    }

    public void addResidence(Context context, String name, String email, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"nombre\": \"" + name + "\", \"email\": \"" + email + "\"}";
        ApiClient.postResidence(context,jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllResidencesAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al añadir residencia: " + error);
                onError.run();
            }
        });
    }

    public void addResidenteAdmin(Context context, long idResi, String nombre, String apellido, LocalDate fechaNacimiento, String documentoIdentidad, String familiar1, String familiar2, int year, int month, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"nombre\": \"" + nombre + "\", \"apellido\": \"" + apellido + "\", \"fechaNacimiento\": \"" + fechaNacimiento + "\", \"documentoIdentidad\": \"" + documentoIdentidad + "\", \"familiar1\": \"" + familiar1 + "\", \"familiar2\": \"" + familiar2 + "\", \"year\": " + year + ", \"month\": " + month + "}";
        ApiClient.postResidentAdmin(context,idResi,jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllResidentsAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al añadir residente: " + error);
                onError.run();
            }
        });
    }

    public void addGame( Context context, String name, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"nombre\": \"" + name + "\"}";
        ApiClient.postGame(context,jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchAllGamesAdmin(context, onSuccess, onError);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al añadir juego: " + error);
                onError.run();
            }
        });
    }


    // -------------------- Otras acciones API no ADMIN --------------------

    public void login(Context context, String email, String password, boolean rememberPassword, Runnable onSuccess, Runnable onSuccesAdmin, Runnable onError) {
        ApiClient.postLogin(email, password, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                try {
                    JSONObject json = new JSONObject(jsonText);
                    String token = json.getString("token");
                    long expiresIn = json.getLong("expiresIn");
                    long idResidence = json.getLong("idResidencia");
                    long idUser = json.getLong("idUser");

                    long tokenExpiration = System.currentTimeMillis() + expiresIn;

                    SharedPreferences.Editor editor = SecurePreferencesUtil.getEncryptedPrefs(context).edit();
                    editor.putString("token", token);
                    editor.putLong("token_expiration", tokenExpiration);
                    editor.putBoolean("rememberPassword", rememberPassword);
                    editor.putLong("idResidence", idResidence);
                    editor.putLong("idUser", idUser);

                    if (rememberPassword) {
                        editor.putString("email", email);
                        editor.putString("password", password);
                    }

                    SecurePreferencesUtil.edit(context, editor);

                    if(idUser == 1)
                        fetchAllResidencesAdmin(context, onSuccesAdmin, onError);
                    else
                        fetchActualUser(context, onSuccess, onError);

                } catch (Exception e) {
                    throw new ParserException("Error al parsear respuesta de login: " + e.getMessage(), e);
                }
                Log.d("API", "Inicio de sesión exitoso");
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al iniciar sesión: " + error);
                onError.run();
            }
        });
    }

    public void changePassword( Context context, String currentPassword, String newPassword, Runnable onSuccess, Runnable onError) {
        ApiClient.patchChangePassword(context, currentPassword, newPassword, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                Log.d("API", "Contraseña cambiada correctamente");
                onSuccess.run();
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al cambiar contraseña: " + error);
                onError.run();
            }
        });
    }

    public void addResident(Context context, String nombre, String apellido, LocalDate fechaNacimiento, String documentoIdentidad, String familiar1, String familiar2, int year, int month, Runnable onSuccess, Runnable onError) {
        ApiClient.postResident(context, nombre, apellido, fechaNacimiento, documentoIdentidad, familiar1, familiar2, year, month, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchResidentsOnly(context, onSuccess);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al añadir residente: " + error);
                onError.run();
            }
        });
    }


    public void addActivity(Context context, String nombre, String descripcion, LocalDateTime fecha, Runnable onSuccess, Runnable onError) {
        ApiClient.postEvento(context, nombre, descripcion, fecha, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchActivitiesOnly(context, onSuccess);
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al añadir actividad: " + error);
            }
        });
    }

    public void deleteGameStat(Context context, long id, Runnable onSuccess, Runnable onError) {
        ApiClient.deleteGameStat(context, id, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchGameStatsOnly(context, onSuccess);
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al borrar partida: " + error);
            }
        });
    }

    public void updateObservation(Context context, String comment, long id, Runnable onSuccess, Runnable onError) {
        ApiClient.patchObservation(context, comment, id, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchGameStatsOnly(context, onSuccess);
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al actualizar observación: " + error);
            }
        });
    }

    public void changeActivityState(Context context, long id, ActivityState state, Runnable onSuccess, Runnable onError) {
        ApiClient.patchActivityState(context, id, state, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchActivitiesOnly(context, onSuccess);
            }

            @Override
            public void onError(String error) {
                Log.e("API", "Error al cambiar estado: " + error);
                onError.run();
            }
        });
    }

    public void takeDownResident(Context context, long residentid, Runnable onSuccess, Runnable onError) {
        ApiClient.patchTakeOutResident( context, residentid, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                Log.d("API", "Residente dado de baja correctamente");
                fetchResidentsOnly(context, onSuccess); //TODO: Que llame a fetchResidentsTakenOutOnly también
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al eliminar residente: " + error);
            }
        });
    }

    public void updateAssistance(Context context, long activityId, long participantId, boolean assistance, Runnable onSuccess, Runnable onError) {
        if (assistance){
            ApiClient.allowParticipant( context, activityId, participantId , new ApiClient.RawCallback() {
                @Override
                public void onSuccess(String jsonText) {
                    fetchParticipantsOnly(context, onSuccess);
                    Log.d("API", "Asistencia actualizada correctamente");
                }

                @Override
                public void onError(String error) {
                    onError.run();
                    Log.e("API", "Error al actualizar asistencia: " + error);
                }
            });
        } else {
            ApiClient.denyParticipant( context, activityId, participantId , new ApiClient.RawCallback() {
                @Override
                public void onSuccess(String jsonText) {
                    fetchParticipantsOnly(context, onSuccess);
                    Log.d("API", "Asistencia actualizada correctamente");
                }

                @Override
                public void onError(String error) {
                    onError.run();
                    Log.e("API", "Error al actualizar asistencia: " + error);
                }
            });
        }
    }
    public void updateOpinion(Context context, long activityId, long participantId, boolean isPreOpinion, String opinion, Runnable onSuccess, Runnable onError) {
        String jsonBody = "";
        if (isPreOpinion) {
            jsonBody = "{\"preOpinion\": \"" + opinion + "\"}";
        } else {
            jsonBody = "{\"postOpinion\": \"" + opinion + "\"}";
        }
        ApiClient.patchParticipant( context, activityId, participantId , jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchParticipantsOnly(context, onSuccess);
                Log.d("API", "Opinión actualizada correctamente");
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al actualizar opinión: " + error);
            }
        });
    }
    public void updateMaterialHelp(Context context, long activityId, long participantId, boolean materialHelp, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"recursosMateriales\": " + materialHelp + "}";
        ApiClient.patchParticipant( context, activityId, participantId , jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchParticipantsOnly(context, onSuccess);
                Log.d("API", "Ayuda material actualizada correctamente");
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al actualizar ayuda material: " + error);
            }
        });
    }
    public void updateHumanHelp(Context context, long activityId, long participantId, boolean humanHelp, Runnable onSuccess, Runnable onError) {
        String jsonBody = "{\"recursosHumanos\": " + humanHelp + "}";
        ApiClient.patchParticipant( context, activityId, participantId , jsonBody, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchParticipantsOnly(context, onSuccess);
                Log.d("API", "Ayuda humana actualizada correctamente");
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al actualizar ayuda humana: " + error);
            }
        });
    }
    public void addParticipant(Context context, long activityId, long residentId, Runnable onSuccess, Runnable onError) {
        ApiClient.postParticipant(context, residentId, false, false, "", "", activityId, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchParticipantsOnly(context, onSuccess);
                Log.d("API", "Participante añadido correctamente");
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al añadir participante: " + error);
            }
        });
    }

    public void deleteParticipant(Context context, long activityId, long participantId, Runnable onSuccess, Runnable onError) {
        ApiClient.deleteParticipant(context, activityId, participantId, new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                fetchParticipantsOnly(context, onSuccess);
                Log.d("API", "Participante eliminado correctamente");
            }

            @Override
            public void onError(String error) {
                onError.run();
                Log.e("API", "Error al eliminar participante: " + error);
            }
        });
    }
}

