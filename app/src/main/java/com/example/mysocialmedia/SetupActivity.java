package com.example.mysocialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SetupActivity extends AppCompatActivity {

    private EditText setupUsername, setupFullName, setupCountryName;
    private Button setupSaveInfo;
    private CircleImageView setupProfieImage;

    // loadingbar from class ProgressDialog to be changed to ProgressBar
    private ProgressDialog loadingbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;
    // Create a storage reference from our app
    private StorageReference mUserProfileImage;

    String currentUserId;
    private final static int gallery_Prick = 1;
    private final static int PERMISSION_CODE_01 = 1001;
    private final static int PERMISSION_CODE_02 = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mUserProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");

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
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                //if statement to check wheather storage permission has being granted
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_DENIED){
                        //permission not granted, request it
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show popup for runtime permission request
                        requestPermissions(permissions, PERMISSION_CODE_01);
                    }
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_DENIED){
                        //permission not granted, request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup for runtime permission request
                        requestPermissions(permissions, PERMISSION_CODE_02);
                    }
                    //permission already granted
                    /*
                    *Remember to remove the final from the resultUri in other to use a reference variable and assign it it the resultUri
                    * so that we can directly put the image in the userProfileImage after asking storage permission
                    **/
                }else{
                    //System O.S is less than Marshmallow
                    Toast.makeText(SetupActivity.this, "System O.S is less than Marshmallow", Toast.LENGTH_SHORT).show();
                }

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallery_Prick);
            }
        });

        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileImage")){
                        //getting string url of the image just uploaded after cropping
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Toast.makeText(SetupActivity.this, image+"\n\n message", Toast.LENGTH_SHORT).show();

                        //Note that Picasso is not deprecated, I annotated it as deprecated as a mistake
                        //loading the square image uploaded to firebase storage as profile image
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(setupProfieImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE_01:
            {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Toast.makeText(SetupActivity.this, "You granted READ storage permission to this app", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SetupActivity.this, "Storage Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }

            case PERMISSION_CODE_02:
            {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Toast.makeText(SetupActivity.this, "You granted WRITE storage permission to this app", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SetupActivity.this, "Storage Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_Prick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();


            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1).start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                // loadingbar from class ProgressDialog to be changed to ProgressBar
                loadingbar = new ProgressDialog(this);
                loadingbar.setTitle("Saving Info");
                loadingbar.setMessage("Updating your Profile Image");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);

                final Uri resultUri = result.getUri();

                StorageReference filePath = mUserProfileImage.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Profile Image Successfully Uploaded ", Toast.LENGTH_LONG);
                            final String downloadaUrl = task.getResult().getUploadSessionUri().toString();

                            mUserReference.child("profileImage").setValue(downloadaUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @SuppressLint("ShowToast")
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(SetupActivity.this, SetupActivity.class);
                                        startActivity(intent);

                                        //mUserProfileImage using bitmap;
                                        /*try {
                                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                                            setupProfieImage.setImageBitmap(bitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/
                                        //mUserProfileImage using setImageURI;
                                        setupProfieImage.setImageURI(resultUri);
                                        Toast.makeText(SetupActivity.this, "Profile Image Succefully saved", Toast.LENGTH_LONG);
                                        loadingbar.dismiss();
                                    }
                                    else{
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error Message \n" + errorMessage, Toast.LENGTH_LONG);
                                        loadingbar.dismiss();
                                    }
                                }
                            });
                        }
                        else{
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error Message \n" + errorMessage, Toast.LENGTH_LONG);
                        }
                    }
                });

            }
            else{
                Toast.makeText(SetupActivity.this, "Error Message \n Image can't be cropped", Toast.LENGTH_LONG);
            }
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
            // loadingbar from class ProgressDialog to be changed to ProgressBar
            loadingbar = new ProgressDialog(this);
            loadingbar.setTitle("Saving Info");
            loadingbar.setMessage("Creating Your New Account");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", userName);
            userMap.put("full_name", fullName);
            userMap.put("country", country);
            userMap.put("status", "Hey, using my social media");
            userMap.put("level_of_Study", "none");
            userMap.put("gender", "none");
            userMap.put("birth_date", "none");
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
        finish();
    }
}
