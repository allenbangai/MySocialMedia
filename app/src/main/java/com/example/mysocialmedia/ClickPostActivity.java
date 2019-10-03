package com.example.mysocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView clickImageView;
    private TextView clickTextDescription;
    private Button clickEditButton, clickDeleteButton;
    String postkey, currentUserId;
    private FirebaseAuth mAuth;
    FirebaseUser mCurrrentUser;
    private DatabaseReference clickPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        clickImageView = findViewById(R.id.id_edit_post_imageView);
        clickTextDescription = findViewById(R.id.id_edit_post_description);
        clickEditButton = findViewById(R.id.id_edit_post_button);
        clickDeleteButton = findViewById(R.id.id_delete_post_button);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        postkey = getIntent().getExtras().get("PostKey").toString();
        Toast.makeText(ClickPostActivity.this, "popstKey is \n"+ postkey, Toast.LENGTH_LONG).show();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Post").child(currentUserId).child(postkey);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("description")){
                    String editDescription = dataSnapshot.child("description").getValue().toString();
                    clickTextDescription.setText(editDescription);
                }
                if(dataSnapshot.hasChild("postImage")){
                    String editPostImage = dataSnapshot.child("postImage").getValue().toString();
                    Picasso.get().load(editPostImage).placeholder(R.drawable.select_image).into(clickImageView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainAcitivyt();
    }

    private void sendUserToMainAcitivyt(){
        startActivity(new Intent(ClickPostActivity.this, MainActivity.class));
        finish();
    }
}
