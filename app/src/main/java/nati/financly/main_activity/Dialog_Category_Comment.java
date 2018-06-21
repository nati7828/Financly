package nati.financly.main_activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import nati.financly.R;

public class Dialog_Category_Comment extends AppCompatDialogFragment {

    EditText comment;
    Spinner spinner;
    TextView digitsCounter;
    SpinnerAdapter adapter;
    PassDataBetweenDialogs passDataBetweenDialogsListener;
    ArrayList<SpinnerItem> categoriesList;
    String category;
    int index = 0;

    String selectedSpinnerItem = "";
    String commentText = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //final BalanceMainFragment balanceMainFragment = new BalanceMainFragment();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_AppCompat_Light_Dialog_MinWidth);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_category_comment, null, false);

        spinner = view.findViewById(R.id.comment_dialog_spinner);
        comment = view.findViewById(R.id.comment_dialog__comment_et);
        digitsCounter = view.findViewById(R.id.comment_dialog_counter_tv);
        TextView cancelBtn = view.findViewById(R.id.comment_dialog_cancelBtn);
        TextView okBtn = view.findViewById(R.id.comment_dialog_okBtn);

        //adding digits counter visible to the user(max length = 20 digits)//
        comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                String counterText = comment.length() + "/20";
                digitsCounter.setText(counterText);
                digitsCounter.setTextColor(getResources().getColor(R.color.colorBlue));

                if (comment.length() == 20) {
                    digitsCounter.setTextColor(getResources().getColor(R.color.colorRed));
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }
        });
        ////

        categoriesList = new ArrayList<>();
        categoriesList.add(new SpinnerItem(getString(R.string.בית), R.drawable.home));
        categoriesList.add(new SpinnerItem(getString(R.string.אוכל), R.drawable.food));
        categoriesList.add(new SpinnerItem(getString(R.string.קניות), R.drawable.shopping_cart));
        categoriesList.add(new SpinnerItem(getString(R.string.ביגוד), R.drawable.clothes));
        categoriesList.add(new SpinnerItem(getString(R.string.שכירות), R.drawable.rent));
        categoriesList.add(new SpinnerItem(getString(R.string.משכנתא), R.drawable.mortgage));
        categoriesList.add(new SpinnerItem(getString(R.string.חשבונות), R.drawable.bills));
        categoriesList.add(new SpinnerItem(getString(R.string.משכורת), R.drawable.payment));
        categoriesList.add(new SpinnerItem(getString(R.string.תשלום), R.drawable.payment));
        categoriesList.add(new SpinnerItem(getString(R.string.הלוואות), R.drawable.loan));
        categoriesList.add(new SpinnerItem(getString(R.string.חיסכון), R.drawable.savings));
        categoriesList.add(new SpinnerItem(getString(R.string.טואלטיקה), R.drawable.toliet_and_clean));
        categoriesList.add(new SpinnerItem(getString(R.string.רכב), R.drawable.car));
        categoriesList.add(new SpinnerItem(getString(R.string.Public_transport), R.drawable.taxi));
        categoriesList.add(new SpinnerItem(getString(R.string.ביטוחים), R.drawable.insurance));
        categoriesList.add(new SpinnerItem(getString(R.string.תקשורת), R.drawable.phone));
        categoriesList.add(new SpinnerItem(getString(R.string.חדר_כושר), R.drawable.gym));
        categoriesList.add(new SpinnerItem(getString(R.string.לימודים), R.drawable.study));
        categoriesList.add(new SpinnerItem(getString(R.string.בתי_ספר), R.drawable.school));
        categoriesList.add(new SpinnerItem(getString(R.string.גני_ילדים), R.drawable.kindergarden));
        categoriesList.add(new SpinnerItem(getString(R.string.בילויים), R.drawable.hangout));
        categoriesList.add(new SpinnerItem(getString(R.string.מסעדות), R.drawable.restaurent));
        categoriesList.add(new SpinnerItem(getString(R.string.חיות), R.drawable.pet));
        categoriesList.add(new SpinnerItem(getString(R.string.מתנות), R.drawable.gift));
        categoriesList.add(new SpinnerItem(getString(R.string.אחר), R.drawable.other));

        adapter = new SpinnerAdapter(getContext(), categoriesList);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                SpinnerItem clickedItem = (SpinnerItem) parent.getItemAtPosition(pos);
                category = clickedItem.getCategory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Set the string to the method that send the appropriate item.
        //the string is initialized from the
        setSpinText(selectedSpinnerItem);
        comment.setText(commentText);

        //Change the spinner arrow color.
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentText = comment.getText().toString().trim();
                passDataBetweenDialogsListener.passCategoryAndComment(category, commentText);
                Log.d("##",getParentFragment()+"..");
                getDialog().dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }
    //End of onCreateDialog//

    //In case the user chose category, pressed ok, and later he came back to this dialog,
    //The function get the string(selected spinner item) from the last dialog and set the spinner item as that string.
    public void setSpinText(String categoryText) {
        SpinnerItem spinnerItem;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            spinnerItem = (SpinnerItem) spinner.getAdapter().getItem(i);
            if (spinnerItem.getCategory().contains(categoryText)) {
                spinner.setSelection(i);
            }
        }
    }

    public void updateSelectedSpinnerItemValue(String newValue){
        selectedSpinnerItem = newValue;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //Passing the category and comment - the interface connects between the fragments(and the activity).
            passDataBetweenDialogsListener = (PassDataBetweenDialogs) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e("**onAttach**", "onAttach: ClassCastException : " + e.getMessage());
        }
    }

    //Method to get the text from dialog_add_line.
    public void updateCommentText(String comment_text) {
        commentText = comment_text;
    }
}
