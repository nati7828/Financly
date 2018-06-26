package nati.financly.main_activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import nati.financly.R;

public class UserDetailsFragment extends Fragment implements View.OnClickListener {

    TextView nameTV;
    TextView emailTV;

    public UserDetailsFragment() {
        //Empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_user_details, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        nameTV = v.findViewById(R.id.details_nameTV);
        emailTV = v.findViewById(R.id.details_emailTV);
        Button clickButton = v.findViewById(R.id.user_details_button);
        final TextView moneyTV = v.findViewById(R.id.details_moneyTV);

        LinearLayout nameLinearClick = v.findViewById(R.id.user_details_name);
        LinearLayout emailLinearClick = v.findViewById(R.id.user_details_email);
        LinearLayout moneyLinearClick = v.findViewById(R.id.user_details_money_balance);
        nameLinearClick.setOnClickListener(this);
        emailLinearClick.setOnClickListener(this);
        moneyLinearClick.setOnClickListener(this);
        clickButton.setOnClickListener(this);

        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }

        DatabaseReference userRef = myRef.child("Users").child(userId);

        DatabaseReference nameUserRef = userRef.child("name");

        nameUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(isAdded()) {
                    String name = dataSnapshot.getValue(String.class);
                    nameTV.setText(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference emailUserRef = userRef.child("email");

        emailUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                emailTV.setText(email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference moneyUserRef = userRef.child("money");

        moneyUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String money = dataSnapshot.getValue(String.class);
                Spannable spannedMoney = new SpannableString(money);
                spannedMoney.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                spannedMoney.setSpan(new RelativeSizeSpan(0.85f), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                if (money != null && !money.isEmpty()) {
                    String temporaryMoney = "";
                    if (money.contains("₪")) {
                        temporaryMoney = money.substring(2);
                    }

                    if (temporaryMoney.contains(",")) {
                        temporaryMoney = temporaryMoney.replace(",", "");
                    }
                    if (Integer.valueOf(temporaryMoney) >= 0) {
                        moneyTV.setTextColor(getResources().getColor(R.color.colorGreen));
                    } else {
                        moneyTV.setTextColor(getResources().getColor(R.color.colorRed));
                    }
                }
                moneyTV.setText(spannedMoney);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Method to update name of user in drawer header

        return v;
    }

    @Override
    public void onClick(View view) {
        final Animation animation = new AlphaAnimation(0.5f, 1);
        animation.setDuration(80);
        view.startAnimation(animation);

        switch (view.getId()) {
            case R.id.user_details_button:
                    DialogUserDetails dialog = new DialogUserDetails();
                    String userName = nameTV.getText().toString();
                    dialog.getNameFromFragment(userName);
                    dialog.show(getFragmentManager(), "dialogUserDetails");
        }
    }
    //End of onCreateView//
}
