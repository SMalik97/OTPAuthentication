package com.malik.otpverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    Button btnGenerateOTP, btnSignIn;
    EditText etPhoneNumber, etOTP;
    private FirebaseAuth mAuth;
    private Spinner spinner;
    private String verificationid;
    PhoneAuthProvider.ForceResendingToken token;
    TextView resend;
    String ph_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGenerateOTP = findViewById(R.id.btn_generate_otp);
        btnSignIn = findViewById(R.id.btn_sign_in);

        etPhoneNumber = findViewById(R.id.et_phone_number);
        etOTP = findViewById(R.id.et_otp);
        resend=(TextView)findViewById(R.id.resend);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //set country code spinner
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));



        btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ph_number="+"+CountryData.countryAreaCodes[spinner.getSelectedItemPosition()]+etPhoneNumber.getText().toString();
                sendVerificationCode(ph_number);
            }
        });


        //resend otp
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthProvider.verifyPhoneNumber(
                        PhoneAuthOptions
                                .newBuilder(FirebaseAuth.getInstance())
                                .setActivity(MainActivity.this)
                                .setPhoneNumber(ph_number)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setCallbacks(mCallBack)
                                .setForceResendingToken(token)
                                .build());
            }
        });


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = etOTP.getText().toString().trim();

                if ((code.isEmpty() || code.length() < 6)){

                    etOTP.setError("Enter code...");
                    etOTP.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });


    }
    private void verifyCode(String code){
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid, code);
            signInWithCredential(credential);
        }catch (Exception e){
            Toast toast = Toast.makeText(getApplicationContext(), "Verification Code is wrong, try again", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(getApplicationContext(), SignedIn.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                           // progressDialog.hide();

                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //progressDialog.hide();
                        }
                    }

                });
    }

    private void sendVerificationCode(String number){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(MainActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationid = s;
            token=forceResendingToken;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
              //  progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
               // progressDialog.hide();
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
           // progressDialog.hide();
        }
    };



}
