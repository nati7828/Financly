package nati.financly.main_activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
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

import nati.financly.R;

public class DialogAddLine extends DialogFragment implements PassDataBetweenDialogs, TextWatcher {
    private DatePickerDialog dateDialog;
    private TimePickerDialog timeDialog;
    private DatePickerDialog.OnDateSetListener dateListener;
    private TimePickerDialog.OnTimeSetListener timeListener;

    BalanceFragmentAdapter adapter;
    DatabaseReference userRef;
    String date_time;

    //widgets
    private EditText popupMoney;
    private TextView popupDate;
    private TextView popupCategoryName;
    private TextView popupUserComment;

    ItemView itemView;

    boolean isEditing = false;
    String category, comment, date, money;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
        if (isAdded()) {
            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_add_line, null, false);
            builder.setView(view);

            popupMoney = view.findViewById(R.id.popup_money);
            popupDate = view.findViewById(R.id.popup_date);
            popupCategoryName = view.findViewById(R.id.popup_category_name);
            popupUserComment = view.findViewById(R.id.popup_user_comment);
            TextView popupEnter = view.findViewById(R.id.popupEnterNewLine);
            ImageView popupDismiss = view.findViewById(R.id.popupDismiss);

            if (popupUserComment.getText().toString().equals("")) {
                popupUserComment.setVisibility(View.INVISIBLE);
            }

            if (isEditing) {
                popupCategoryName.setText(category);
                if (!comment.isEmpty()) {
                    popupUserComment.setVisibility(View.VISIBLE);
                    popupUserComment.setText(comment);
                } else {
                    popupUserComment.setVisibility(View.INVISIBLE);
                }

                Log.d("###", "money: " + money + ", date:" + date);

                popupDate.setText(date);

                String moneyInt;
                if (money.contains(",")){
                    moneyInt = money.replace(",","");
                }else{
                    moneyInt = money;
                }
                int textIntVal = Integer.parseInt(moneyInt);
                DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
                formatter.applyPattern("#,###,###,###");
                String formattedString = formatter.format(textIntVal);
                popupMoney.setText(formattedString);

                popupEnter.setText(R.string.change);
            }

            //Click the 'X' button to exit_dialog the dialog.
            popupDismiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isEditing = false;
                    dismiss();
                }
            });

            //Click the add new line button - add the content to the database//
            popupEnter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String dtStart = popupDate.getText().toString();

                    SimpleDateFormat format;
                    format = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());

//                    if (!DateFormat.is24HourFormat(getActivity())) {//if 12 hours format show am/pm
//                        format = new SimpleDateFormat("MM/dd/yyyy, KK:mm aa", Locale.getDefault());
//                        if (date != null) {
//                            String am_pm = date.substring(date.length() - 2, date.length());
//                            int hour = Integer.parseInt(date.substring(12, 14));
//                            int minute = Integer.parseInt(date.substring(15, 17));
//
//                        Log.d("###", "" + hour + ":" + minute + ", " + am_pm);
//
//
//                    }
//                    else {
//                        format = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
//                    }

                    String stampDate = "";
                    //Convert the date to another pattern to send to the fire base database.
                    try {
                        Date date = format.parse(dtStart);
                        long stamp = date.getTime();
                        stampDate = String.valueOf(stamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //If itemView has values, user is editing.
                    if (isEditing) {
                        if (!popupMoney.getText().toString().isEmpty()) {
                            itemView.setIncome_outcome(popupMoney.getText().toString());
                        }

                        itemView.setCategoryName(popupCategoryName.getText().toString());

                        if (!popupUserComment.getText().toString().trim().isEmpty()) {
                            itemView.setUserComment(popupUserComment.getText().toString());
                        }
                        itemView.setDate(stampDate);
                        userRef.child(itemView.getKey()).setValue(itemView);
                    } else {
                        itemView = new ItemView(popupCategoryName.getText().toString(), stampDate, popupMoney.getText().toString(), popupUserComment.getText().toString());
                    }
                    if (!popupMoney.getText().toString().isEmpty() && !popupCategoryName.getText().toString().isEmpty()) {
                        if (!isEditing) {
                            //Toast.makeText(getActivity(),"not editing..",Toast.LENGTH_SHORT).show();
                            String key = userRef.push().getKey();
                            itemView.setKey(key);
                            userRef.child(key).setValue(itemView);
                        }
                        isEditing = false;
                        adapter.notifyDataSetChanged();
                        dismiss();
                    } else {
                        final Animation animation = new TranslateAnimation(Animation.ABSOLUTE, -50, Animation.ABSOLUTE, 0);
                        animation.setDuration(80);
                        if (popupMoney.getText().toString().isEmpty()) {
                            popupMoney.startAnimation(animation);
                        }
                        if (popupCategoryName.getText().toString().isEmpty()) {
                            popupCategoryName.startAnimation(animation);
                        }
                    }


                }
            });
            //End of popupEnter OnClick.


            //Make a new String which hold the current date and time automatically
            final Calendar cal = Calendar.getInstance();
            final int year = cal.get(Calendar.YEAR);
            final int month = cal.get(Calendar.MONTH) + 1;
            final int day = cal.get(Calendar.DAY_OF_MONTH);

            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            String currentDate = "";
            String currentTime = "";

