package com.example.mysocialmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText setupUsername, setupFullName, setupCountryName;
    private Button setupSaveInfo;
    private CircleImageView setupProfieImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupCountryName = findViewById(R.id.id_setup_country);
        setupFullName = findViewById(R.id.id_setuo_full_name);
        setupProfieImage = findViewById(R.id.id_setup_profile_image);
        setupUsername = findViewById(R.id.id_setup_username);
        setupSaveInfo = findViewById(R.id.id_setup_save_button);
    }
}
