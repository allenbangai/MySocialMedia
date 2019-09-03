package com.example.mysocialmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    EditText registerUserEmail, registerUserPassword, registerUserConfirmPassword;
    Button registerCreateAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerCreateAccountButton = findViewById(R.id.id_button_register);
        registerUserConfirmPassword = findViewById(R.id.id_register_confirm_password);
        registerUserPassword = findViewById(R.id.id_register_password);
        registerUserEmail = findViewById(R.id.id_register_email);
    }


}
