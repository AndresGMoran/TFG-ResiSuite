package com.andresgmoran.apptrabajadores.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnActivityListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnAddParticipantListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameStatsListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnParticipantListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnResidentListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnChageStateActivityListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnClickOnBackButtonListener;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;
import com.andresgmoran.apptrabajadores.network.ApiClient;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.andresgmoran.apptrabajadores.ui.fragments.account.AccountFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.activities.ActivitiesFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.activities.ActivityDetailFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.activities.AddActivityFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.activities.OpinionFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.activities.ParticipantDetailFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.activities.ParticipantSelectionDialogFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.admin.AdminPanelFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.authentication.LoginFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.game.GameFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.gameDetail.GameDetailFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.home.HomeFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.residence.ResidenceDetailFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.resident.AddResidentFragment;
import com.andresgmoran.apptrabajadores.ui.fragments.resident.ResidentFragment;
import com.andresgmoran.apptrabajadores.utils.SecurePreferencesUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        AdminPanelFragment.IOnAdminPanel,
        NavigationBarView.OnItemSelectedListener,
        IOnClickOnBackButtonListener,
        HomeFragment.IOnRefreshHomeListener, HomeFragment.IOnClickOnAddParticipantListener,
        LoginFragment.OnLoginListener,
        IOClickOnResidentListener, AddResidentFragment.OnAddResidentListener, ResidentFragment.IOnRefreshResidentListener,
        IOClickOnGameListener, GameFragment.IOnRefreshGameListener,
        IOClickOnGameStatsListener, GameDetailFragment.IOnAddObservationListener, GameDetailFragment.IOnRefreshGameStatsListener,
        IOClickOnActivityListener, AddActivityFragment.IOnAddActivity, IOnChageStateActivityListener, ActivityDetailFragment.OnRefreshActivityDetailListener, ActivitiesFragment.IOnActivities,
        IOClickOnParticipantListener, IOClickOnAddParticipantListener, ParticipantSelectionDialogFragment.OnParticipantSelectedListener, ParticipantDetailFragment.IOnRefreshParticipantDetailListener,
        OpinionFragment.OnAddOpinionListener,
        AccountFragment.IOAccountFragmentListener,
        ResidenceDetailFragment.IOnRefreshResidenceDetailListener{

    private Fragment lastFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adaptar diseño a la pantalla pudiendo cambiar el color del padding top desde los fragments que sean necesarios
        View rootView = findViewById(R.id.fcvMain);
        //View statusBarView = findViewById(R.id.status_bar_background);
        View statusBarView = findViewById(R.id.status_bar_background);


        if (rootView != null && statusBarView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);

                ViewGroup.LayoutParams params = statusBarView.getLayoutParams();
                params.height = systemBars.top;
                statusBarView.setLayoutParams(params);

                return insets;
            });
        }

        // -------- INICIO AUTOMÁTICO SI RECUERDA CONTRASEÑA Y TOKEN VÁLIDO --------
        try {
            boolean rememberPassword = SecurePreferencesUtil.getBoolean(this, "rememberPassword", false);
            String token = SecurePreferencesUtil.getString(this, "token", null);
            long expiration = SecurePreferencesUtil.getLong(this, "token_expiration", 0);

            if (rememberPassword) {
                if (token != null && System.currentTimeMillis() < expiration) {
                    ProgressBar loader = findViewById(R.id.progress_loader);
                    loader.setVisibility(View.VISIBLE);

                    AppDataRepository.getInstance().fetchActualUser(this, () -> {
                        runOnUiThread(() -> {
                            loadFragment(createHomeFragment());
                            findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
                            loader.setVisibility(View.GONE);
                        });
                    }, () -> {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.progress_loader).setVisibility(View.GONE);
                            loadFragment(new LoginFragment());
                        });
                    });
                } else {
                    Log.i( "AUTH", "Token no válido o expirado, intentando login automático");
                    // Token expirado: intentar login automático
                    String email = SecurePreferencesUtil.getString(this, "email", null);
                    String password = SecurePreferencesUtil.getString(this, "password", null);
                    if (email != null && password != null) {
                        postLogin(email, password, true);
                    } else {
                        loadFragment(new LoginFragment());
                    }
                }
            } else {
                loadFragment(new LoginFragment());
            }

        } catch (Exception e) {
            Log.e("AUTH", "Error leyendo preferencias: " + e.getMessage());
            loadFragment(new LoginFragment());
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setVisibility(View.GONE);
        navView.setOnItemSelectedListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String language = SecurePreferencesUtil.getString(newBase, "language", "es");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.setLocale(locale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment f = null;
        int id = item.getItemId();
        if (id == R.id.navigation_home)
            f = createHomeFragment();
        else if (id == R.id.navigation_activities)
            f = new ActivitiesFragment();
        else if (id == R.id.navigation_account)
            f = new AccountFragment();

        return loadFragment(f);
    }

// --------------------------------------------------------------------- Login ---------------------------------------------------------------------

    /**
     * Llama al método de login con los datos introducidos en el fragmento de LoginFragment
     * @param email correo electrónico del usuario
     * @param password contraseña del usuario
     * @param rememberPassword indica si se debe recordar la contraseña
     */
    @Override
    public void onLogin(String email, String password, boolean rememberPassword) {
        postLogin(email, password, rememberPassword);
    }

    /**
     * Realiza el login con los datos introducidos
     * @param email correo electrónico del usuario
     * @param password contraseña del usuario
     * @param rememberPassword indica si se debe recordar la contraseña
     */
    public void postLogin(String email, String password, boolean rememberPassword) {
        AppDataRepository.getInstance().login(MainActivity.this, email, password, rememberPassword, () -> runOnUiThread(() -> {
                    loadFragment(createHomeFragment());
                    findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
                    findViewById(R.id.progress_loader).setVisibility(View.GONE);
                    ((BottomNavigationView) findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_home);
                }), () -> runOnUiThread(() -> {
                    loadFragment(new AdminPanelFragment());
                }),
                () -> runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.progress_loader).setVisibility(View.GONE);
                })
        );
    }




    // --------------------------------------------------------------------- Fragment Creation ---------------------------------------------------------------------

    /**
     * Cambia el fragmento actual por otro
     * @param fragment fragmento a cargar
     * @return true si se ha cargado correctamente, false si no
     */
    private boolean loadFragment(Fragment fragment) {
        if (isFinishing() || isDestroyed()) return false;

        if (fragment != null) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcvMain);
            if (currentFragment != null) {
                lastFragment = currentFragment;
            }
            if (fragment instanceof GameFragment || fragment instanceof ResidentFragment){
                lastFragment = createHomeFragment();
            }
            if (fragment instanceof ActivityDetailFragment) {
                lastFragment = new ActivitiesFragment();
            }
            if (fragment instanceof ResidenceDetailFragment) {
                lastFragment = new AccountFragment();
            }
        }
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fcvMain, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Crea el fragmento de HOME
     * @return fragmento creado
     */
    public Fragment createHomeFragment(){
        HomeFragment homeFragment = new HomeFragment();
        return homeFragment;
    }

    /**
     * Crea el fragmento de RESIDENT
     * @param resident residente a mostrar en el fragmento
     * @return fragmento creado
     */
    public Fragment createResidentFragment(Resident resident){
        ResidentFragment residentFragment = new ResidentFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("resident", resident);
        residentFragment.setArguments(bundle);
        return residentFragment;
    }

    /**
     * Crea el fragmento de GAME
     * @param game juego a mostrar en el fragmento
     * @return fragmento creado
     */
    public Fragment createGameFragment(Game game){
        GameFragment gameFragment = new GameFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("game", game);
        gameFragment.setArguments(bundle);
        return gameFragment;
    }

    /**
     * Crea el fragmento de GAME DETAIL
     * @param gameStat partida a mostrar en el fragmento
     * @param gameStatResident residente que ha jugado la partida
     * @param gameStatGame juego que se ha jugado
     * @return fragmento creado
     */
    public Fragment createGameDetailFragment(GameStat gameStat, Resident gameStatResident, Game gameStatGame){
        GameDetailFragment gameDetailFragment = new GameDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("gameStat", gameStat);
        bundle.putSerializable("gameStatResident", gameStatResident);
        bundle.putSerializable("gameStatGame", gameStatGame);
        gameDetailFragment.setArguments(bundle);
        return gameDetailFragment;
    }

    /**
     * Crea el fragmento de ActivityDetail
     * @param activity actividad a mostrar en el fragmento
     * @param participants lista de participantes de la actividad
     * @return fragmento creado
     */
    public Fragment createActivityDetailFragment(Activity activity, List<ActivityResident> participants){
        ActivityDetailFragment activityDetailFragment = new ActivityDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("activity", activity);
        bundle.putSerializable("participants", (ArrayList<ActivityResident>) participants);
        activityDetailFragment.setArguments(bundle);
        return activityDetailFragment;
    }

    /**
     * Para el botón de ir hacia atrás, depende del fragmento que se esté mostrando, recargará el fragmento correspondiente
     */
    private void reloadFragment(Fragment fragment) {
        if (fragment == null) return;

        Bundle args = fragment.getArguments();

        if (fragment instanceof HomeFragment) {
            loadFragment(createHomeFragment());

        } else if (fragment instanceof ResidentFragment && args != null) {
            Resident resident = (Resident) args.getSerializable("resident");
            if (resident != null) {
                loadFragment(createResidentFragment(resident));
            }

        } else if (fragment instanceof GameFragment && args != null) {
            Game game = (Game) args.getSerializable("game");
            if (game != null) {
                loadFragment(createGameFragment(game));
            }

        } else if (fragment instanceof GameDetailFragment && args != null) {
            GameStat originalStat = (GameStat) args.getSerializable("gameStat");
            Resident resident = (Resident) args.getSerializable("gameStatResident");
            Game game = (Game) args.getSerializable("gameStatGame");

            if (originalStat != null && resident != null && game != null) {
                GameStat updatedStat = null;
                for (GameStat stat : AppDataRepository.getInstance().getGameStats()) {
                    if (stat.getId() == originalStat.getId()) {
                        updatedStat = stat;
                        break;
                    }
                }
                if (updatedStat != null) {
                    loadFragment(createGameDetailFragment(updatedStat, resident, game));
                }
            }

        } else if (fragment instanceof ActivitiesFragment) {
            loadFragment(new ActivitiesFragment());

        } else if (fragment instanceof ActivityDetailFragment && args != null) {
            Activity originalActivity = (Activity) args.getSerializable("activity");
            if (originalActivity != null) {
                Activity updatedActivity = null;
                for (Activity act : AppDataRepository.getInstance().getActivities()) {
                    if (act.getId() == originalActivity.getId()) {
                        updatedActivity = act;
                        break;
                    }
                }

                if (updatedActivity != null) {
                    List<ActivityResident> participants = new ArrayList<>();
                    for (ActivityResident ar : AppDataRepository.getInstance().getActivityResidents()) {
                        if (ar.getActivityId() == updatedActivity.getId()) {
                            participants.add(ar);
                            Log.e( "API", "Reloading ActivityDetailFragment for activity: " + ar.isHumanHelp());
                        }
                    }
                    Log.e( "API", "Reloading ActivityDetailFragment for activity: " + updatedActivity.getId());
                    loadFragment(createActivityDetailFragment(updatedActivity, participants));
                }
            }
        } else if (fragment instanceof AccountFragment){
            loadFragment(new AccountFragment());
        }
    }


    // --------------------------------------------------------------------- Resident listeners ---------------------------------------------------------------------

    /**
     * Listener para el click en un residente
     * @param resident residente sobre el que se ha hecho click
     */
    @Override
    public void onClickOnResident(Resident resident) {
        Fragment f = createResidentFragment(resident);
        loadFragment(f);
    }

    /**
     * Listener para el click en el botón de dar de baja a un residente
     * @param resident residente que se quiere dar de baja
     */
    @Override
    public void onTakeOutResident(Resident resident, Runnable refresh) {
        AppDataRepository.getInstance().takeDownResident(MainActivity.this, resident.getId(), () -> {
            runOnUiThread(() -> {
                AppDataRepository.getInstance().fetchResidentsTakenOutOnly( MainActivity.this, () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Residente dado de baja correctamente", Toast.LENGTH_SHORT).show();
                        refresh.run();
                    });
                });
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al dar de baja al residente: ", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el envío del formulario de añadir residente
     * @param nombre nombre del residente
     * @param apellido apellido del residente
     * @param fechaNacimiento fecha de nacimiento del residente
     * @param documentoIdentidad documento de identidad del residente
     * @param familiar1 correo electrónico del primer familiar
     * @param familiar2 correo electrónico del segundo familiar
     * @param year año de validez de documento de identidad
     * @param month mes de validez de documento de identidad
     */
    @Override
    public void onAddResidentFormSubmitted(String nombre, String apellido, LocalDate fechaNacimiento, String documentoIdentidad, String familiar1, String familiar2, int year, int month) {
        AppDataRepository.getInstance().addResident( MainActivity.this, nombre, apellido, fechaNacimiento, documentoIdentidad, familiar1, familiar2, year, month, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Residente añadido correctamente", Toast.LENGTH_SHORT).show();
                if (lastFragment instanceof HomeFragment) {
                    ((HomeFragment) lastFragment).refreshData();
                }
                loadFragment(lastFragment);
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al añadir residente", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // --------------------------------------------------------------------- Game listeners ---------------------------------------------------------------------

    /**
     * Listener para el click en un juego
     * @param game juego sobre el que se ha hecho click
     */
    @Override
    public void onClickOnGame(Game game) {
        Fragment f = createGameFragment(game);
        loadFragment(f);
    }

    // --------------------------------------------------------------------- GameStats Listeners ---------------------------------------------------------------------

    /**
     * Listener para el click en una partida
     * @param gameStat partida sobre la que se ha hecho click
     * @param gameStatResident residente que ha jugado la partida
     * @param gameStatGame juego que se ha jugado
     */
    @Override
    public void onClickOnLatestGame(GameStat gameStat, Resident gameStatResident, Game gameStatGame) {
        loadFragment(createGameDetailFragment( gameStat, gameStatResident, gameStatGame));
    }

    /**
     * Listener para el click en el botón de eliminar partida
     * @param gameStat partida que se quiere eliminar
     * @param gamestatGame juego de la partida
     */
    @Override
    public void onDeleteGameStat(GameStat gameStat, Game gamestatGame, Runnable refresh) {
        AppDataRepository.getInstance().deleteGameStat(MainActivity.this, gameStat.getId(), () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Partida eliminada correctamente", Toast.LENGTH_SHORT).show();
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al eliminar partida", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el click en el botón de añadir observación a una partida
     * @param observation observación a añadir
     * @param gameId id del juego al que pertenece la partida
     * @param gameStatId id de la partida a la que se quiere añadir la observación
     */
    @Override
    public void onAddObservation(String observation, long gameId, long gameStatId, Runnable refresh) {
        AppDataRepository.getInstance().updateObservation(MainActivity.this, observation, gameStatId, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Observación añadida correctamente", Toast.LENGTH_SHORT).show();
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al añadir observación", Toast.LENGTH_SHORT).show();
            });
        });
    }


    /**
     * Listener para la actualización de las estadísticas de los juegos
     * @param refresh Runnable para refrescar la vista después de actualizar las estadísticas
     */
    @Override
    public void onRefreshGameStats(Runnable refresh) {
        AppDataRepository.getInstance().fetchGameStatsOnly( MainActivity.this, () -> {
            runOnUiThread(() -> {
                refresh.run();
            });
        });
    }

    // --------------------------------------------------------------------- Activity Listeners ---------------------------------------------------------------------

    /**
     * Listener para el click en una actividad
     * @param activity actividad sobre la que se ha hecho click
     * @param participants lista de participantes de la actividad
     */
    @Override
    public void onClickOnActivity(Activity activity, List<ActivityResident> participants) {
        loadFragment(createActivityDetailFragment(activity, participants));
    }

    /**
     * Listener para el click en el botón de añadir actividad
     */
    @Override
    public void onClickOnAddActivity() {
        loadFragment(new AddActivityFragment());
    }

    /**
     * Listener para el envío del formulario de añadir actividad
     * @param activityName nombre de la actividad
     * @param activityDescription descripción de la actividad
     * @param date fecha de la actividad
     */
    @Override
    public void onAddActivity(String activityName, String activityDescription, LocalDateTime date) {
        AppDataRepository.getInstance().addActivity(MainActivity.this, activityName, activityDescription, date, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Actividad añadida correctamente", Toast.LENGTH_SHORT).show();
                if (lastFragment instanceof ActivitiesFragment)
                    ((ActivitiesFragment) lastFragment).reloadData();
                loadFragment(lastFragment);
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al añadir actividad", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el click en el botón de eliminar actividad
     * @param activity actividad que se quiere eliminar
     * @param refresh Runnable para refrescar la vista después de eliminar la actividad
     */
    @Override
    public void onDeleteActivitie(Activity activity, Runnable refresh) {
        ApiClient.deleteActivity(MainActivity.this, activity.getId(), new ApiClient.RawCallback() {
            @Override
            public void onSuccess(String jsonText) {
                AppDataRepository.getInstance().fetchActivitiesOnly(MainActivity.this, () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Actividad eliminada correctamente", Toast.LENGTH_SHORT).show();
                        refresh.run();
                    });
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Error al eliminar actividad", Toast.LENGTH_SHORT).show();
                Log.e("API", "Error al eliminar actividad: " + error);
            }
        });
    }

    /**
     * Listener para el cambio de estado de una actividad
     * @param activity actividad sobre la que se ha cambiado el estado
     * @param state nuevo estado de la actividad (Abierto -> Cerrado -> En curso -> Finalizado)
     * @param refresh Runnable para refrescar la vista después de cambiar el estado
     */
    @Override
    public void onChangeStateActivity(Activity activity, ActivityState state, Runnable refresh) {
        Log.e( "API", "Cambiando estado de la actividad: " + activity.getId());
        AppDataRepository.getInstance().changeActivityState(MainActivity.this, activity.getId(), state, () -> {
            runOnUiThread(() -> {
                AppDataRepository.getInstance().fetchActivitiesOnly( MainActivity.this, () -> {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Estado de la actividad actualizado correctamente", Toast.LENGTH_SHORT).show();
                        refresh.run();
                    });
                });
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al actualizar el estado de la actividad", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para la actualización de los detalles de una actividad
     * @param refresh Runnable para refrescar la vista después de actualizar los detalles
     */
    @Override
    public void onRefreshActivityDetail(Runnable refresh) {
        AppDataRepository.getInstance().fetchActivitiesOnly( MainActivity.this, () -> {
            runOnUiThread(() -> {
                AppDataRepository.getInstance().fetchParticipantsOnly( MainActivity.this, () -> {
                    runOnUiThread(() -> {
                        refresh.run();
                    });
                });
            });
        });
    }

    /**
     * Listener para la actualización de las actividades
     * @param refresh Runnable para refrescar la vista después de actualizar las actividades
     */
    @Override
    public void onRefreshActivities(Runnable refresh) {
        AppDataRepository.getInstance().fetchActivitiesOnly( MainActivity.this, () -> {
            runOnUiThread(() -> {
                refresh.run();

            });
        });
    }

    // --------------------------------------------------------------------- Participant Listeners ---------------------------------------------------------------------


    /**
     * Listener para el click en el botón de añadir participante a una actividad
     * @param activity actividad a la que se quiere añadir un participante
     * @param participants lista de participantes de la actividad
     */
    @Override
    public void onClickOnAddParticipant(Activity activity, List<ActivityResident> participants) {
        ParticipantSelectionDialogFragment dialog = new ParticipantSelectionDialogFragment(activity, participants);
        dialog.show(getSupportFragmentManager(), "ParticipantDialog");
    }

    /**
     * Listener para el click en el botón de añadir participante desde el fragmento de actividades
     */
    @Override
    public void onClickOnAddParticipant() {
        loadFragment(new AddResidentFragment());
    }

    /**
     * Listener para el click en un participante
     * @param participant participante sobre el que se ha hecho click
     */
    @Override
    public void onClickOnParticipant(ActivityResident participant) {
        Fragment f = new ParticipantDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("participant", participant);
        f.setArguments(bundle);
        loadFragment(f);
    }

    /**
     * Listener para el click en el botón de asistencia de un participante
     * @param participant participante sobre el que se ha hecho click
     * @param assistance indica si el participante ha asiste o no a la actividad
     * @param refresh Runnable para refrescar la vista después de actualizar la asistencia
     */
    @Override
    public void onClickOnAssistance(ActivityResident participant, boolean assistance, Runnable refresh ) {
        AppDataRepository.getInstance().updateAssistance(MainActivity.this, participant.getActivityId(), participant.getId(), assistance, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Asistencia actualizada correctamente", Toast.LENGTH_SHORT).show();
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al actualizar asistencia", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el click en el botón de opinión de un participante
     * @param participant participante sobre el que se ha hecho click
     * @param isPreOpinion indica si es una opinión previa o posterior a la actividad
     */
    @Override
    public void onClickOnOpinion(ActivityResident participant, boolean isPreOpinion) {
        Fragment f = new OpinionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("participant", participant);
        bundle.putBoolean("isPreOpinion", isPreOpinion);
        f.setArguments(bundle);
        loadFragment(f);
    }

    /**
     * Listener para el envío de la opinión de un participante
     * @param participant participante al que se le añade la opinión
     * @param isPreOpinion indica si es una opinión previa o posterior a la actividad
     * @param opinion opinión a añadir
     */
    @Override
    public void onAddOpinion(ActivityResident participant ,boolean isPreOpinion, String opinion) {
        AppDataRepository.getInstance().updateOpinion( MainActivity.this, participant.getActivityId(), participant.getId(), isPreOpinion, opinion, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Opinión actualizada correctamente", Toast.LENGTH_SHORT).show();
                if (lastFragment instanceof ActivityDetailFragment)
                    ((ActivityDetailFragment) lastFragment).updateData();
                loadFragment(lastFragment);
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al actualizar opinión", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el click en el botón de ayuda material de un participante
     * @param participant participante sobre el que se ha hecho click
     * @param materialHelp indica si el participante necesita ayuda material
     * @param refresh Runnable para refrescar la vista después de actualizar la ayuda material
     */
    @Override
    public void onClickOnMaterialHelp(ActivityResident participant, boolean materialHelp, Runnable refresh) {
        AppDataRepository.getInstance().updateMaterialHelp( MainActivity.this, participant.getActivityId(), participant.getId(), materialHelp, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Ayuda material actualizada correctamente", Toast.LENGTH_SHORT).show();
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al actualizar ayuda material", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el click en el botón de ayuda humana de un participante
     * @param participant participante sobre el que se ha hecho click
     * @param humanHelp indica si el participante necesita ayuda humana
     * @param refresh Runnable para refrescar la vista después de actualizar la ayuda humana
     */
    @Override
    public void onClickOnHumanHelp(ActivityResident participant, boolean humanHelp, Runnable refresh) {
        AppDataRepository.getInstance().updateHumanHelp(MainActivity.this, participant.getActivityId(), participant.getId(), humanHelp, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Ayuda humana actualizada correctamente", Toast.LENGTH_SHORT).show();
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al actualizar ayuda humana", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para el click en el botón de eliminar participante
     * @param participant participante que se quiere eliminar
     * @param refresh Runnable para refrescar la vista después de eliminar el participante
     */
    @Override
    public void onDeleteParticipant(ActivityResident participant, Runnable refresh) {
        AppDataRepository.getInstance().deleteParticipant(MainActivity.this, participant.getActivityId(), participant.getId(), () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Participante eliminado correctamente", Toast.LENGTH_SHORT).show();
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al eliminar participante", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para la selección de un participante en el diálogo de selección de participantes
     * @param activity actividad a la que se quiere añadir el participante
     * @param participants lista de participantes de la actividad
     * @param selectedResident residente seleccionado para añadir como participante
     */
    @Override
    public void onParticipantSelected(Activity activity, List<ActivityResident> participants ,Resident selectedResident) {
        Log.e( "API", "Adding participant: " + selectedResident.getId() + " to activity: " + activity.getId());
        AppDataRepository.getInstance().addParticipant( MainActivity.this, activity.getId(), selectedResident.getId(), () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Participante añadido correctamente", Toast.LENGTH_SHORT).show();
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fcvMain);
                if (currentFragment instanceof ActivityDetailFragment) {
                    ((ActivityDetailFragment) currentFragment).updateData();
                }
                loadFragment(currentFragment);
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al añadir participante", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para la actualización de los detalles de un participante
     * @param refresh Runnable para refrescar la vista después de actualizar los detalles del participante
     */
    @Override
    public void onRefreshParticipantDetail(Runnable refresh) {
        AppDataRepository.getInstance().fetchParticipantsOnly( MainActivity.this, () -> {
            runOnUiThread(() -> {
                refresh.run();
            });
        });
    }

    // --------------------------------------------------------------------- Account listeners ---------------------------------------------------------------------

    @Override
    public void OnResidenceButtonClicked() {
        loadFragment( new ResidenceDetailFragment());
    }

    @Override
    public void onLanguageSelected(String languageCode) {
        SharedPreferences.Editor editor = SecurePreferencesUtil.getEncryptedPrefs(MainActivity.this).edit();
        editor.putString("language", languageCode);
        editor.apply();

        recreate();
        ((BottomNavigationView) findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_home);
    }

    @Override
    public void onLogOutButtonClicked() {
        SecurePreferencesUtil.clear(MainActivity.this);
        loadFragment(new LoginFragment());
        findViewById(R.id.nav_view).setVisibility(View.GONE);
    }

    @Override
    public void onChangePassword(String currentPassword, String newPassword) {
        AppDataRepository.getInstance().changePassword( MainActivity.this, currentPassword, newPassword, () -> {
            runOnUiThread(() -> {
                SharedPreferences.Editor editor = SecurePreferencesUtil.getEncryptedPrefs(MainActivity.this).edit();
                editor.putString("password", newPassword);
                Toast.makeText(MainActivity.this, "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show();
                loadFragment(getSupportFragmentManager().findFragmentById(R.id.fcvMain));
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Listener para la actualización de los detalles de la residencia
     * @param refresh Runnable para refrescar la vista después de actualizar los detalles de la residencia
     */
    @Override
    public void onRefreshResidenceDetail(Runnable refresh) {
        AppDataRepository.getInstance().fetchResidenceOnly( MainActivity.this, () -> {
            runOnUiThread(() -> {
                AppDataRepository.getInstance().fetchResidentsOnly(MainActivity.this, () -> {
                    runOnUiThread(() -> {
                        AppDataRepository.getInstance().fetchResidentsTakenOutOnly( MainActivity.this, () -> {
                            runOnUiThread(() -> {
                                AppDataRepository.getInstance().fetchGamesOnly( MainActivity.this, () -> {
                                    runOnUiThread(() -> {
                                        AppDataRepository.getInstance().fetchGameStatsOnly( MainActivity.this, () -> {
                                            runOnUiThread(() -> {
                                                refresh.run();
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    // --------------------------------------------------------------------- General listeners ---------------------------------------------------------------------

    /**
     * Listener para el click en el botón de volver atrás
     */
    @Override
    public void onClickOnBackButton() {
        Log.e( "API", "Back button clicked, reloading last fragment: " + (lastFragment != null ? lastFragment.getClass().getSimpleName() : "null"));
        reloadFragment(lastFragment);
    }

    /**
     * Listener para la actualización de la pantalla de inicio
     * @param refresh Runnable para refrescar la vista después de actualizar la pantalla de inicio
     */
    @Override
    public void onRefreshHome(Runnable refresh) {
        AppDataRepository.getInstance().fetchActualUser( MainActivity.this, () -> {
            runOnUiThread(() -> {
                refresh.run();
            });
        }, () -> {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            });
        });
    }


}