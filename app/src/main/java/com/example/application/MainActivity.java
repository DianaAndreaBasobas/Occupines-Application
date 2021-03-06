package com.example.application;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;

import com.example.application.databinding.ActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static File localFile;
    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Connect to Firebase
        mAuth = FirebaseAuth.getInstance();

        //Initialize fragments
        FirstFragment firstFragment = new FirstFragment();
        SecondFragment secondFragment = new SecondFragment();
        ThirdFragment thirdFragment = new ThirdFragment();
        FourthFragment fourthFragment = new FourthFragment();
        FifthFragment fifthFragment = new FifthFragment();

        //Set first fragment on load
        downloadImage(firstFragment);

        //Add badge on notification
        BottomNavigationView bottomNav = binding.bottomNavigationView;
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.notifications);

        //Number of notifications
        MutableLiveData<Integer> number = new MutableLiveData<>();
        //Initialize with a value
        number.setValue(fifthFragment.getNotificationCount());
        //Listener for variable
        number.observe(MainActivity.this, integer -> {
            destroyBadge(badge);
            //noinspection ConstantConditions
            setupBadge(badge, number.getValue());
        });

        //Show each fragment on each menu item click
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            getSupportFragmentManager().popBackStack();

            if (itemId == R.id.home) {
                //1st page
                setCurrentFragment(firstFragment);
                return true;
            } else if (itemId == R.id.likes) {
                //2nd page
                addNotification(fifthFragment, "Testing", number);
                setCurrentFragment(secondFragment);
                return true;
            } else if (itemId == R.id.messages) {
                //3rd page
                setCurrentFragment(thirdFragment);
                return true;
            } else if (itemId == R.id.calendar) {
                //4th page
                setCurrentFragment(fourthFragment);
                return true;
            } else if (itemId == R.id.notifications) {
                //5th page
                setCurrentFragment(fifthFragment);
                //Remove badge
                destroyBadge(badge);
                return true;
            }
            return false;
        });
    }

    private void addNotification(FifthFragment fifthFragment, String message, MutableLiveData<Integer> number) {
        fifthFragment.addNotification(message);
        number.postValue(fifthFragment.getAdded());
    }

    private void downloadImage(Fragment firstFragment) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storageRef.child("images").child(Objects.requireNonNull(mAuth.getUid())).child("profile");
        try {
            localFile = File.createTempFile("profile", "jpg");
            pathReference.getFile(localFile).addOnCompleteListener(v ->
                    getSupportFragmentManager().beginTransaction().
                            add(R.id.flFragment, firstFragment).
                            commitAllowingStateLoss());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.flFragment, fragment).
                commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        //If back button is pressed and we are not at home page
        //Then go back 1 page
        //Else ask if going to sign out
        if (fm.getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            signOut();
        }
    }

    private void signOut() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    mAuth.signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Sign out?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void setupBadge(BadgeDrawable badge, int number) {
        if (badge != null) {
            if (number != 0) {
                // An icon only badge will be displayed unless a number is set:
                badge.setBackgroundColor(getResources().getColor(R.color.badge_color));
                badge.setBadgeTextColor(Color.WHITE);
                badge.setBadgeGravity(BadgeDrawable.TOP_END);
                badge.setMaxCharacterCount(3);
                badge.setNumber(number);
                badge.setVisible(true);
            } else {
                destroyBadge(badge);
            }
        }
    }

    private void destroyBadge(BadgeDrawable badge) {
        badge.clearNumber();
        badge.setVisible(false);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

}
