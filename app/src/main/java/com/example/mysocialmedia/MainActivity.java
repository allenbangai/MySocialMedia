package com.example.mysocialmedia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private CircleImageView navProfileImage;
    private TextView navProfilename;
    private ImageButton addNewPostButton;
    private RecyclerView mPostRecycler;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef, mPostRef;
    String currentUserId;
    private FirebaseRecyclerAdapter<PostModule, PostModuleViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.id_add_new_post_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mPostRecycler = findViewById(R.id.id_all_student_post_list);
        mPostRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mPostRecycler.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        if (mCurrentUser != null) {
            currentUserId = mAuth.getUid();
            Toast.makeText(MainActivity.this, currentUserId, Toast.LENGTH_SHORT).show();
        }
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mPostRef = FirebaseDatabase.getInstance().getReference().child("Post").child(currentUserId);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        addNewPostButton = findViewById(R.id.id_add_new_post_button);

        View navView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        navProfileImage = navView.findViewById(R.id.id_imageHeaderView);
        navProfilename = navView.findViewById(R.id.id_textHeaderView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        mUserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("full_name")) {
                        String name = dataSnapshot.child("full_name").getValue().toString();
                        Toast.makeText(MainActivity.this, "Your full name is"+ name, Toast.LENGTH_SHORT).show();
                        navProfilename.setText(name);
                    }
                    if (dataSnapshot.hasChild("profileImage")) {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);
                        Toast.makeText(MainActivity.this, "profile url is \n\n" + image, Toast.LENGTH_SHORT);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //method to display users updated post
        displayAllUsersPost();
    }

    private void displayAllUsersPost() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<PostModule>().setQuery(mPostRef, PostModule.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostModule, PostModuleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostModuleViewHolder holder, final int position, @NonNull PostModule model) {
                final String postKey = getRef(position).getKey();

                holder.setfullName(model.getFullName());
                holder.setProfileImage(model.getProfileImage());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setPostImage(model.getPostImage());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });
            }

            @NonNull
            @Override
            public PostModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts, parent, false);
                PostModuleViewHolder postModuleViewHolder = new PostModuleViewHolder(view);
                return postModuleViewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        mPostRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostModuleViewHolder extends RecyclerView.ViewHolder{

        View mView;
        private TextView username;
        private CircleImageView image;
        private TextView postTime;
        private TextView postDate;
        private TextView postDescription;
        private ImageView imagePostView;

        public PostModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setfullName(String fullname){
            username = mView.findViewById(R.id.id_post_username);
            username.setText(fullname);
        }

        public void setProfileImage(String profileImage){
            image = mView.findViewById(R.id.id_post_profileImage);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(image);
        }

        public void setTime(String time){
            postTime = mView.findViewById(R.id.id_post_time);
            postTime.setText(time);
        }

        public void setDate(String date){
            postDate = mView.findViewById(R.id.id_post_date);
            postDate.setText(date);
        }

        public void setDescription(String description){
            postDescription = mView.findViewById(R.id.id_post_description);
            postDescription.setText(description);
        }

        @SuppressLint("ResourceType")
        public void setPostImage(String postImage){
            imagePostView = mView.findViewById(R.id.id_post_image);
            Picasso.get().load(postImage).placeholder(R.drawable.select_image).into(imagePostView);
        }
    }

    private void sendUserToPostActivity() {
        startActivity(new Intent(MainActivity.this, PostActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        if(mCurrentUser == null) {
            sendUserToLogInActivity();
        }else{
            checkUserExperience();
        }

        if(firebaseRecyclerAdapter == null){
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseRecyclerAdapter != null){
            firebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(firebaseRecyclerAdapter == null){
            firebaseRecyclerAdapter.startListening();
        }
    }

    private void checkUserExperience() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupActivityIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivityIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_add_post) {
            sendUserToPostActivity();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_find_friends) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if(id == R.id.nav_logout){
            mAuth.signOut();
            sendUserToLogInActivity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendUserToLogInActivity() {
        Intent loginActivityIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivityIntent);
        finish();
    }
}
