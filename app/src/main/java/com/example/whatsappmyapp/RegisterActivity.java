package com.example.whatsappmyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        IntializeFields();

        AlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });
        
        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString().trim();
        String password = UserPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password...", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we ware creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password) /** MAuth ekata username ekai password ekai daala register karanawa .. mAuth = FirebaseAuth.getInstance();*/
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String currentUserId = mAuth.getCurrentUser().getUid();  /**Get Current mAuth eke user Id eka*/
                                RootRef.child("Users").child(currentUserId).setValue("");/** RootRef = FirebaseDatabase.getInstance().getReference();*/

                                String deviceToken = FirebaseInstanceId.getInstance().getToken(); /* Device ekata adaala unic Token id ekak*/

                                RootRef.child("Users").child(currentUserId).child("device_token")
                                        .setValue(deviceToken);


                                SendUserToMainActivity();

                                Toast.makeText(RegisterActivity.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+message , Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void IntializeFields() {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccount = (TextView) findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(LoginIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);/**User Account eka hadala regi wechcha gaman yanne MainActivity ekata.*/
        startActivity(mainIntent);                                     /**Back kalata aaai Register Activity ekata enna baha enna oonanam log out wela enna oone meeka wadagath*/
        finish();
    }
}