package com.iosite.io_safesite.Activity.LoginActivity.OTPFragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iosite.io_safesite.Activity.LoginActivity.LoginActivity;
import com.iosite.io_safesite.R;


public class OTPFragment extends Fragment implements View.OnClickListener {

    private TextView userPhoneNumber;
    private TextView wrongNumber;
    private EditText otpOne;
    private EditText otpTwo;
    private EditText otpThree;
    private EditText otpFour;
    private EditText otpFive;
    private EditText otpSix;
    private TextView otpExpireTimer;
    private TextView resendOtp;
    private Button submitOtp;
    private TextView otpExpireText;
    private int otpExpireDuration = 10;  // minutes
    private View view;
    private LinearLayout invalidOtpLayout;
    private boolean otpExpired;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_otp, container, false);
        initUI(view);
        return view;
    }

    private void initUI(View view) {
        userPhoneNumber = view.findViewById(R.id.user_phone_number);
        wrongNumber = view.findViewById(R.id.wrong_number);
        otpOne = view.findViewById(R.id.otp_1);
        otpTwo = view.findViewById(R.id.otp_2);
        otpThree = view.findViewById(R.id.otp_3);
        otpFour = view.findViewById(R.id.otp_4);
        otpFive = view.findViewById(R.id.otp_5);
        otpSix = view.findViewById(R.id.otp_6);
        otpExpireTimer = view.findViewById(R.id.otp_expire_timer);
        resendOtp = view.findViewById(R.id.resend_otp);
        submitOtp = view.findViewById(R.id.submit_otp);
        otpExpireText = view.findViewById(R.id.otp_expire_text);
        invalidOtpLayout = view.findViewById(R.id.contact_layout);
        invalidOtpLayout.setVisibility(View.GONE);

        userPhoneNumber.setText(((LoginActivity)getActivity()).getPhoneNumber());

        wrongNumber.setOnClickListener(this);
        resendOtp.setOnClickListener(this);
        submitOtp.setOnClickListener(this);
        submitOtp.setEnabled(false);

        otpExpired = false;

        updateOtpExpireText("Your OTP will expire in");

        startCountDownTimer(otpExpireDuration*60*1000);
        setupAutoFocusInOtp();
    }

    private void startCountDownTimer(final int timerDuration) {
        new CountDownTimer(timerDuration, 1000) {

            public void onTick(long millisUntilFinished) {
                String timeString = String.format("%02d:%02d", ((int)((millisUntilFinished / 1000)/60)), ((int) (millisUntilFinished / 1000) % 60));
                otpExpireTimer.setText(timeString);
            }

            public void onFinish() {
                otpExppireView();
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wrong_number:
//                ((LoginActivity)getActivity()).removeOneFragment();
                ((LoginActivity)getActivity()).showMobileFragment();
                break;
            case R.id.resend_otp:
//                ((LoginActivity)getActivity()).removeOneFragment();
                ((LoginActivity)getActivity()).generateOTP();
                break;
            case R.id.submit_otp:
                String inputOtp = otpOne.getText().toString() +
                        otpTwo.getText().toString() +
                        otpThree.getText().toString() +
                        otpFour.getText().toString() +
                        otpFive.getText().toString() +
                        otpSix.getText().toString();
                ((LoginActivity)getActivity()).saveOtp(inputOtp);
                ((LoginActivity)getActivity()).validateOTP();
                break;
            default:
                break;
        }

    }

    public void updateOtpExpireText(String newText) {
        otpExpireText.setText(newText);
    }

    public void updateOtpExpireTextColor(int color) {
        otpExpireText.setTextColor(color);
    }

    public void getValidateOtpResponse(int responseCode) {
        if(responseCode == 400) {
            invalidOtpLayout.setVisibility(View.VISIBLE);
        } else if(responseCode == 404) {
            otpExppireView();
        }
    }

    private void otpExppireView() {
        otpExpired = true;
        updateOtpExpireText("OTP Expired. Try resending.");
        updateOtpExpireTextColor(Color.RED);
        otpExpireTimer.setText("");
        submitOtp.setBackgroundResource(R.drawable.button_with_round_corner);
        submitOtp.setEnabled(false);

    }

    private void isOtpComplete() {
        if(
        otpOne.getText().toString().length() == 1 &&
        otpTwo.getText().toString().length() == 1 &&
        otpThree.getText().toString().length() == 1 &&
        otpFour.getText().toString().length() == 1 &&
        otpFive.getText().toString().length() == 1 &&
        otpSix.getText().toString().length() == 1
        ) {
            closeSoftKeyboard();
            if(!otpExpired) {
                submitOtp.setBackgroundResource(R.drawable.button_with_round_corner_enabled);
                submitOtp.setEnabled(true);
            }
        } else {
            submitOtp.setBackgroundResource(R.drawable.button_with_round_corner);
            submitOtp.setEnabled(false);
        }
    }

    private void closeSoftKeyboard() {
        view = getActivity().getWindow().getDecorView();
        view.clearFocus();
    }

    private void setupAutoFocusInOtp() {
        otpOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(otpOne.getText().toString().length() == 1)     //size as per your requirement
                {
                    otpTwo.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        otpTwo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(otpTwo.getText().toString().length()==1)     //size as per your requirement
                {
                    otpThree.requestFocus();
                }
                isOtpComplete();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0) {
                    otpOne.requestFocus();
                }
            }
        });
        otpThree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(otpThree.getText().toString().length()==1)     //size as per your requirement
                {
                    otpFour.requestFocus();
                }
                isOtpComplete();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0) {
                    otpTwo.requestFocus();
                }
            }
        });
        otpFour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(otpFour.getText().toString().length()==1)     //size as per your requirement
                {
                    otpFive.requestFocus();
                }
                isOtpComplete();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0) {
                    otpThree.requestFocus();
                }
            }
        });
        otpFive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(otpFive.getText().toString().length()==1)     //size as per your requirement
                {
                    otpSix.requestFocus();
                }
                isOtpComplete();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0) {
                    otpFour.requestFocus();
                }
            }
        });
        otpSix.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isOtpComplete();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() == 0) {
                    otpFive.requestFocus();
                }
            }
        });
    }

}
