package nati.financly;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordDialog extends AppCompatDialogFragment {

    Context context;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
        builder.setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_message);

        //creating and handling the cancel button.
        builder.setNegativeButton(R.string.cancel, null);

        //creating and handling the Ok button.
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String email = getArguments().getString("user_email");
                if (email != null) {
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, R.string.email_sent, Toast.LENGTH_LONG).show();
                                    } else {
                                        if (task.getException() != null) {
                                            try {
                                                throw task.getException();
                                            } catch (Exception e) {
                                                Toast.makeText(context, R.string.email_was_not_sent_successfully, Toast.LENGTH_LONG).show();
                                                //Log.d("$$$", "exception : " + e);
                                            }
                                        }
                                    }
                                }
                            });
                } else {
                    Toast.makeText(context, R.string.email_text_is_empty, Toast.LENGTH_LONG).show();
                }
            }
        });

        return builder.create();
    }

    public void passContext(Context context) {
        this.context = context;
    }

}
