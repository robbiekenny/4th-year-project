package finalproject.homesecurity.UI.SetUpSecurityOrPersonal;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import finalproject.homesecurity.R;

/*
SIMPLY DISPLAY INFORMATION ON HOW TO SET UP THE DEVICE AS A SECURITY DEVICE
 */

public class SecurityHelpFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.security_help_layout, container, false);
    }


}
