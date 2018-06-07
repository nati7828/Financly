package nati.financly;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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

        String name = user_name.getText().toString();
        String pass = user_pass.getText().toString();
        String validatePass = user_validatePass.getText().toString();
        String email = user_email.getText().toString();

        if (name.isEmpty() || pass.isEmpty() || email.isEmpty() || validatePass.isEmpty()) {
            if (name.isEmpty()) {
                user_name.startAnimation(animation);
            }
            if (pass.isEmpty()) {
                user_pass.startAnimation(animation);
            }
            if (validatePass.isEmpty()) {
                user_validatePass.startAnimation(animation);
            }
            if (email.isEmpty()) {
                user_email.startAnimation(animation);
            }
        } else if (!validatePass.equals(pass)) {
            user_validatePass.startAnimation(animation);
            //user_validatePass.startAnimation(animation);
            Toast.makeText(this, "הסיסמא לא זהה", Toast.LENGTH_SHORT).show();
        } else {
            //Create user with email and password
            firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.VISIBLE);
                        firebaseUser = firebaseAuth.getCurrentUser();//get current user.
                        //Check if the user is created successfully, and send him email verification.
                        if (firebaseUser != null) {
                            user.setName(user_name.getText().toString());//input of the user.
                            user.setEmail(user_email.getText().toString());//another input of the user.

                            //itemView.setName(user_name.getText().toString());//input of the user.
                            //itemView.setEmail(user_email.getText().toString());//another input of the user.
                            String userId = firebaseUser.getUid();//get the userId from firebase.
                            DBUser.child(userId).setValue(user); // set each user with his own details.

                            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "היכנס לאימייל שלך על מנת לבצע אימות", Toast.LENGTH_SHORT).show();
                                        firebaseAuth.signOut();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "נדרש מייל אימות,אנא נסה שנית", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "ישנה תקלה, נסה שנית", Toast.LENGTH_SHORT).show();
                        }

                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "יצירת המשתמש לא בוצעה בהצלחה,אנא נסה שנית", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    ////
    //sign_in(btn) function//
    public void AlreadyHaveUser(View view) {
        finish();
    }
    ////
}
