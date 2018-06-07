package nati.financly.main_activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    BalanceMainFragment balanceMainFragment;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        balanceMainFragment = new BalanceMainFragment();
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        Toast.makeText(getActivity(),"check" + year + "/" + month + "/" + day,Toast.LENGTH_SHORT).show();
        String date = year + "/" + month + "/" + day;
        Bundle args = new Bundle();
        args.putString("date", date);
        balanceMainFragment.setArguments(args);
    }
}