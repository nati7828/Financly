package nati.financly.main_activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import nati.financly.R;

public class DialogLogout extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth);
        builder.setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message);

        //creating and handling the cancel button.
        builder.setNegativeButton(R.string.dialog_logout_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });

        //creating and handling the Ok button.
        builder.setPositiveButton(R.string.dialog_logout_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                getActivity().finish();
            }
        });
        return builder.create();
    }
}