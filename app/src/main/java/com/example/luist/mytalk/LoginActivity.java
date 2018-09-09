package com.example.luist.mytalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private View loginPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        loginPanel = findViewById(R.id.login_panel);

        // Init FirebaseAuth
        initFirebaseAuth();

        // Init FirebaseAuthStateListener
        initFirebaseAuthStateListener();

    }

    /**
     * Firebase Auth
     */
    private FirebaseAuth mAuth;

    private EditText emailInput;
    private EditText passwordInput;

    private void initFirebaseAuth() {
        // initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.email_input);
        passwordInput = (EditText) findViewById(R.id.password_input);
    }

    public void callLogin(View view) {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Debes completar estos campos...", Toast.LENGTH_SHORT).show();
            return;
        }

        loginPanel.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Sign In user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginPanel.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Nombre de usuario y / o contraseña inválidos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void callRegister(View view) {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese su Email y Password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginPanel.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            loginPanel.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Register failed", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            User user = new User();
                            user.setUid(currentUser.getUid());
                            user.setDisplayName("Nuevo Usuario");
                            user.setEmail(currentUser.getEmail());

                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            usersRef.child(user.getUid()).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onSuccess");
                                            } else {
                                                Log.e(TAG, "onFailure", task.getException());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * Firebase AuthStateListener
     */
    private FirebaseAuth.AuthStateListener mAuthListener;

    private void initFirebaseAuthStateListener() {
        // and the AuthStateListener method so you can track whenever the user signs in or out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Toast.makeText(LoginActivity.this, "Bienvenido" + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                    // Go MainActivity
                    startActivity(new Intent(LoginActivity.this, ListaActivity.class));
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public  void onStop()
    {
        super.onStop();
        if(mAuthListener!=null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}