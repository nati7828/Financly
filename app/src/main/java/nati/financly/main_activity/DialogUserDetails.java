package nati.financly.main_activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import nati.financly.R;

public class DialogUserDetails extends AppCompatDialogFragment {

    String name = "";
    String email = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_user_details, null, false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_MinWidth);


        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = mFirebaseDatabase.getReference();


        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }

        final DatabaseReference userRef = myRef.child("Users").child(userId);

        final EditText nameET = view.findViewById(R.id.user_details_dialog_nameET);
        final EditText emailET = view.findViewById(R.id.user_details_dialog_emailET);

        TextView cancelBtn = view.findViewById(R.id.user_details_dialog_cancelBtn);
        TextView okBtn = view.findViewById(R.id.user_details_dialog_okBtn);

        //Show the imported strings on the edit texts.
        nameET.setText(name);
        emailET.setText(email);


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        if (user != null)
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String name_text = nameET.getText().toString();
                    if (!name_text.equals(name)) {
                        userRef.child("name").setValue(name_text);
                    }

                    final String email_text = emailET.getText().toString();
                    if(!email_text.equals(email)){
                        try {
                            user.updateEmail(email_text);
                            userRef.child("email").setValue(email_text);
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mAuth.signOut();
                                        getActivity().finish();
                                        Toast.makeText(getActivity(), R.string.enter_email, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity(), R.string.email_verification, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }catch (Exception e){
                            e.getMessage();
                        }
                    }

                    getDialog().dismiss();
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserDetailsFragment()).commit();

                }
            });

        builder.setView(view);
        return builder.create();
    }

    //Get name and email from the userDetailsFragment to show them in this dialog editText's.
    public void getNameAndEmailFromFragment(String name, String email) {
        this.name = name;
        this.email = email;
    }


}
