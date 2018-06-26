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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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

import nati.financly.R;

public class BalanceFragment extends Fragment {

    //private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference userRef;

    private String userId;
    private BalanceFragmentAdapter adapter;
    //original list
    private ArrayList<ItemModel> rvItems;
    //filtered list
    ArrayList<ItemModel> filteredList;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    TextView emptyScreenTv;
    EditText search_et;
    ImageButton searchBtn, exitBtn;
    NestedScrollView nestedScrollView;
    FloatingActionButton fab;
    private boolean isFiltering = false;

//    public BalanceFragment() {
//        //Empty constructor
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_balance, container, false);

        Toolbar toolbar = v.findViewById(R.id.balance_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar();

        final DialogAddLine dialogAddLine = new DialogAddLine();

        nestedScrollView = v.findViewById(R.id.nestedScrollView);
        emptyScreenTv = v.findViewById(R.id.emptyScreenTextView);
        fab = v.findViewById(R.id.main_activity_fab);

        searchBtn = v.findViewById(R.id.balance_main_search_btn);
        exitBtn = v.findViewById(R.id.balance_main_exit_search_btn);
        search_et = v.findViewById(R.id.balance_main_search_et);
        filteredList = new ArrayList<>();
        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isFiltering = search_et != null && !search_et.getText().toString().isEmpty();
                final String text = editable.toString();
                readDataFromDB(filteredList, text);
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_et.setVisibility(View.VISIBLE);
                search_et.setText("");
                search_et.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {//show keyboard
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
                if (imm != null) {//hide keyboard
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                view.setVisibility(View.GONE);
            }
        });


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        collapsingToolbarLayout = v.findViewById(R.id.balance_fragment_title);
        //showMoneyBalanceListener.showBalance(money);
        if (user != null) {
            userId = user.getUid();
        }
        //Recycler view part//
        final RecyclerView recyclerView = v.findViewById(R.id.main_activity_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        rvItems = new ArrayList<>();
        adapter = new BalanceFragmentAdapter(getActivity(), rvItems);
        recyclerView.setAdapter(adapter);

        userRef = myRef.child("Users").child(userId);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                ItemModel selectedItem;
                if (filteredList != null && !filteredList.isEmpty() && filteredList.size() != rvItems.size()) {
                    selectedItem = filteredList.get(position);
                } else {
                    selectedItem = rvItems.get(position);
                }

                dialogAddLine.setItemViewForEditing(selectedItem);//send the dialog the itemModel for editing
                dialogAddLine.setUserRefAndAdapter(userRef, adapter);
                if (!dialogAddLine.isAdded()) {
                    dialogAddLine.show(getFragmentManager(), "dialogAddLine");
                }
            }

            @Override
            public void onDeleteClick(int position) {
                ItemModel selectedItem;
                String selectedKey;

                if (filteredList != null && !filteredList.isEmpty() && filteredList.size() != rvItems.size()) {
                    selectedItem = filteredList.get(position);
                    selectedKey = selectedItem.getKey();

                    myRef.child("Users").child(userId).child(selectedKey).removeValue();
                    adapter.filteredList(filteredList);

                } else {
                    selectedItem = rvItems.get(position);
                    selectedKey = selectedItem.getKey();
                    myRef.child("Users").child(userId).child(selectedKey).removeValue();
                    adapter.originalList(rvItems);
                }

            }
        });

        readDataFromDB(rvItems, "");


        //Floating action button//
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAddLine.setUserRefAndAdapter(userRef, adapter);
                if (!dialogAddLine.isAdded()) {
                    dialogAddLine.show(getFragmentManager(), "dialogAddLine");
                }
            }
        });

        return v;
        //
    }
    //End of onCreate//

    private void readDataFromDB(final ArrayList<ItemModel> arrayList, final String text) {
        userRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    showData(dataSnapshot, arrayList, text);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //ShowData method - invokes in the OnCreateView//
    private void showData(DataSnapshot dataSnapshot, ArrayList<ItemModel> arrayList, String text) {
        int moneySum = 0;
        arrayList.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            ItemModel itemModel = new ItemModel();
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
            SimpleDateFormat formatter;

            if (!DateFormat.is24HourFormat(getActivity())) {
                formatter = new SimpleDateFormat("MM/dd/yyyy, KK:mm aa", Locale.getDefault());
            } else {
                formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            }
            // Make a calendar object that will convert the date and time value in
            // milliseconds to date.
            if (date != null && !date.equals("")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.valueOf(date));
                String formattedDate = formatter.format(calendar.getTime());
                itemModel.setDate(formattedDate);
            }
            itemModel.setCategoryName(categoryName);
            itemModel.setUserComment(userComment);
            itemModel.setIncome_outcome(income_outcome);
            itemModel.setKey(key);

            if (itemModel.getCategoryName() != null) {
                switch (itemModel.getCategoryName()) {
                    case "משכורת":
                        itemModel.setImage(R.drawable.payment);
                        break;
                    case "Salary":
                        itemModel.setImage(R.drawable.payment);
                        break;
                    case "תשלום":
                        itemModel.setImage(R.drawable.payment);
                        break;
                    case "Payment":
                        itemModel.setImage(R.drawable.payment);
                        break;
                    case "בית":
                        itemModel.setImage(R.drawable.home);
                        break;
                    case "House":
                        itemModel.setImage(R.drawable.home);
                        break;
                    case "אוכל":
                        itemModel.setImage(R.drawable.food);
                        break;
                    case "Food":
                        itemModel.setImage(R.drawable.food);
                        break;
                    case "קניות":
                        itemModel.setImage(R.drawable.shopping_cart);
                        break;
                    case "Shopping":
                        itemModel.setImage(R.drawable.shopping_cart);
                        break;
                    case "ביגוד והנעלה":
                        itemModel.setImage(R.drawable.clothes);
                        break;
                    case "Clothes and shoes":
                        itemModel.setImage(R.drawable.clothes);
                        break;
                    case "שכירות":
                        itemModel.setImage(R.drawable.rent);
                        break;
                    case "Rent":
                        itemModel.setImage(R.drawable.rent);
                        break;
                    case "משכנתא":
                        itemModel.setImage(R.drawable.mortgage);
                        break;
                    case "Mortgage":
                        itemModel.setImage(R.drawable.mortgage);
                        break;
                    case "חשבונות":
                        itemModel.setImage(R.drawable.bills);
                        break;
                    case "Bills":
                        itemModel.setImage(R.drawable.bills);
                        break;
                    case "הלוואות":
                        itemModel.setImage(R.drawable.loan);
                        break;
                    case "Loans":
                        itemModel.setImage(R.drawable.loan);
                        break;
                    case "טואלטיקה וניקיון":
                        itemModel.setImage(R.drawable.toliet_and_clean);
                        break;
                    case "Toiletries/Cleaning":
                        itemModel.setImage(R.drawable.toliet_and_clean);
                        break;
                    case "רכב":
                        itemModel.setImage(R.drawable.car);
                        break;
                    case "Car":
                        itemModel.setImage(R.drawable.car);
                        break;
                    case "תחבורה ציבורית":
                        itemModel.setImage(R.drawable.taxi);
                        break;
                    case "Public transport":
                        itemModel.setImage(R.drawable.taxi);
                        break;
                    case "ביטוחים":
                        itemModel.setImage(R.drawable.insurance);
                        break;
                    case "Insurance":
                        itemModel.setImage(R.drawable.insurance);
                        break;
                    case "תקשורת":
                        itemModel.setImage(R.drawable.phone);
                        break;
                    case "Communications":
                        itemModel.setImage(R.drawable.phone);
                        break;
                    case "חדר כושר":
                        itemModel.setImage(R.drawable.gym);
                        break;
                    case "Gym":
                        itemModel.setImage(R.drawable.gym);
                        break;
                    case "חיסכון":
                        itemModel.setImage(R.drawable.savings);
                        break;
                    case "Savings":
                        itemModel.setImage(R.drawable.savings);
                        break;
                    case "לימודים":
                        itemModel.setImage(R.drawable.study);
                        break;
                    case "Studies":
                        itemModel.setImage(R.drawable.study);
                        break;
                    case "בתי ספר":
                        itemModel.setImage(R.drawable.school);
                        break;
                    case "School":
                        itemModel.setImage(R.drawable.school);
                        break;
                    case "גני ילדים":
                        itemModel.setImage(R.drawable.kindergarden);
                        break;
                    case "Kindergarten":
                        itemModel.setImage(R.drawable.kindergarden);
                        break;
                    case "בילויים":
                        itemModel.setImage(R.drawable.hangout);
                        break;
                    case "Spending time":
                        itemModel.setImage(R.drawable.hangout);
                        break;
                    case "מסעדות":
                        itemModel.setImage(R.drawable.restaurent);
                        break;
                    case "Restaurants":
                        itemModel.setImage(R.drawable.restaurent);
                        break;
                    case "חיות":
                        itemModel.setImage(R.drawable.pet);
                        break;
                    case "Pets":
                        itemModel.setImage(R.drawable.pet);
                        break;
                    case "מתנות":
                        itemModel.setImage(R.drawable.gift);
                        break;
                    case "Gifts":
                        itemModel.setImage(R.drawable.gift);
                        break;
                    case "אחר":
                        itemModel.setImage(R.drawable.other);
                        break;
                    case "Other":
                        itemModel.setImage(R.drawable.other);
                        break;
                    default:
                        itemModel.setImage(R.drawable.other);
                        break;
                }
                if (isFiltering) {
                    addForFilteredList(arrayList, itemModel, text);
                } else {
                    addForOriginalList(arrayList, itemModel);
                }
            }
        }
        if (moneySum == 0) {
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
        userRef.child("money").setValue(String.valueOf(txtMoney));

        Spannable spannedMoney = new SpannableString(txtMoney);
        spannedMoney.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannedMoney.setSpan(new RelativeSizeSpan(0.85f), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannedMoney.setSpan(new StyleSpan(Typeface.NORMAL), 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        collapsingToolbarLayout.setTitle(spannedMoney + "");
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

    private void addForOriginalList(ArrayList<ItemModel> rvItems, ItemModel itemModel) {
        rvItems.add(0, itemModel);
        adapter.originalList(rvItems);
    }

    private void addForFilteredList(ArrayList<ItemModel> filteredList, ItemModel itemModel, String text) {
        if (itemModel.getDate() != null) {
            if (itemModel.getDate().toLowerCase().contains(text.toLowerCase()) ||
                    itemModel.getCategoryName().toLowerCase().contains(text.toLowerCase()) ||
                    itemModel.getUserComment().toLowerCase().contains(text.toLowerCase()) ||
                    itemModel.getIncome_outcome().contains(text)) {
                filteredList.add(0, itemModel);
                adapter.filteredList(filteredList);
            }
        }
    }


}

