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

    String stampDate = "";
    Calendar cal = Calendar.getInstance();
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH) + 1;
    int day = cal.get(Calendar.DAY_OF_MONTH);

    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int minute = cal.get(Calendar.MINUTE);

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

                popupDate.setText(date);

                String moneyInt;
                if (money.contains(",")) {
                    moneyInt = money.replace(",", "");
                } else {
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


                    formatDate(dtStart);

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
                            //if user is creating a new item -> create a new node in the firebase DB.
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

            if (isEditing) {
                String itemViewDate = itemView.getDate();
                popupDate.setText(itemViewDate);
            } else {
                updateDate(day, month-1, year);
                updateTime(hour, minute);
            }

            //In order to show the user the edit text with "," in big numbers - like 10,000 instead of 10000,
            //I made a listener and after every user interaction, it will format the text to the desired one(with commas).
            popupMoney.addTextChangedListener(this);

            //When press the date textView, calender dialog pops up, so the user can choose the desired date//
            popupDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getDialog().hide();
                    if (isEditing) {
                        String date = popupDate.getText().toString();
                        year = Integer.parseInt(date.substring(6, 10));
                        hour = Integer.parseInt(date.substring(12, 14));
                        minute = Integer.parseInt(date.substring(15, 17));

                        if (!DateFormat.is24HourFormat(getActivity())) {
                            //If date format is 12 hours.
                            day = Integer.parseInt(date.substring(3, 5));
                            month = Integer.parseInt(date.substring(0, 2));
                            if (date.toLowerCase().contains("PM".toLowerCase())) {
                                hour += 12;
                            }
                        } else {
                            //If date format is 24 hours.
                            day = Integer.parseInt(date.substring(0, 2));
                            month = Integer.parseInt(date.substring(3, 5));
                        }
                    }

                    ////Date dialog////
                    dateDialog = new DatePickerDialog(getActivity(),
                            R.style.date_dialog,
                            dateListener,
                            year, month-1, day);

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
                    ////End of date dialog////

                    ////Time dialog////
                    timeDialog = new TimePickerDialog(getActivity(),
                            R.style.time_dialog,
                            timeListener,
                            hour, minute, DateFormat.is24HourFormat(getActivity()));


                    if (timeDialog.getWindow() != null) {
                        timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorDarkGray)));
                    }


                    timeDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dateDialog.show();
                        }
                    });
                    ////End of Time dialog////
                }
            });
            //End of popupDate//

            //DatePicker listener//
            dateListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    updateDate(day, month, year);
                    timeDialog.show();
                }
            };
            //End of datePicker listener//

            //TimePicker listener//
            timeListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    updateTime(hour, minute);
                    getDialog().show();
                }
            };
            //End of timePicker listener//

            //Pressing the categoryName text will open category_comment dialog.
            popupCategoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = popupCategoryName.getText().toString().trim();

                    Dialog_Category_Comment dialog = new Dialog_Category_Comment();
                    dialog.setTargetFragment(DialogAddLine.this, 1);
                    dialog.show(getFragmentManager(), "Dialog_Category_Comment");

                    if (!text.equals("")) {
                        dialog.updateSelectedSpinnerItemValue(text);
                    } else {
                        dialog.updateSelectedSpinnerItemValue("בית");
                    }
                    //if comment is not empty,
                    if (popupUserComment.getVisibility() == View.VISIBLE && !popupUserComment.getText().toString().isEmpty()) {
                        String comment_text = popupUserComment.getText().toString();
                        dialog.updateCommentText(comment_text);//move the String to category_comment_dialog.
                    }
                }
            });

            //Pressing the userComment text will open category_comment dialog.
            popupUserComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = popupCategoryName.getText().toString().trim();
                    Dialog_Category_Comment dialog = new Dialog_Category_Comment();
                    dialog.setTargetFragment(DialogAddLine.this, 1);
                    dialog.show(getFragmentManager(), "Dialog_Category_Comment");

                    if (!text.equals("")) {
                        dialog.updateSelectedSpinnerItemValue(text);
                    } else {
                        dialog.updateSelectedSpinnerItemValue("בית");
                    }
                    String comment_text = popupUserComment.getText().toString();
                    dialog.updateCommentText(comment_text);//move the String to category_comment_dialog.
                }
            });
        }
        return builder.create();

    }

    private void formatDate(String dtStart) {
        SimpleDateFormat format;
        // format = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());

        if (!DateFormat.is24HourFormat(getActivity())) {//if 12 hours format show am/pm
            format = new SimpleDateFormat("MM/dd/yyyy, KK:mm aa", Locale.getDefault());
        } else {
            format = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
        }

        String stampDate = "";
        //Convert the date to another pattern to send to the fire base database.
        try {
            Date date = format.parse(dtStart);
            long stamp = date.getTime();
            stampDate = String.valueOf(stamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.stampDate = stampDate;
    }


    private void updateDate(int day, int month, int year) {
        month+=1;
        if (!DateFormat.is24HourFormat(getActivity())) {
            //If Format is 12 hours
            if (day < 10 && month < 10) {
                date_time = "0" + month + "/" + "0" + day + "/" + year;
            } else if (day < 10) {
                date_time = month + "/" + "0" + day + "/" + year;
            } else if (month < 10) {
                date_time = "0" + month + "/" + day + "/" + year;
            } else {
                date_time = month + "/" + day + "/" + year;
            }
        } else {
            if (day < 10 && month < 10) {
                date_time = "0" + day + "/" + "0" + month + "/" + year;
            } else if (day < 10) {
                date_time = "0" + day + "/" + month + "/" + year;
            } else if (month < 10) {
                date_time = day + "/" + "0" + month + "/" + year;
            } else {
                date_time = day + "/" + month + "/" + year;
            }
        }
    }

    private void updateTime(int hour, int minute) {
        if (!DateFormat.is24HourFormat(getActivity())) {
            // If dateFormat is 12 hours
            if (hour >= 12) {
                hour -= 12;
                date_time += " PM";
            } else {
                date_time += " AM";
            }
        }

        //Showing the user the time in hh:mm format, instead of h:m - ex: if the time is 03:09 - show it that way instead of 3:9.
        //Because the date_string last chars are " AM/ PM" I cut the string and add the formatted date before these last chars.
        if (hour < 10 && minute < 10) {
            date_time = date_time.substring(0, date_time.length() - 3) + ", " + "0" + hour + ":" + "0" + minute + date_time.substring(date_time.length() - 3, date_time.length());
        } else if (hour < 10) {
            date_time = date_time.substring(0, date_time.length() - 3) + ", " + "0" + hour + ":" + minute + date_time.substring(date_time.length() - 3, date_time.length());
        } else if (minute < 10) {
            date_time = date_time.substring(0, date_time.length() - 3) + ", " + hour + ":" + "0" + minute + date_time.substring(date_time.length() - 3, date_time.length());
        } else {
            date_time = date_time.substring(0, date_time.length() - 3) + ", " + hour + ":" + minute + date_time.substring(date_time.length() - 3, date_time.length());
        }

        popupDate.setText(date_time);
    }

    //Implementing interface//
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

    //Method to use in the balanceFragment - to get the itemView for editing.
    public void setItemViewForEditing(ItemView itemView) {
        String category = itemView.getCategoryName();
        String comment = itemView.getUserComment();
        String date = itemView.getDate();
        String money = itemView.getIncome_outcome();

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

    //Implementing textWatcher listener//
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
    //End of textWatcher listener//

}
