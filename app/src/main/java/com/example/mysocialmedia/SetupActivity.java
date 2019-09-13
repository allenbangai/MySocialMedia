package com.example.mysocialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText setupUsername, setupFullName, setupCountryName;
    private Button setupSaveInfo;
    private CircleImageView setupProfieImage;
    private ProgressDialog loadingbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;


    String currentUserId;
    final static int gallery_Prick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        setupCountryName = findViewById(R.id.id_setup_country);
        setupFullName = findViewById(R.id.id_setuo_full_name);
        setupProfieImage = findViewById(R.id.id_setup_profile_image);
        setupUsername = findViewById(R.id.id_setup_username);
        setupSaveInfo = findViewById(R.id.id_setup_save_button);

        setupSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSetupAccountInfo();
            }
        });

        setupProfieImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallery_Prick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_Prick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();
        }
    }

    private void saveSetupAccountInfo() {
        String fullName = setupFullName.getText().toString();
        String userName = setupUsername.getText().toString();
        String country = setupCountryName.getText().toString();

        if(TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Full Name field is empty", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(userName)){
            Toast.makeText(this, "User Name field is empty", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Country field is empty", Toast.LENGTH_LONG).show();
        }
        else{
            HashMap userMap = new HashMap();
            userMap.put("username", userName);
            userMap.put("full_name", fullName);
            userMap.put("country", country);
            userMap.put("status", "Hey, using my social media");
            userMap.put("level_of_Study", "none");
            userMap.put("gender", "none");
            userMap.put("birth_date", "none");


            loadingbar = new ProgressDialog(this);
            loadingbar.setTitle("Saving Info");
            loadingbar.setMessage("Creating Your New Account");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            mUserReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfully", Toast.LENGTH_LONG);
                        loadingbar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured \n"+ message, Toast.LENGTH_LONG);
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }
    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
    }
}
