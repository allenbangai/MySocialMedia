package com.example.mysocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerUserEmail, registerUserPassword, registerUserConfirmPassword;
    private Button registerCreateAccountButton;
    private ProgressDialog loadingbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        registerCreateAccountButton = findViewById(R.id.id_button_register);
        registerUserConfirmPassword = findViewById(R.id.id_register_confirm_password);
        registerUserPassword = findViewById(R.id.id_register_password);
        registerUserEmail = findViewById(R.id.id_register_email);

        registerCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccout();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }

    private void createAccout() {
        String email = registerUserEmail.getText().toString();
        String password = registerUserPassword.getText().toString();
        String confirmPassword = registerUserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "enter email, Email Address field is empty", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Password fiels is empty", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Confirm Password field is empty", Toast.LENGTH_SHORT).show();
        }
        else if(password.length()<6){
            Toast.makeText(this, "Password length should be greater than 5", Toast.LENGTH_LONG).show();
        }
        else if(!confirmPassword.equals(password)){
            Toast.makeText(this, "Password does not match Confirm Password", Toast.LENGTH_LONG).show();
        }
        else{
            loadingbar = new ProgressDialog(this);
            loadingbar.setTitle("Creating User Account");
            loadingbar.setMessage("Please wait while we are creating your new account");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToSetupActivity();

                        Toast.makeText(RegisterActivity.this,"You have being authenticated", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error occured \n"+message, Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupActivityIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivityIntent);
        finish();
    }


}
