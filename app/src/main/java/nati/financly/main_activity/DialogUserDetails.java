package nati.financly.main_activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import nati.financly.R;

public class DialogUserDetails extends AppCompatDialogFragment {

    String name = "";

    EditText nameET;

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

        nameET = view.findViewById(R.id.user_details_dialog_nameET);

        TextView cancelBtn = view.findViewById(R.id.user_details_dialog_cancelBtn);
        TextView okBtn = view.findViewById(R.id.user_details_dialog_okBtn);

        //Show the imported strings on the edit texts.
        nameET.setText(name);

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
                        updateName();
                    }

                    getDialog().dismiss();
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserDetailsFragment()).commit();

                }
            });

        builder.setView(view);
        return builder.create();
    }


    public void updateName(){
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());

        Intent intent = new Intent("user_name");
        intent.putExtra("name",nameET.getText().toString());
        manager.sendBroadcast(intent);
    }

    //Get name and email from the userDetailsFragment to show them in this dialog editText's.
    public void getNameFromFragment(String name) {
        this.name = name;
    }


}
