package nati.financly.main_activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import nati.financly.ItemView;
import nati.financly.R;

public class BalanceMainFragment extends Fragment {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference userRef;

    private String userId;
    private BalanceFragmentAdapter adapter;
    private ArrayList<ItemView> rvItems;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    TextView emptyScreenTv, moneyTv;
    EditText search_et;
    ImageButton searchBtn, exitBtn;
    NestedScrollView nestedScrollView;

    FloatingActionButton fab;

    public BalanceMainFragment() {
        //Empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_balance_main, container, false);

        Toolbar toolbar = v.findViewById(R.id.balance_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar();

        fab = v.findViewById(R.id.main_activity_fab);

        searchBtn = v.findViewById(R.id.balance_main_search_btn);
        exitBtn = v.findViewById(R.id.balance_main_exit_search_btn);
        search_et = v.findViewById(R.id.balance_main_search_et);
            search_et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String text = editable.toString();

                    ArrayList<ItemView> filteredList = new ArrayList<>();

                    for (int i = 0; i<rvItems.size(); i++ ) {
                        if (rvItems.get(i).getCategoryName().toLowerCase().contains(text.toLowerCase()) ||
                                rvItems.get(i).getUserComment().toLowerCase().contains(text.toLowerCase()) ||
                                rvItems.get(i).getDate().contains(text)||
                                rvItems.get(i).getIncome_outcome().contains(text)) {
                            filteredList.add(rvItems.get(i));
                        }
                    }
                    adapter.filteredList(filteredList);
                }
            });



        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setVisibility(View.VISIBLE);
                search_et.setText("");
                adapter.originalList(rvItems);
                search_et.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                exitBtn.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBtn.setVisibility(View.VISIBLE);
                search_et.setText("");
                search_et.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                view.setVisibility(View.GONE);
                adapter.originalList(rvItems);
                adapter.notifyDataSetChanged();
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        collapsingToolbarLayout = v.findViewById(R.id.balance_fragment_title);

        if (user != null) {
            userId = user.getUid();
        }

        //Recycler view part//
        final RecyclerView recyclerView = v.findViewById(R.id.main_activity_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        rvItems = new ArrayList<>();
        adapter = new BalanceFragmentAdapter(getActivity(), rvItems);
        recyclerView.setAdapter(adapter);

        userRef = myRef.child("Users").child(userId);

        adapter.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onEditClick(int position) {
                Toast.makeText(getActivity(),"clicked number" + position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(int position) {
                if (!search_et.isShown())
                {
                    adapter.filteredList(rvItems);
                }

                ItemView selectedItem = rvItems.get(position);
                String selectedKey = selectedItem.getKey();

               // adapter.originalList(rvItems);
                myRef.child("Users").child(userId).child(selectedKey).removeValue();
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, rvItems.size());
                adapter.notifyDataSetChanged();
            }
        });


        userRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        });

        //Floating action button//

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpDialogAddLine popUpDialogAddLine = new PopUpDialogAddLine();
                popUpDialogAddLine.userRef = userRef;
                popUpDialogAddLine.adapter = adapter;

                popUpDialogAddLine.show(getFragmentManager(), "popUpDialogAddLine");
            }
        });

        nestedScrollView = v.findViewById(R.id.nestedScrollView);
        emptyScreenTv = v.findViewById(R.id.emptyScreenTextView);
        return v;
        //
    }
    //End of onCreate//


    //ShowData method - invokes in the OnCreateView//
    private void showData(DataSnapshot dataSnapshot) {
        int moneySum = 0;
        rvItems.clear();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            ItemView itemView = new ItemView();
            String date = ds.child("date").getValue(String.class);
            String income_outcome = ds.child("income_outcome").getValue(String.class);
            String categoryName = ds.child("categoryName").getValue(String.class);
            String userComment = ds.child("userComment").getValue(String.class);
            String key = ds.getKey();
            if (income_outcome != null) {
                if ((income_outcome.contains(","))) {
                    income_outcome = income_outcome.replace(",", "");
                }
                moneySum += Integer.parseInt(income_outcome);
            }
            //Format the time in millis to date - to show the user the date in dd/MM//yyyy, HH:mm format.
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            // Create a calendar object that will convert the date and time value in
            // milliseconds to date.
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.valueOf(date));
                String formattedDate = formatter.format(calendar.getTime());

                itemView.setCategoryName(categoryName);
                itemView.setUserComment(userComment);
                itemView.setDate(formattedDate);
                itemView.setIncome_outcome(income_outcome);
                itemView.setKey(key);
            }

            if (itemView.getCategoryName() != null) {
                switch (itemView.getCategoryName()) {
                    case "משכורת":
                        itemView.setImage(R.drawable.payment);
                        break;
                    case "Salary":
                        itemView.setImage(R.drawable.payment);
                        break;
                    case "תשלום":
                        itemView.setImage(R.drawable.payment);
                        break;
                    case "Payment":
                        itemView.setImage(R.drawable.payment);
                        break;
                    case "בית":
                        itemView.setImage(R.drawable.home);
                        break;
                    case "House":
                        itemView.setImage(R.drawable.home);
                        break;
                    case "אוכל":
                        itemView.setImage(R.drawable.food);
                        break;
                    case "Food":
                        itemView.setImage(R.drawable.food);
                        break;
                    case "קניות":
                        itemView.setImage(R.drawable.shopping_cart);
                        break;
                    case "Shopping":
                        itemView.setImage(R.drawable.shopping_cart);
                        break;
                    case "ביגוד והנעלה":
                        itemView.setImage(R.drawable.clothes);
                        break;
                    case "Clothes and shoes":
                        itemView.setImage(R.drawable.clothes);
                        break;
                    case "שכירות":
                        itemView.setImage(R.drawable.rent);
                        break;
                    case "Rent":
                        itemView.setImage(R.drawable.rent);
                        break;
                    case "משכנתא":
                        itemView.setImage(R.drawable.mortgage);
                        break;
                    case "Mortgage":
                        itemView.setImage(R.drawable.mortgage);
                        break;
                    case "חשבונות":
                        itemView.setImage(R.drawable.bills);
                        break;
                    case "Bills":
                        itemView.setImage(R.drawable.bills);
                        break;
                    case "הלוואות":
                        itemView.setImage(R.drawable.loan);
                        break;
                    case "Loans":
                        itemView.setImage(R.drawable.loan);
                        break;
                    case "טואלטיקה":
                        itemView.setImage(R.drawable.toliet_and_clean);
                        break;
                    case "Toilet and clean":
                        itemView.setImage(R.drawable.toliet_and_clean);
                        break;
                    case "רכב":
                        itemView.setImage(R.drawable.car);
                        break;
                    case "Car":
                        itemView.setImage(R.drawable.car);
                        break;
                    case "תחבורה ציבורית":
                        itemView.setImage(R.drawable.taxi);
                        break;
                    case "Public transport":
                        itemView.setImage(R.drawable.taxi);
                        break;
                    case "ביטוחים":
                        itemView.setImage(R.drawable.insurance);
                        break;
                    case "Insurance":
                        itemView.setImage(R.drawable.insurance);
                        break;
                    case "תקשורת":
                        itemView.setImage(R.drawable.phone);
                        break;
                    case "Communications":
                        itemView.setImage(R.drawable.phone);
                        break;
                    case "חדר כושר":
                        itemView.setImage(R.drawable.gym);
                        break;
                    case "Gym":
                        itemView.setImage(R.drawable.gym);
                        break;
                    case "חיסכון":
                        itemView.setImage(R.drawable.savings);
                        break;
                    case "Savings":
                        itemView.setImage(R.drawable.savings);
                        break;
                    case "לימודים":
                        itemView.setImage(R.drawable.study);
                        break;
                    case "Studies":
                        itemView.setImage(R.drawable.study);
                        break;
                    case "בתי ספר":
                        itemView.setImage(R.drawable.school);
                        break;
                    case "School":
                        itemView.setImage(R.drawable.school);
                        break;
                    case "גני ילדים":
                        itemView.setImage(R.drawable.kindergarden);
                        break;
                    case "Kindergarten":
                        itemView.setImage(R.drawable.kindergarden);
                        break;
                    case "בילויים":
                        itemView.setImage(R.drawable.hangout);
                        break;
                    case "Spending time":
                        itemView.setImage(R.drawable.hangout);
                        break;
                    case "מסעדות":
                        itemView.setImage(R.drawable.restaurent);
                        break;
                    case "Restaurants":
                        itemView.setImage(R.drawable.restaurent);
                        break;
                    case "חיות":
                        itemView.setImage(R.drawable.pet);
                        break;
                    case "Pets":
                        itemView.setImage(R.drawable.pet);
                        break;
                    case "מתנות":
                        itemView.setImage(R.drawable.gift);
                        break;
                    case "Gifts":
                        itemView.setImage(R.drawable.gift);
                        break;
                    case "אחר":
                        itemView.setImage(R.drawable.other);
                        break;
                    case "Other":
                        itemView.setImage(R.drawable.other);
                        break;
                    default:
                        itemView.setImage(R.drawable.other);
                        break;
                }
                adapter.filteredList(rvItems);
                rvItems.add(0, itemView);
            }
        }
        if(moneySum == 0){
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedToolbarTitleZero);
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedToolbarTitleZero);
        } else if (moneySum > 0) {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedToolbarTitlePositive);
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedToolbarTitlePositive);
        } else {
            collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedToolbarTitleNegative);
            collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedToolbarTitleNegative);
        }
        String formattedMoneySum = String.format(Locale.getDefault(), "%,d", moneySum);
        String shekel = "₪ ";
        String txtMoney = shekel + formattedMoneySum;
        Spannable spannedMoney = new SpannableString(txtMoney);
        spannedMoney.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannedMoney.setSpan(new RelativeSizeSpan(0.85f), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannedMoney.setSpan(new StyleSpan(Typeface.NORMAL), 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        collapsingToolbarLayout.setTitle(spannedMoney + "");
        //moneyTv.setText(spannedMoney);
        adapter.notifyDataSetChanged();

        //In case the list is empty(no incomes/expanses) show text
        if (adapter.getItemCount() == 0) {
            emptyScreenTv.setVisibility(View.VISIBLE);
            nestedScrollView.setVisibility(View.GONE);

            String emptyText = emptyScreenTv.getText().toString();

            Spannable spannedTextForEmptyScreen = new SpannableString(emptyText);
            spannedTextForEmptyScreen.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorHint)), 18, emptyText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannedTextForEmptyScreen.setSpan(new RelativeSizeSpan(0.89f), 18, emptyText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            spannedTextForEmptyScreen.setSpan(new StyleSpan(Typeface.BOLD), 0, 18, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            emptyScreenTv.setText(spannedTextForEmptyScreen);
        } else {
            emptyScreenTv.setVisibility(View.GONE);
            nestedScrollView.setVisibility(View.VISIBLE);
        }
    }
    //End showData method//
}
