package finalproject.homesecurity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Robbie on 30/09/2015.
 */
public class RegisterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_layout,
                container, false);

        Button b = (Button) view.findViewById(R.id.cancel_button);
        return view;
    }



}
