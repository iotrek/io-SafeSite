package com.iosite.io_safesite.Activity.LoginActivity.PermissionFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iosite.io_safesite.Activity.LoginActivity.LoginActivity;
import com.iosite.io_safesite.R;


public class PermissionFragment extends Fragment implements View.OnClickListener {
    private Button setupDeviceButton;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_permission, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        setupDeviceButton = view.findViewById(R.id.setup_device_button);
        setupDeviceButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setup_device_button:
                ((LoginActivity)getActivity()).getPermissions();
                break;
            default:
                break;
        }
    }
}
