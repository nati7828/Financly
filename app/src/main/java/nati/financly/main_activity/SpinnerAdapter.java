package nati.financly.main_activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import nati.financly.R;

public class SpinnerAdapter extends ArrayAdapter<SpinnerItem> {

    public SpinnerAdapter(Context context, ArrayList<SpinnerItem>categories){
        super(context,0,categories);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    private View initView(int position, View view, ViewGroup parent){
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.spinner_category_item,parent,false);
        }

        ImageView icon = view.findViewById(R.id.spinner_row_image);
        TextView category = view.findViewById(R.id.spinner_row_text);

        SpinnerItem spinnerItem = getItem(position);

        if(spinnerItem != null){
            icon.setImageResource(spinnerItem.getImage());
            category.setText(spinnerItem.getCategory());
        }
        return view;
    }
}
