package nati.financly.main_activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import nati.financly.ItemView;
import nati.financly.R;

public class PopUpDialogAddLine extends DialogFragment implements PassDataBetweenDialogs {
    private DatePickerDialog dateDialog;
    private TimePickerDialog timeDialog;
    private DatePickerDialog.OnDateSetListener dateListener;
    private TimePickerDialog.OnTimeSetListener timeListener;

    String h = "..";
    BalanceFragmentAdapter adapter;
    DatabaseReference userRef;
    String date_time;

    //widgets
    private EditText popupMoney;
    private TextView popupDate;
    private TextView popupCategoryName;
    private TextView popupUserComment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_add_line, null, false);
        builder.setView(view);

        popupMoney = view.findViewById(R.id.popup_money);
        popupDate = view.findViewById(R.id.popup_date);
        popupCategoryName = view.findViewById(R.id.popup_category_name);
        popupUserComment = view.findViewById(R.id.popup_user_comment);
        ImageView popupDismiss = view.findViewById(R.id.popupDismiss);
        TextView popupEnter = view.findViewById(R.id.popupEnterNewLine);

        if (popupUserComment.getText().toString().equals("")) {
            popupUserComment.setVisibility(View.INVISIBLE);
        }
        popupDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //Click the add new line button - add the content to the database//
        popupEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String key = userRef.push().getKey();
                String dtStart = popupDate.getText().toString();
                String stampDate = "";
                //convert the date to another pattern
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
                try {
                    Date date = format.parse(dtStart);
                    long stamp = date.getTime();
                    stampDate = String.valueOf(stamp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                ItemView itemView = new ItemView(popupCategoryName.getText().toString(), stampDate, popupMoney.getText().toString(), popupUserComment.getText().toString());
                //write to that node
                if (!popupMoney.getText().toString().isEmpty() && !popupCategoryName.getText().toString().isEmpty()) {
                    userRef.child(key).setValue(itemView);
                    adapter.notifyDataSetChanged();
                    dismiss();
                } else {
                    final Animation animation = new TranslateAnimation(Animation.ABSOLUTE, -50, Animation.ABSOLUTE, 0);
                    animation.setDuration(80);
                    if (popupMoney.getText().toString().isEmpty()){
                        popupMoney.startAnimation(animation);
                    }
                    if (popupCategoryName.getText().toString().isEmpty()){
                        popupCategoryName.startAnimation(animation);
                    }
                }
            }
        });
        //End of popupEnter OnClick.


        //Make a new String which hold the current date and time automatically
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String currentDate = "";
        String currentTime = "";

