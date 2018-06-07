package nati.financly.main_activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import nati.financly.R;


public class UserDocumentsFragment extends Fragment {


    public UserDocumentsFragment() {
        //Empty constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_documents,container,false);



        return v;
    }

}
