package com.car.carsquad.carapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RiderActivity extends AppCompatActivity implements View.OnClickListener {

    //Firebase object
    private FirebaseAuth firebaseAuth;

    //UI references
    private TextView textviewUserEmail;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FloatingActionButton mSearch;

    private RecyclerView mPostList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);

        mSearch = (FloatingActionButton) findViewById(R.id.search_ride_button);
        mSearch.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("post");
        mDatabase.keepSynced(true);
        mPostList = (RecyclerView) findViewById(R.id.rider_post_view);
        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(new LinearLayoutManager(this));

        //initialize fireBase
        firebaseAuth = firebaseAuth.getInstance();

        //if user is not logged in yet
        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        //for the sidebar
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {
                            //logout from menu bar
                            case R.id.nav_logout:
                                logout();
                                break;
                            case R.id.nav_switch_to_driver:
                                AlertDialog.Builder builder = new AlertDialog.Builder(RiderActivity.this);
                                builder.setCancelable(true);
                                builder.setTitle("You are about to enter DRIVER mode");
                                builder.setMessage("Do you wish to proceed?");

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                                builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {


                                        DatabaseReference databaseUser =
                                             FirebaseDatabase.getInstance().getReference("users");
                                        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        //skip if user already signed up as DRIVER
                                        databaseUser.child(userId).child("isDriver").addListenerForSingleValueEvent(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                             String isDriver;
                                             isDriver = dataSnapshot.getValue(String.class);
                                             if (Objects.equals(isDriver, "true")) {
                                                 finish();
                                                 startActivity(new Intent(getApplicationContext(), DriverActivity.class));
                                             } else {
                                                 startActivity(new Intent(RiderActivity.this, DriverProfileActivity.class));
                                             }
                                         }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                });
                                builder.show();
                                break;

                            case R.id.nav_account:
                                startActivity(new Intent(RiderActivity.this, UpdateUserInfoActivity.class));
                        }
                        return true;
                    }
                });
    }

    //search for posts
    private void firebaseSearch(String searchText){
        Query firebaseSearchQuery = mDatabase.orderByChild("startPt")
                .startAt(searchText).endAt(searchText + "/uf8ff");
        FirebaseRecyclerAdapter<Post,RiderActivity.PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, RiderActivity.PostViewHolder>
                        (Post.class, R.layout.post_cardview, RiderActivity.PostViewHolder.class, firebaseSearchQuery){
                    @Override
                    protected void populateViewHolder(RiderActivity.PostViewHolder viewHolder, Post model, int position){
                        viewHolder.setDepLoc(model.getStartPt().toUpperCase());
                        viewHolder.setArrLoc(model.getEndPt().toUpperCase());
                        viewHolder.setDepDate(model.getDate());
                        viewHolder.setDepTime(model.getTime());
                    }
                };
        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Post,RiderActivity.PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, RiderActivity.PostViewHolder>
                        (Post.class, R.layout.post_cardview, RiderActivity.PostViewHolder.class, mDatabase){
                    @Override
                    protected void populateViewHolder(RiderActivity.PostViewHolder viewHolder, Post model, int position){
                        viewHolder.setDepLoc(model.getStartPt().toUpperCase());
                        viewHolder.setArrLoc(model.getEndPt().toUpperCase());
                        viewHolder.setDepDate(model.getDate());
                        viewHolder.setDepTime(model.getTime());
                    }
                };
        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    //for the side bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if(id == R.id.action_settings){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public PostViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setDepLoc(String start){
            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
            post_title.setText("FROM: " +start);
        }
        public void setArrLoc(String dest){
            TextView post_desc = (TextView)mView.findViewById(R.id.post_desc);
            post_desc.setText("TO: " +dest);
        }
        public void setDepDate(String depDate){
            TextView post_dep_date = (TextView)mView.findViewById(R.id.post_dep_date);
            post_dep_date.setText("DEPARTURE DATE: " +depDate);
        }
        public void setDepTime(String depTime){
            TextView post_dep_time = (TextView)mView.findViewById(R.id.post_dep_time);
            post_dep_time.setText("DEPARTURE TIME: " + depTime);
        }
        public void setImage(Context ctx, String image){
            ImageView postImage = (ImageView)mView.findViewById(R.id.post_image);
            //Picasso.with(ctx).load(image).into(postImage);
        }
    }

    //LOGOUT of the app
    private void logout() {
        //sign user out
        firebaseAuth.signOut();
        //end current activity and go back to main screen
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onClick(View view) {
        if(view == mSearch){
            startActivity(new Intent(this, SearchActivity.class));
        }
    }

    //prevent user from pressing the back button to go back from the main app screen
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
