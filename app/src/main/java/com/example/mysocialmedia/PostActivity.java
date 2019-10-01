package com.example.mysocialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.content.pm.PackageManager.PERMISSION_DENIED;


public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText postdescribtion;
    private ImageButton selectPostImage;
    private Button updatePostButton;
    // loadingbar from class ProgressDialog to be changed to ProgressBar
    private ProgressDialog loadingbar;

    private String description;
    private String saveCurrrentTime = "void";
    private String saveCurrentDate = "void";
    private String postRandomName;
    private String imageUriToString;
    private String currentUserId;
    private String userFullName;
    private String userProfileImage;
    private final static int gallery_Prick = 1;
    private final static int PERMISSION_CODE_01 = 1001;
    private final static int PERMISSION_CODE_02 = 1002;
    private Uri imageUri;

    private Calendar callForDate, callForTime;
    private SimpleDateFormat currentDate, currentTime;
    private StorageReference mPostImageStorageRef;
    private String downloadUri;
    private DatabaseReference mUserRef, mUserPost;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostImageStorageRef = FirebaseStorage.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserPost = FirebaseDatabase.getInstance().getReference().child("Post");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser != null){
            currentUserId = mCurrentUser.getUid();
        }
        mToolbar = findViewById(R.id.id_update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postdescribtion = findViewById(R.id.id_postActivity_EditText);
        selectPostImage = findViewById(R.id.id_postActivity_ImageButton);
        updatePostButton = findViewById(R.id.id_postActivity_Button);

        //get user to select image from gallery and post it
        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                }else{
                    //System O.S is less than Marshmallow
                    Toast.makeText(PostActivity.this, "System O.S is less than Marshmallow", Toast.LENGTH_SHORT).show();
                }

                //got to gallery method
                openGallery();
            }
        });

        //update post status
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });
    }

    private void validatePostInfo() {
        description = postdescribtion.getText().toString();
        if(imageUri == null){
            Toast.makeText(PostActivity.this, " Pls select an image to post", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(PostActivity.this, "Pls write something", Toast.LENGTH_SHORT).show();
        }else{
            storeDataToFirebase();
        }
    }

    private void storeDataToFirebase() {
        // loadingbar from class ProgressDialog to be changed to ProgressBar
        loadingbar = new ProgressDialog(this);
        loadingbar.setTitle("Saving Info");
        loadingbar.setMessage("Updating your post");
        loadingbar.show();
        loadingbar.setCanceledOnTouchOutside(true);
        //function that store the data entered in the postActivity to firebase

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            callForDate = Calendar.getInstance();
            currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            callForTime = Calendar.getInstance();
            currentTime = new SimpleDateFormat("HH:mm");
            saveCurrrentTime = currentTime.format(callForTime.getTime());
        }
        imageUriToString = imageUri.getLastPathSegment();
        // Hence concatinating saveCurrentDate, saveCurrentTime
        // and imageUriToString to have the child path
        // of "Post Image" of each user in postRandomName
        postRandomName = saveCurrentDate + saveCurrrentTime + imageUriToString;

        StorageReference filePath = mPostImageStorageRef.child("Post Images").child(currentUserId).child(postRandomName + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    downloadUri = task.getResult().getUploadSessionUri().toString();
                    savingPostInfoToDatabase();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }
                else{
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occurred \n" + errorMessage, Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }
        });
    }

    private void savingPostInfoToDatabase() {
        mUserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userFullName = dataSnapshot.child("full_name").getValue().toString();
                    userProfileImage = dataSnapshot.child("profileImage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUserId);
                    postMap.put("time", saveCurrrentTime);
                    postMap.put("day", saveCurrentDate);
                    postMap.put("description", description);
                    postMap.put("postImage", downloadUri);
                    postMap.put("fullName", userFullName);
                    postMap.put("profileImage", userProfileImage);
                    mUserPost.child(currentUserId).child(postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(PostActivity.this, "New Post Updated", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }else{
                                Toast.makeText(PostActivity.this, "Error occurred while updating your post \n Try again", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openGallery() {
        /*
        *                  Issue 01
        * pls write a code that disables tint at runtime so that the tint of the
        * image view can be enable and disable when imageView is touched
        *
        *                  Issue 02
        * pls write a code to change the layout_width of selectPostImage when the
        * selectPostImage(Image View) is being selected from 350dp to wrap_content
        * --In short, change the layout_width of the ImageView in activity_post.xmt to wrap_content--
        *  */

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, gallery_Prick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_Prick && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(PostActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}
