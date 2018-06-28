package nati.financly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import nati.financly.main_activity.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth.AuthStateListener authStateListenerForUi;

    private EditText et_password;
    private EditText et_email;
    private ProgressBar progressBar;

    boolean isEmailVerified;//boolean to check if user already exists.

    Button loginBtn, verifyEmailAgainBtn;
    int loginBtnCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isEmailVerified = false;

        progressBar = findViewById(R.id.login_progress_bar);
        loginBtn = findViewById(R.id.login_login_btn);
        verifyEmailAgainBtn = findViewById(R.id.login_resend_email);
        et_password = findViewById(R.id.login_password_et);
        et_email = findViewById(R.id.login_email_et);

        Button toRegisterPageBtn = findViewById(R.id.login_register_btn);

        Spannable darkerText = new SpannableString(toRegisterPageBtn.getText().toString());

        //check language, adapt the spanned text with text length, if it's english - span other than in hebrew.
        if (Locale.getDefault().getDisplayLanguage().equals("English")) {
            darkerText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorHint)), 23, toRegisterPageBtn.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            darkerText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorHint)), 20, toRegisterPageBtn.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        toRegisterPageBtn.setText(darkerText);

        //Save the email on the phone, and show it on start.
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = preferences.getString("email", "");
        et_email.setText(email);
        if (!et_email.getText().toString().isEmpty()) {
            et_password.requestFocus();

        }
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    isEmailVerified = user.isEmailVerified();
                    if (!isEmailVerified) {
                        verifyEmailAgainBtn.setVisibility(View.VISIBLE);
                    } else {
                        verifyEmailAgainBtn.setVisibility(View.GONE);
                    }
                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.GONE);
        firebaseAuth.addAuthStateListener(authStateListener);
        loginBtnCount = 0;
        loginBtn.setClickable(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    //Forgot password(btn) function//
    public void ResetPassword(View view) {
        if (!et_email.getText().toString().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("user_email", et_email.getText().toString().trim());
            // set ResetPasswordDialog Arguments
            ResetPasswordDialog dialog = new ResetPasswordDialog();
            dialog.setArguments(bundle);
            dialog.passContext(this);
            dialog.show(getSupportFragmentManager(), "dialog");
        } else {
            final Animation animation = new TranslateAnimation(Animation.ABSOLUTE, -50, Animation.ABSOLUTE, 0);
            animation.setDuration(80);
            et_email.startAnimation(animation);
            Toast.makeText(this, R.string.email_text_is_empty, Toast.LENGTH_LONG).show();
        }
    }
    ////

    //login(btn) function//
    public void Login(final View view) {
        final String email = et_email.getText().toString();
        final String pass = et_password.getText().toString();

        final Animation animation = new TranslateAnimation(Animation.ABSOLUTE, -50, Animation.ABSOLUTE, 0);
        animation.setDuration(100);
        //login checks
        if (pass.isEmpty() || email.isEmpty()) {
            if (pass.isEmpty()) {
                et_password.startAnimation(animation);
            }
            if (email.isEmpty()) {
                et_email.startAnimation(animation);
            }
        } else {//If password and email are not empty
            loginBtnCount += 1;
            progressBar.setVisibility(View.VISIBLE);
            view.setClickable(false);

            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    verifyEmailAgainBtn.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        if (user != null) {
                            isEmailVerified = user.isEmailVerified();
                        }

                        //If user signed in successfully
                        user = firebaseAuth.getCurrentUser();

                        if (!isEmailVerified) {// check is user
                            verifyEmailAgainBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            loginBtn.setClickable(true);
                            Toast.makeText(getApplicationContext(), R.string.email_not_verified, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //save the email in the phone so the user will use it next time.
                        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        String sharedEmail = preferences.getString("email", "");
                        if (!email.equals(sharedEmail)) {
                            editor.putString("email", et_email.getText().toString());
                            editor.apply();
                        }
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    // If user was not signed in successfully - show the reason.
                     else {
                        try {
                            if (task.getException() != null) {
                                throw task.getException();
                            }
                        }
                        //If user enters wrong email.
                        catch (FirebaseAuthInvalidUserException invalidEmail) {
                            et_email.startAnimation(animation);
                            Toast.makeText(LoginActivity.this, R.string.email_is_wrong, Toast.LENGTH_SHORT).show();
                        }
                        //If user enters wrong password.
                        catch (FirebaseAuthInvalidCredentialsException wrongPassword) {
                            et_password.startAnimation(animation);
                            Toast.makeText(LoginActivity.this, R.string.password_is_wrong, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.d("*onCompleate*", "onComplete: " + e.getMessage());
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        loginBtnCount = 0;
                        loginBtn.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
    ////login finished////

    //Register(btn) function//
    public void Register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    //resend email verification again(if user didn't open it on time)//
    public void ResendEmail(View view) {
        if (user != null && !isEmailVerified) {
            user.sendEmailVerification();
            Toast.makeText(this, R.string.confirmation_email_was_sent, Toast.LENGTH_SHORT).show();
        }
    }

}
