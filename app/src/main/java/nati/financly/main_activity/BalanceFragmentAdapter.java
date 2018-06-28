package nati.financly.main_activity;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import nati.financly.R;

public class BalanceFragmentAdapter extends RecyclerView.Adapter<BalanceFragmentAdapter.ViewHolder> {
    private OnItemClickListener listener;

    private Context context;
    private ArrayList<ItemModel> itemsList;

    //Constructor
    BalanceFragmentAdapter(Context context, ArrayList<ItemModel> list) {
        this.context = context;
        itemsList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    ////Recycler view adapter methods////
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.rv_item, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemModel currentItem = itemsList.get(position);
        ImageView categoryIcon = holder.categoryIcon;
        TextView categoryName = holder.rvCategoryName;
        TextView userComment = holder.userComment;
        TextView date = holder.rvItemDate;
        TextView moneyLabel = holder.rvItemMoneyLabel;

        categoryIcon.setImageResource(currentItem.getImage());
        categoryName.setText(currentItem.getCategoryName());
        userComment.setText(currentItem.getUserComment());
        date.setText(currentItem.getDate());

        if (userComment.getText().toString().isEmpty()) {
            userComment.setVisibility(View.GONE);

            LinearLayout.LayoutParams categoryParams = (LinearLayout.LayoutParams) categoryName.getLayoutParams();
            categoryParams.setMargins(0, 27, 0, 0); //substitute parameters for left, top, right, bottom
            categoryName.setLayoutParams(categoryParams);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) date.getLayoutParams();
            params.setMargins(0, 10, 0, 0); //substitute parameters for left, top, right, bottom
            date.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams categoryParams = (LinearLayout.LayoutParams) categoryName.getLayoutParams();
            categoryParams.setMargins(0, 0, 0, 0); //substitute parameters for left, top, right, bottom
            categoryName.setLayoutParams(categoryParams);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) date.getLayoutParams();
            params.setMargins(0, 0, 0, 0); //substitute parameters for left, top, right, bottom
            date.setLayoutParams(params);
            userComment.setVisibility(View.VISIBLE);
        }
        //Show the money label with color - green or red with shekel icon.
        String currentMoneyChange = currentItem.getIncome_outcome();

        String formattedMoney;

        if(currentMoneyChange.contains(",")) {
            formattedMoney = currentMoneyChange;
        }else {
            formattedMoney = String.format(Locale.getDefault(), "%,d", Integer.valueOf(currentMoneyChange));
        }
        String shekel = "₪ ";
        if(Locale.getDefault().getDisplayLanguage().equals(Locale.ENGLISH.getDisplayName())){
            shekel = "$ ";
        }

        String txtMoney = shekel + formattedMoney;

        Spannable spannedMoney = new SpannableString(txtMoney);
        spannedMoney.setSpan(new RelativeSizeSpan(0.85f), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannedMoney.setSpan(new StyleSpan(Typeface.NORMAL), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        if (!currentMoneyChange.isEmpty()) {
            if (currentMoneyChange.contains(",")){
                currentMoneyChange = currentMoneyChange.replace(",","");
            }
            if (Integer.valueOf(currentMoneyChange) >= 0) {
                moneyLabel.setTextColor(context.getResources().getColor(R.color.colorGreen));
            } else {
                moneyLabel.setTextColor(context.getResources().getColor(R.color.colorRed));
            }
        }
        moneyLabel.setText(spannedMoney);
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public void filteredList(ArrayList<ItemModel> filteredList) {
        itemsList = filteredList;
        notifyItemRemoved(getItemCount());
        notifyItemRangeChanged(getItemCount(), itemsList.size());
        notifyDataSetChanged();
    }

    public void originalList(ArrayList<ItemModel> rvItems) {
        itemsList = rvItems;
        notifyItemRemoved(getItemCount());
        notifyItemRangeChanged(getItemCount(), itemsList.size());
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        ImageView categoryIcon;
        TextView rvCategoryName, userComment, rvItemDate, rvItemMoneyLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.main_rv_icon);
            rvCategoryName = itemView.findViewById(R.id.main_rv_category_name);
            userComment = itemView.findViewById(R.id.main_rv_category_details);
            rvItemDate = itemView.findViewById(R.id.main_rv_date);
            rvItemMoneyLabel = itemView.findViewById(R.id.main_rv_money_text);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

        }
        //End of view holder constructor.

        //When clicking a short click on item - just highlight it.
        @Override
        public void onClick(View view) {
            final Animation animation = new AlphaAnimation(0.5f, 1);
            animation.setDuration(80);
            view.startAnimation(animation);
        }

        ///When clicking a long click on item, a menu will appear with two options: edit or delete.
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(R.string.choose);//title
            MenuItem edit = contextMenu.add(Menu.NONE, 1, 1, R.string.edit);//option 1
            MenuItem delete = contextMenu.add(Menu.NONE, 2, 2, R.string.delete);//option 2

            edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (menuItem.getItemId()) {
                        case 1:
                            listener.onEditClick(position);
                            return true;
                        case 2:
                            listener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }
    ////End of ViewHolder class////
}
