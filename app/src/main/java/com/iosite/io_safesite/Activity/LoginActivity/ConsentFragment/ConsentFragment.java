package com.iosite.io_safesite.Activity.LoginActivity.ConsentFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.iosite.io_safesite.Activity.LoginActivity.LoginActivity;
import com.iosite.io_safesite.R;


public class ConsentFragment extends Fragment implements View.OnClickListener {
    private Button iAgreeButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_consent, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        iAgreeButton = view.findViewById(R.id.i_agree_button);
        iAgreeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.i_agree_button:
//                ((LoginActivity)getActivity()).showNextActivity();
                ((LoginActivity)getActivity()).bleInitialCheck();
                break;
            default:
                break;
        }
    }
}
