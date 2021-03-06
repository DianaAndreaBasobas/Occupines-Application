package com.example.application;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";
    private FirebaseAuth mAuth;
    private Utility utils;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        utils = new Utility();

        utils.removeBlinkOnTransition(LoginActivity.this);

        buttonEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void buttonEvents() {
        Button signIn = findViewById(R.id.signIn);
        Button signUp = findViewById(R.id.signUp);

        signIn.setOnClickListener(v -> {
            utils.toggleButton(signIn);
            utils.toggleButton(signUp);

            String email = ((EditText) findViewById(R.id.email)).getText().toString();
            String password = ((EditText) findViewById(R.id.password)).getText().toString();

            if (utils.checkInputs(email, password)) {
                signIn(email, password);
            } else {
                utils.showToast(LoginActivity.this, "Some fields are empty.");
            }

            utils.toggleButton(signIn);
            utils.toggleButton(signUp);
        });

        signUp.setOnClickListener(v -> {
            utils.toggleButton(signUp);

            //Shared element transition
            ImageView imageView = findViewById(R.id.icon);
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(LoginActivity.this, imageView, "icon");
            startActivity(intent, options.toBundle());

            utils.toggleButton(signUp);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        utils.showToast(LoginActivity.this, "Login success.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            utils.showToast(LoginActivity.this, "Authentication failed: User not found.");
                        } catch (Exception e) {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            utils.showToast(LoginActivity.this, "Authentication failed.");
                        }
                        updateUI(null);
                    }
                });
    }
}