package com.iosite.io_safesite.Activity.LoginActivity.MobileFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iosite.io_safesite.Activity.LoginActivity.LoginActivity;
import com.iosite.io_safesite.R;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.PrefUtil;
import com.iosite.io_safesite.Util.StringUtil;
public class MobileFragment extends Fragment implements View.OnClickListener {


    private EditText mobileNumber;
    private Button submitPhoneNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_mobile, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {

        mobileNumber = view.findViewById(R.id.mobile_number);
        submitPhoneNumber = view.findViewById(R.id.submit_phone_number);
        submitPhoneNumber.setEnabled(false);
        submitPhoneNumber.setOnClickListener(this);
        mobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!StringUtil.isNullOrEmpty(String.valueOf(mobileNumber.getText()))) {
                    if(mobileNumber.getText().length() == 10) {
                        submitPhoneNumber.setBackgroundResource(R.drawable.button_with_round_corner_enabled);
                        submitPhoneNumber.setEnabled(true);
                    } else {
                        submitPhoneNumber.setBackgroundResource(R.drawable.button_with_round_corner);
                        submitPhoneNumber.setEnabled(false);
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_phone_number:
                if (validate()) {
                    PrefUtil.putString(getActivity(), Constants.PREF_CONTACT_NUMBER, mobileNumber.getText().toString());
                    ((LoginActivity)getActivity()).savePhoneNumber(mobileNumber.getText().toString());
                    ((LoginActivity)getActivity()).generateOTP();
                }
                break;
            default:
                break;
        }
    }

    private boolean validate() {
        if (StringUtil.isNullOrEmpty(mobileNumber.getText().toString())) {
            Toast.makeText(getActivity(), "Please enter mobile number first.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
