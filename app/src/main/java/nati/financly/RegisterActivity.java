package nati.financly;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

import nati.financly.main_activity.ItemView;

public class RegisterActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference DBUser = database.getReference("Users");
    FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;

    private EditText user_name;
    private EditText user_pass;
    private EditText user_validatePass;
    private EditText user_email;
    private ProgressBar progressBar;
    private Button signUp;

    ItemView itemView;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registar);

        user_name = findViewById(R.id.register_name_et);
        user_pass = findViewById(R.id.register_pass_et);
        user_validatePass = findViewById(R.id.register_pass_validate_et);
        user_email = findViewById(R.id.register_email_et);

        signUp = findViewById(R.id.signin_register_btn);
        Spannable darkerText = new SpannableString(signUp.getText().toString());
        if (Locale.getDefault().getDisplayLanguage().equals("English")) {
            darkerText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorHint)), 21, signUp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        } else {
            darkerText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorHint)), 17, signUp.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        signUp.setText(darkerText);

        progressBar = findViewById(R.id.register_progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();
        itemView = new ItemView();
        user = new User();
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.INVISIBLE);
    }

    //sign_Up(btn) function//
    public void SignUp(View view) {
        Animation animation = new TranslateAnimation(Animation.ABSOLUTE, -50, Animation.ABSOLUTE, 0);
        animation.setDuration(100);

        final String nameTxt = user_name.getText().toString();
        final String passTxt = user_pass.getText().toString();
        final String validatePassTxt = user_validatePass.getText().toString();
        final String emailTxt = user_email.getText().toString();

        if (nameTxt.isEmpty() || passTxt.isEmpty() || emailTxt.isEmpty() || validatePassTxt.isEmpty()) {
            if (nameTxt.isEmpty()) {
                user_name.startAnimation(animation);
            }
            if (passTxt.isEmpty()) {
                user_pass.startAnimation(animation);
            }
            if (validatePassTxt.isEmpty()) {
                user_validatePass.startAnimation(animation);
            }
            if (emailTxt.isEmpty()) {
                user_email.startAnimation(animation);
            }
        } else if (passTxt.length() < 6) {
            user_pass.startAnimation(animation);
            Toast.makeText(this, R.string.register_pass_alert, Toast.LENGTH_SHORT).show();
        } else if (!validatePassTxt.equals(passTxt)) {
            user_validatePass.startAnimation(animation);
            //user_validatePass.startAnimation(animation);
            Toast.makeText(this, R.string.password_not_equal, Toast.LENGTH_SHORT).show();
        } else {
            //Create user with email and password
            firebaseAuth.createUserWithEmailAndPassword(emailTxt, passTxt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.VISIBLE);
                        firebaseUser = firebaseAuth.getCurrentUser();//get current user.

                        user.setName(nameTxt);//input of the user.
                        user.setEmail(emailTxt);//another input of the user.
                        user.setMoney("0");

                        String userId = firebaseUser.getUid();//get the userId from firebase.
                        DBUser.child(userId).setValue(user); // set each user with his own details.

                        //Check if the user is created successfully, and send him email verification.
                        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, R.string.enter_email, Toast.LENGTH_LONG).show();
                                    firebaseAuth.signOut();
                                } else {
                                    Toast.makeText(RegisterActivity.this, R.string.email_verification, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        finish();
                    } else {
                        if (task.getException() != null) {
                            if (Locale.getDefault().getDisplayLanguage().equals("English")) {
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "כתובת האימייל הזאת כבר נמצאת בשימוש על ידי חשבון אחר", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            });
        }
    }

    //end of sign up function(via button)//
    //sign_in(btn) function//
    public void AlreadyHaveUser(View view) {
        finish();
    }
    ////
}