//       //Fix later, set by phone default language and time zone .
        //show the date(month and days) always as MM and not M - ex: 05 and not 5(00/05/00).

        if (day < 10 && month < 10) {
            currentDate = "0" + day + "/" + "0" + month + "/" + year;
        } else if (day < 10) {
            currentDate = "0" + day + "/" + month + "/" + year;
        } else if (month < 10) {
            currentDate = day + "/" + "0" + month + "/" + year;
        } else {
            currentDate = day + "/" + month + "/" + year;
        }


        //Show the hour and minutes always as : hh/mm - ex: 12:12 .
        if (hour < 10 && minute < 10) {
            currentTime = "0" + hour + ":" + "0" + minute;
        } else if (hour < 10) {
            currentTime = "0" + hour + ":" + minute;
        } else if (minute < 10) {
            currentTime = hour + ":" + "0" + minute;
        } else {
            currentTime = hour + ":" + minute;
        }
        String date_and_time = currentDate + ", " + currentTime;

        popupDate.setText(date_and_time);


        //In order to show the user the edit text with "," in big numbers - like 10,000 instead of 10000,
        //I made a listener and after every user interaction, it will format the text to the desired one(with commas).
        popupMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                popupMoney.removeTextChangedListener(this);
                try {
                    String originalString = editable.toString();

                    if (originalString.contains(",")) {
                        originalString = originalString.replaceAll(",", "");
                    }
                    int textIntVal = Integer.parseInt(originalString);
                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
                    formatter.applyPattern("#,###,###,###");
                    String formattedString = formatter.format(textIntVal);
                    popupMoney.setText(formattedString);
                    popupMoney.setSelection(popupMoney.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                popupMoney.addTextChangedListener(this);
            }
        });
        //End of text Converting.


        //When press the date textView, calender dialog pops up, so the user can choose the desired date//
        popupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().hide();
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                dateDialog = new DatePickerDialog(getActivity(),
                        android.R.style.Theme_Holo_Dialog,
                        dateListener,
                        year, month, day);

                if (dateDialog.getWindow() != null) {
                    dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            getDialog().show();
                        }
                    }
                });
                dateDialog.show(); // when clicking the date TextView, the date dialog will be shown.

                timeDialog = new TimePickerDialog(getActivity(),
                        android.R.style.Theme_Holo_Dialog,
                        timeListener,
                        hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));

                if (timeDialog.getWindow() != null) {
                    timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                timeDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dateDialog.show();
                    }
                });
            }
        });

        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;

                if (day < 10 && month < 10) {
                    date_time = "0" + day + "/" + "0" + month + "/" + year;
                } else if (day < 10) {
                    date_time = "0" + day + "/" + month + "/" + year;
                } else if (month < 10) {
                    date_time = day + "/" + "0" + month + "/" + year;
                } else {
                    date_time = day + "/" + month + "/" + year;
                }
                //date_time = day + "/" + month + "/" + year;
                timeDialog.show();
            }
        };
        //End of date dialog//

        //TimePicker listener//
        timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                //showing the user the time in hh:mm format, instead of h:m - ex: if the time is 03:09 - show it that way instead of 3:9.
                if (hour < 10 && minute < 10) {
                    date_time = date_time + ", " + "0" + hour + ":" + "0" + minute;
                } else if (hour < 10) {
                    date_time = date_time + ", " + "0" + hour + ":" + minute;
                } else if (minute < 10) {
                    date_time = date_time + ", " + hour + ":" + "0" + minute;
                } else {
                    date_time = date_time + ", " + hour + ":" + minute;
                }
                popupDate.setText(date_time);
                getDialog().show();
                //dismiss();
            }
        };

        popupCategoryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = popupCategoryName.getText().toString().trim();

                Category_Comment_Dialog dialog = new Category_Comment_Dialog();
                dialog.setTargetFragment(PopUpDialogAddLine.this, 1);
                dialog.show(getFragmentManager(), "Category_Comment_Dialog");

                if (!text.equals("")) {
                    dialog.selectedSpinnerItem = text;
                } else {
                    dialog.selectedSpinnerItem = "בית";
                }

                if (popupUserComment.getVisibility() == View.VISIBLE && !popupUserComment.getText().toString().isEmpty()) {
                    dialog.commentText = popupUserComment.getText().toString();
                }
            }
        });

        ////
        return builder.create();
    }


    //TRYING TO SET ON CLICK LISTENER ON COMMENT TEXT
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (popupUserComment.getVisibility() == View.VISIBLE && !popupUserComment.getText().toString().isEmpty()){
//
//            popupUserComment.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Category_Comment_Dialog dialog = new Category_Comment_Dialog();
//
//                    dialog.setTargetFragment(PopUpDialogAddLine.this,1);
//                    dialog.show(getFragmentManager(),"Category_Comment_Dialog");
//                    dialog.commentText = popupUserComment.getText().toString();
//                }
//            });
//        }else{
//            Toast.makeText(getActivity(),"not good",Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    public void passCategoryAndComment(String category, String comment) {
        popupCategoryName.setText(category);
        popupUserComment.setText(comment);

        if (!comment.equals("")) {
            popupUserComment.setVisibility(View.VISIBLE);
        } else {
            popupUserComment.setVisibility(View.INVISIBLE);
        }
    }
}