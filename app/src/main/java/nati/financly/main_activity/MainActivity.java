package nati.financly.main_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import nati.financly.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    TextView welcomeText;
    String timeWelcome = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.main_drawer);
        NavigationView navigationView = findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.OpenDrawer, R.string.CloseDrawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }

        DatabaseReference userRef = myRef.child("Users").child(userId).child("name");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);

                welcomeText = findViewById(R.id.drawer_header_welcome_user_txt_view);
                Calendar c = Calendar.getInstance();
                int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

                if (timeOfDay >= 6 && timeOfDay < 12) {
                    timeWelcome = getString(R.string.good_morning);
                } else if (timeOfDay >= 12 && timeOfDay < 16) {
                    timeWelcome = getString(R.string.good_noon);
                } else if (timeOfDay >= 16 && timeOfDay < 18) {
                    timeWelcome = getString(R.string.good_afternoon);
                } else if (timeOfDay >= 18 && timeOfDay < 21) {
                    timeWelcome = getString(R.string.good_evening);
                } else if (timeOfDay >= 21 && timeOfDay < 24 || timeOfDay >= 0 && timeOfDay < 6) {
                    timeWelcome = getString(R.string.good_night);
                }
                String welcome = timeWelcome + ", " + name;
                welcomeText.setText(welcome);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        toolbar = findViewById(R.id.main_toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide(); // Hiding the activity costume toolbar because the first page is fragment with it's own costume toolbar.
        }
        //Setting the costume tool bar of the activity


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new BalanceFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_balance);//Show that we are on the balance page - highlight the message item.
        }

        listenToUserName();
    }
    //End of onCreate//

    //broadcast receiver to get the name of the user if he changes it.
    private void listenToUserName() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        BroadcastReceiver listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("name");
                String newWelcome = timeWelcome + ", " + name;
                welcomeText.setText(newWelcome);
            }
        };
        IntentFilter filter = new IntentFilter("user_name");

        manager.registerReceiver(listener, filter);
    }


    //Action when the back button is pressed.
    @Override
    public void onBackPressed() {
        //If the drawer is open, close it.
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else { // else - if it is close - get out of the app
           // super.onBackPressed();
            DialogLogout dialog = new DialogLogout();
            dialog.show(getSupportFragmentManager(), "dialog");
        }
    }

    //Handling the clicks on the drawer.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //When selecting every navigation item(except the balance and the logout) - show the activity toolbar.
        int itemId = item.getItemId();

        if (itemId != R.id.nav_logout) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            item.setChecked(true);
        }
        switch (itemId) {
            case R.id.nav_balance:
                //Only in this item (fragment balance) - hide the activity tool bar, showing only the fragment's costume toolbar.
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BalanceFragment(), "balance_fragment").commit();
                break;
            case R.id.nav_details:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserDetailsFragment()).commit();
                getSupportActionBar().setTitle(R.string.my_details);
                break;
            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                getSupportActionBar().setTitle(R.string.about);
                break;
            case R.id.nav_logout:
                item.setCheckable(false);
                DialogLogout dialog = new DialogLogout();
                dialog.show(getSupportFragmentManager(), "dialog");
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }


}
