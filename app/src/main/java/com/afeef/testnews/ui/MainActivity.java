package com.afeef.testnews.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.appcompat.app.AppCompatActivity;

import com.afeef.testnews.api.ApiInterface;
import com.afeef.testnews.data.NewsDatabase;
import com.afeef.testnews.data.dao.SavedDao;
import com.afeef.testnews.models.WeatherResponse;
import com.afeef.testnews.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.afeef.testnews.R;
import com.afeef.testnews.api.ApiClient;
import com.afeef.testnews.ui.fragment.HomeFragment;
import com.afeef.testnews.ui.fragment.SavedNewsFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawer;

    HomeFragment homeFragment;
    SavedNewsFragment savedNewsFragment;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_SAVED = "savec";
    private static final String TAG_CLEAR = "clear";

    public static String CURRENT_TAG = TAG_HOME;
    private Handler mHandler;

    SavedDao savedDao;

    private final FragmentManager fragmentManager = getSupportFragmentManager();

    TextView tv_city_name, tv_date, tv_weather, tv_tempreature, tv_max_temp, tv_min_temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler();
        savedDao = NewsDatabase.getInstance(this).savedDao();


        if (savedInstanceState == null) {
            // Add a default fragment
            homeFragment = HomeFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(R.id.nav_host_fragment, homeFragment)
                    .commit();
        }


        navigationView = findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);

        tv_city_name = header.findViewById(R.id.tv_city_name);
        tv_date = header.findViewById(R.id.tv_date);
        tv_weather = header.findViewById(R.id.tv_weather);
        tv_tempreature = header.findViewById(R.id.tv_tempreature);
        tv_max_temp = header.findViewById(R.id.tv_max_temp);
        tv_min_temp = header.findViewById(R.id.tv_min_temp);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_saved:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_SAVED;
                        break;
                    case R.id.nav_clear:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_CLEAR;
                        savedDao.removeAllSaved();
                        drawer.close();
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                if (navItemIndex != 2){
                    loadHomeFragment();
                }

                return true;
            }
        });
        drawer = findViewById(R.id.drawer_layout);

        tv_city_name.setText("Bangalore");
        tv_date.setText(Utils.getLastSyncString());

        getWeatherData("Bangalore");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2) {

                    homeFragment.onLoadingSwipeRefresh(query);
                } else {
                    Toast.makeText(MainActivity.this, "Type more than two letters!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);

        return true;
    }


    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.nav_host_fragment, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }


        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }


    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                return homeFragment;
            case 1:
                // photos
                savedNewsFragment = SavedNewsFragment.newInstance();
                return savedNewsFragment;

            default:
                return new HomeFragment();
        }
    }


    private void getWeatherData(String name) {

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

        Call<WeatherResponse> call = apiInterface.getWeatherData(name);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {

                try {
                    WeatherResponse weatherResponse = response.body();

                    tv_weather.setText(weatherResponse.weather.get(0).description);
                    tv_tempreature.setText(weatherResponse.main.temp+"\u2103");
                    tv_max_temp.setText("Max : "+weatherResponse.main.temp_max);
                    tv_min_temp.setText("Min : "+weatherResponse.main.temp_min);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {

            }
        });

    }


    @Override
    public void onBackPressed() {

    }
}
