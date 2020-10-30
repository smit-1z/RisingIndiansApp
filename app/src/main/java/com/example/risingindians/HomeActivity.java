package com.example.risingindians;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private String current_user_id;
    private FloatingActionButton addpostbtn;

    private BottomNavigationView navigationView;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;
    private Uri profileImageURI= null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private EventsFragment eventsFragment;
    private DonateFragment donateFragment;
    private Timeline_Fragment timelineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        ActionBar actionBar;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#E26347"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        dl = (DrawerLayout)findViewById(R.id.activity_home);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        nv = (NavigationView)findViewById(R.id.nv);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser() != null) {
            navigationView = findViewById(R.id.bottom_navigation);

            // FRAGMENTS
            eventsFragment = new EventsFragment();
            donateFragment = new DonateFragment();
            timelineFragment = new Timeline_Fragment();

            initializeFragment();

            nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    switch(id)
                    {
                        case R.id.action_logout_btn:
                            logout();
                            return true;

                        case R.id.action_settings_btn:
                            Intent settingsintent = new Intent(HomeActivity.this, AccountSetupActivity.class);
                            startActivity(settingsintent);
                            return true;

                        case R.id.action_contact_btn:
                            Intent contactintent = new Intent(HomeActivity.this, ContactUsActivity.class);
                            startActivity(contactintent);
                            return true;

                        case R.id.action_aboutus_btn:
                            Intent aboutintent = new Intent(HomeActivity.this, About_Activity.class);
                            startActivity(aboutintent);
                            return true;

                        default:
                            return false;
                    }

                }
            });

            navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                    switch (item.getItemId()) {

                        case R.id.navigation_home:

                            replaceFragment(eventsFragment, currentFragment);
                            return true;


                        case R.id.navigation_donate:

                            replaceFragment(donateFragment, currentFragment);
                            return true;

                        case R.id.navigation_timeline:

                            replaceFragment(timelineFragment, currentFragment);
                            return true;

                        default:
                            return false;


                    }
                }
            });


            addpostbtn = findViewById(R.id.add_post_btn);
            addpostbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeActivity.this, NewEventActivity.class);
                    startActivity(intent);
                }
            });



        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }



    /*private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()){

            case R.id.navigation_home:
                fragment = new EventsFragment();
                break;

            case R.id.navigation_donate:
                fragment = new DonateFragment();
                break;

            case R.id.navigation_timeline:
                fragment = new TimelineFragment();
                break;

            case R.id.navigation_account:
                fragment = new AccountFragment();
                break;

        }
        return loadFragment(fragment);
    }*/

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.top_menu,menu);

        return true;
    }*/

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            sendToLogin();
        }
        else{
            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent setupIntent = new Intent(HomeActivity.this, AccountSetupActivity.class);
                            startActivity(setupIntent);

                        }

                        else{

                            View headerview = nv.getHeaderView(0);
                            TextView username_textview = nv.findViewById(R.id.user_name);
                            CircleImageView profileimageview = nv.findViewById(R.id.user_profile_image);

                            String name = task.getResult().getString("name");
                            String image = task.getResult().getString("image");

                            profileImageURI = Uri.parse(image);

                            username_textview.setText(name);

                            RequestOptions placeholderRequest = new RequestOptions();
                            placeholderRequest.placeholder(R.drawable.ic_profile_image);

                            Glide.with(HomeActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileimageview);

                        }

                    } else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(HomeActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();


                    }

                }
            });


        }
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_btn:
                logout();
                return true;

            case R.id.action_settings_btn:
                Intent settingsintent = new Intent(HomeActivity.this, AccountSetupActivity.class);
                startActivity(settingsintent);
                return true;
                
            default:
                return false;

        }
      
    }*/

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, eventsFragment);
        fragmentTransaction.add(R.id.fragment_container, donateFragment);
        fragmentTransaction.add(R.id.fragment_container, timelineFragment);

        fragmentTransaction.hide(donateFragment);
        fragmentTransaction.hide(timelineFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == eventsFragment){

            fragmentTransaction.hide(donateFragment);
            fragmentTransaction.hide(timelineFragment);

        }


        if(fragment == donateFragment){

            fragmentTransaction.hide(eventsFragment);
            fragmentTransaction.hide(timelineFragment);

        }
        if(fragment == timelineFragment){

            fragmentTransaction.hide(eventsFragment);
            fragmentTransaction.hide(donateFragment);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
}
