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
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import nati.financly.main_activity.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;

    private EditText et_password;
    private EditText et_email;
    private ProgressBar progressBar;


    boolean isEmailVerified;//boolean to check if user already exists.

    Button loginBtn;
    int loginBtnCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isEmailVerified = false;

        progressBar = findViewById(R.id.login_progress_bar);
        loginBtn = findViewById(R.id.login_login_btn);
        et_password = findViewById(R.id.login_password_et);
        et_email = findViewById(R.id.login_email_et);


        Button toRegisterPageBtn = findViewById(R.id.login_register_btn);

        Spannable darkerText = new SpannableString(toRegisterPageBtn.getText().toString());

        if (Locale.getDefault().getDisplayLanguage().equals("English")){
            darkerText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorHint)), 23, toRegisterPageBtn.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }else{
            Toast.makeText(getApplicationContext(),"in else.. not english",Toast.LENGTH_SHORT).show();
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
                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.INVISIBLE);
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
        } else {
            loginBtnCount += 1;
            if (loginBtnCount > 0) {
                progressBar.setVisibility(View.VISIBLE);
                view.setClickable(false);
            }
            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (isEmailVerified) {
                        if (task.isSuccessful()) {
                            //save the email in the phone so the user will use it next time.
                            SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            String email = preferences.getString("email", "");
                            if (!et_email.getText().toString().equals(email)) {
                                editor.putString("email", et_email.getText().toString());
                                Log.d("eeeeeeeeeee",email+".");
                                editor.apply();
                            }

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else if (firebaseAuth.getCurrentUser() != null) {
                                loginBtnCount = 0;
                                progressBar.setVisibility(View.INVISIBLE);
                                view.setClickable(true);
                                if (!email.equals(firebaseAuth.getCurrentUser().getEmail())) {
                                    et_email.startAnimation(animation);
                                    Toast.makeText(LoginActivity.this, "כתובת האימייל שגויה", Toast.LENGTH_SHORT).show();
                                } else {
                                    et_password.startAnimation(animation);
                                    Toast.makeText(LoginActivity.this, "הסיסמא שגויה", Toast.LENGTH_SHORT).show();
                                }
                        }

                    } else {
                        loginBtn.setClickable(true);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "בצע אימות בקישור שקיבלת לאימייל", Toast.LENGTH_SHORT).show();
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
    ////
}