//            if (!DateFormat.is24HourFormat(getActivity())) {
//                // If the time format is 12 hours show mm//dd/yyyy.
//
//                if (day < 10 && month < 10) {
//                    currentDate = "0" + month + "/" + "0" + day + "/" + year;
//                } else if (day < 10) {
//                    currentDate = month + "/" + "0" + day + "/" + year;
//                } else if (month < 10) {
//                    currentDate = "0" + month + "/" + day + "/" + year;
//                } else {
//                    currentDate = month + "/" + day + "/" + year;
//                }
//
//                //Show the hour and minutes always as : hh/mm - ex: 12:12 .
//                if (hour < 10 && minute < 10) {
//                    currentTime = "0" + hour + ":" + "0" + minute;
//                } else if (hour < 10) {
//                    currentTime = "0" + hour + ":" + minute;
//                } else if (minute < 10) {
//                    currentTime = hour + ":" + "0" + minute;
//                } else {
//                    currentTime = hour + ":" + minute;
//                }

//            } else { // If not 12 hours format show dd/mm/yyyy.
                //Show the date(month and days) always as MM and not M - ex: 05 and not 5(00/05/00).
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
//            }

            String date_and_time = currentDate + ", " + currentTime;

            if (isEditing) {
                popupDate.setText(itemView.getDate());
            } else {
                popupDate.setText(date_and_time);
            }

            //In order to show the user the edit text with "," in big numbers - like 10,000 instead of 10000,
            //I made a listener and after every user interaction, it will format the text to the desired one(with commas).
            popupMoney.addTextChangedListener(this);
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

                    if (isEditing) {
                        String date = popupDate.getText().toString();
                        Log.d("###date: ", date + "...");
                        day = Integer.parseInt(date.substring(0, 2));
                        month = Integer.parseInt(date.substring(3, 5));
                        year = Integer.parseInt(date.substring(6, 10));

                        hour = Integer.parseInt(date.substring(12, 14));
                        minute = Integer.parseInt(date.substring(15, 17));
                        Log.d("#minute", hour + ":" + minute);
                    }

                    dateDialog = new DatePickerDialog(getActivity(),
                            R.style.date_dialog,
                            dateListener,
                            year, month, day);

                    if (dateDialog.getWindow() != null) {
                        dateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGray)));
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
                            R.style.time_dialog,
                            timeListener,
                            hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));


                    if (timeDialog.getWindow() != null) {
                        timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGray)));
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

                    if (day < 10 && month < 10) {
                        date_time = "0" + day + "/" + "0" + month + "/" + year;
                    } else if (day < 10) {
                        date_time = "0" + day + "/" + month + "/" + year;
                    } else if (month < 10) {
                        date_time = day + "/" + "0" + month + "/" + year;
                    } else {
                        date_time = day + "/" + month + "/" + year;
                    }
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


                    if (!DateFormat.is24HourFormat(getActivity())) {
                        if (hour >= 12) {
                            date_time = date_time + "PM";
                        } else {
                            date_time = date_time + "AM";
                        }
                    }

                    popupDate.setText(date_time);
                    getDialog().show();

                }
            };

            popupCategoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = popupCategoryName.getText().toString().trim();

                    Dialog_Category_Comment dialog = new Dialog_Category_Comment();
                    dialog.setTargetFragment(DialogAddLine.this, 1);
                    dialog.show(getFragmentManager(), "Dialog_Category_Comment");

                    if (!text.equals("")) {
                        //dialog.selectedSpinnerItem = text;
                        dialog.updateSelectedSpinnerItemValue(text);
                    } else {
                        dialog.updateSelectedSpinnerItemValue("בית");
                        //dialog.selectedSpinnerItem = "בית";
                    }
                    //if comment is not empty,
                    if (popupUserComment.getVisibility() == View.VISIBLE && !popupUserComment.getText().toString().isEmpty()) {
                        dialog.commentText = popupUserComment.getText().toString();
                    }
                }
            });
            ////


        }
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
//                    Dialog_Category_Comment dialog = new Dialog_Category_Comment();
//
//                    dialog.setTargetFragment(DialogAddLine.this,1);
//                    dialog.show(getFragmentManager(),"Dialog_Category_Comment");
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

    public void passDate(){

    }

    public void setItemViewForEditing(ItemView itemView) {
        String category = itemView.getCategoryName();
        String comment = itemView.getUserComment();
        String date = itemView.getDate();
        String money = itemView.getIncome_outcome();

        Log.d("###", category + comment + date + money);
        isEditing = true;

        this.itemView = itemView;
        this.category = category;
        this.comment = comment;
        this.date = date;
        this.money = money;
    }

    //get the adapter and the user reference from balance fragment to use them in this dialog.
    public void setUserRefAndAdapter(DatabaseReference userRef, BalanceFragmentAdapter adapter) {
        this.userRef = userRef;
        this.adapter = adapter;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.d("###", editable + "...");
        popupMoney.removeTextChangedListener(this);
        try {
            String originalString = editable.toString();

            Log.d("###original", originalString + " ...");
            if (originalString.contains(",")) {
                originalString = originalString.replaceAll(",", "");
            }
            Log.d("###original", originalString + " 2...");

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

    ////NumbersTextChanges////

    ////End of NumbersTextChanges////


}
