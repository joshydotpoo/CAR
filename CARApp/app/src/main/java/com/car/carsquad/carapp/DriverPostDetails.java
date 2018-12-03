package com.car.carsquad.carapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Objects;

public class DriverPostDetails extends AppCompatActivity implements View.OnClickListener{

    private Button mDeletePost;
    private DatabaseReference mRiderRef, mFriendsRef;
    String postID;
    private String riderID;
    private String currentRiderID;
    private String myID;
    RecyclerView riderRequest, riderAccepted;
    String riderFirstName;
    String riderLastName;
    User requestingRider;
    Post acceptedRide;
    ToggleButton showRequested;
    ToggleButton showAccepted;
    TextView seatsAvailable;
    private DatabaseReference databaseCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).hide();
        setContentView(R.layout.activity_driver_post_details);

        seatsAvailable = (TextView) findViewById(R.id.seats_available_text_view);

        mDeletePost = (Button) findViewById(R.id.delete_ride_button);
        mDeletePost.setOnClickListener(this);

        showRequested = (ToggleButton) findViewById(R.id.button_show_requested);
        showRequested.setOnClickListener(this);
        showAccepted = (ToggleButton) findViewById(R.id.button_show_accepted);
        showAccepted.setOnClickListener(this);

        getIncomingIntent();

        mRiderRef = FirebaseDatabase.getInstance().getReference().child("user");
        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("friends");
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseCar = FirebaseDatabase.getInstance().getReference("car");
        //TODO set CAR INFO
        databaseCar.child(myID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seatsAvailable.setText(dataSnapshot.child("numSeats").getValue(String.class) + " seats available");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        //recycler view for user requests
        riderRequest = (RecyclerView) findViewById(R.id.request_list);
        riderRequest.setHasFixedSize(true);
        riderRequest.setLayoutManager(new LinearLayoutManager(this));

        riderAccepted = (RecyclerView) findViewById(R.id.accepted_list);
        riderAccepted.setHasFixedSize(true);
        riderAccepted.setLayoutManager(new LinearLayoutManager(this));

        //showRequestList();

    }
    @Override
    protected void onStart(){
        super.onStart();

        DatabaseReference requestUserRef = FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID);
        FirebaseRecyclerAdapter<User,DriverPostDetails.RequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, DriverPostDetails.RequestViewHolder>
                        (User.class, R.layout.ride_request_cardview, DriverPostDetails.RequestViewHolder.class, requestUserRef){
                    @Override
                    protected void populateViewHolder(DriverPostDetails.RequestViewHolder viewHolder, final User model, int position){
                        String name = model.getFirstName() + " " + model.getLastName();
                        viewHolder.setRiderName(name);

                        /*final String*/ riderID = model.getUserID();

                        //ACCEPT REQUEST
                        viewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //TODO UPDATE RIDER_ID
                                riderID = model.getUserID();

                                //ACCEPT RIDER
                                acceptRider();

                                //REMOVE RIDER FROM REQUESTS (since already accepted)
                                FirebaseDatabase.getInstance().getReference().child("request").child(postID).child(riderID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("request").child(riderID).child(postID).child("request_type")
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID).child(riderID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(riderID).child(postID).removeValue();
                            }
                        });
                        //REJECT REQUEST
                        viewHolder.btnReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                riderID = model.getUserID();
                                //Toast.makeText(DriverPostDetails.this, "reject button Clicked", Toast.LENGTH_LONG).show();

                                //REMOVE USER'S REQUEST IF DRIVER REJECTED
                                //not myID, rather riderID
                                FirebaseDatabase.getInstance().getReference().child("request").child(postID).child(riderID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        FirebaseDatabase.getInstance().getReference().child("request").child(riderID).child(postID).child("request_type")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(DriverPostDetails.this, "rejected successfully", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID).child(riderID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(riderID).child(postID).removeValue();
                            }
                        });
                    }
                };
        riderRequest.setAdapter(firebaseRecyclerAdapter);


        //ACCEPTED RIDERS
        DatabaseReference acceptedUserRef = FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID);
        FirebaseRecyclerAdapter<User,DriverPostDetails.RequestViewHolder> firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<User, DriverPostDetails.RequestViewHolder>
                        (User.class, R.layout.ride_accepted_cardview, DriverPostDetails.RequestViewHolder.class, acceptedUserRef){
                    @Override
                    protected void populateViewHolder(DriverPostDetails.RequestViewHolder viewHolder, final User model, int position){
                        String name = model.getFirstName() + " " + model.getLastName();
                        viewHolder.setRiderName(name);

                        /*final String*/ riderID = model.getUserID();

                        //MESSAGE ACCEPTED RIDER
                        viewHolder.btnMessage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                riderID = model.getUserID();
                                // TODO INITIATE CHAT ROOM
                                Toast.makeText(DriverPostDetails.this, "message button Clicked", Toast.LENGTH_LONG).show();

                            }
                        });
                        //REMOVE ACCEPTED RIDER
                        viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                riderID = model.getUserID();

                                //Toast.makeText(DriverPostDetails.this, "RiderID: "+riderID, Toast.LENGTH_LONG).show();

                                //REMOVE USER'S REQUEST IF DRIVER REJECTED
                                //not myID, rather riderID
                                FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).child(riderID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("accepted").child(riderID).child(postID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        //Toast.makeText(DriverPostDetails.this, "rejected successfully", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(riderID).removeValue();
                                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(riderID).child(postID).removeValue();
                            }
                        });
                    }
                };
        riderAccepted.setAdapter(firebaseRecyclerAdapter2);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Button btnAccept;
        Button btnReject;
        Button btnMessage;
        Button btnRemove;
        String currentID;

        public RequestViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            btnAccept = (Button) itemView.findViewById(R.id.button_accept_request);
            btnReject = (Button) itemView.findViewById(R.id.button_reject_request);

            btnMessage = (Button) itemView.findViewById(R.id.button_message);
            btnRemove = (Button) itemView.findViewById(R.id.button_remove);

            //currentID = currentRiderID;
        }
        public void setRiderName(String name){
            TextView rider_name = (TextView)mView.findViewById(R.id.rider_name);
            rider_name.setText(name);
        }
    }

    private void acceptRider() {

        FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).child(riderID).child("accept_type")
                .setValue("accepted_rider").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                FirebaseDatabase.getInstance().getReference().child("accepted").child(riderID).child(postID).child("accept_type")
                        .setValue("accepted_post").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) { }
                });
            }
        });


        //RETRIEVE RIDER INFO
        FirebaseDatabase.getInstance().getReference().child("users").child(riderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestingRider = dataSnapshot.getValue(User.class);
                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(riderID).setValue(requestingRider);

                //RETRIEVE POST INFO
                FirebaseDatabase.getInstance().getReference().child("post").child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        acceptedRide = dataSnapshot.getValue(Post.class);
                        FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(riderID).child(postID).setValue(acceptedRide);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void getIncomingIntent(){
        if(getIntent().hasExtra("postID") && getIntent().hasExtra("startPt") && getIntent().hasExtra("endPt") &&
                getIntent().hasExtra("date") && getIntent().hasExtra("time") && getIntent().hasExtra("cost") &&
                getIntent().hasExtra("driverID")){
            postID = getIntent().getStringExtra("postID");
            String startPt = getIntent().getStringExtra("startPt");
            String endPt = getIntent().getStringExtra("endPt");
            String date = getIntent().getStringExtra("date");
            String time = getIntent().getStringExtra("time");
            String cost = "$" + getIntent().getStringExtra("cost");
            String driverID = getIntent().getStringExtra("driverID");

            setDetails(postID, startPt, endPt, date, time, cost, driverID);
        }
    }

    private void setDetails(String postID,String startPt,String endPt,String date,String time, String cost,String driverID){
        TextView startTV = (TextView) findViewById(R.id.start_text_view);
        startTV.setText(startPt.toUpperCase());
        TextView endTV = (TextView) findViewById(R.id.end_text_view);
        endTV.setText(endPt.toUpperCase());
        TextView dateTV = (TextView) findViewById(R.id.date_text_view);
        dateTV.setText(date);
        TextView timeTV = (TextView) findViewById(R.id.time_text_view);
        timeTV.setText(time);
        TextView costTV = (TextView) findViewById(R.id.cost_text_view);
        costTV.setText(cost);
    }

    private void deletePost(){
        AlertDialog.Builder builder = new AlertDialog.Builder(DriverPostDetails.this);
        builder.setCancelable(true);
        builder.setTitle("DELETING POST");
        builder.setMessage("Do you want to proceed?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Delete Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //REMOVE ALL RIDERS ASSOCIATED WITH POST
                final DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();

                //TODO STEP 1: POPULATE ARRAY STORING RIDER IDS

                mReference.child("request").child(postID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //TODO ARRAY POPULATE
                        final ArrayList<String> userIdArray = new ArrayList<>();

                        for(DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                            userIdArray.add(idSnapshot.getValue(String.class));
                        }
                        //Toast.makeText(DriverPostDetails.this, "Array empty: " + userIdArray.isEmpty(),Toast.LENGTH_LONG).show();

                        //TODO STEP 2: DELETION LOOP
                        for(String uid : userIdArray){
                            mReference.child("request_obj").child(uid).child(postID).removeValue();
                            mReference.child("request").child(uid).child(postID).removeValue();

                            mReference.child("accepted_obj").child(uid).child(postID).removeValue();
                            mReference.child("accepted").child(uid).child(postID).removeValue();

                            //Toast.makeText(DriverPostDetails.this, uid, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mReference.child("accepted").child(postID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //TODO ARRAY POPULATE
                                final ArrayList<String> userIdArray = new ArrayList<>();

                                for(DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                                    userIdArray.add(idSnapshot.getValue(String.class));
                                    //Toast.makeText(DriverPostDetails.this, "Array empty: " + userIdArray.isEmpty(),Toast.LENGTH_LONG).show();
                                }

                                //Toast.makeText(DriverPostDetails.this, userIdArray.get(0), Toast.LENGTH_SHORT).show();
                                //TODO STEP 2: DELETION LOOP
                                for(String uid : userIdArray){
                                    mReference.child("request_obj").child(uid).child(postID).removeValue();
                                    mReference.child("request").child(uid).child(postID).removeValue();

                                    mReference.child("accepted_obj").child(uid).child(postID).removeValue();
                                    mReference.child("accepted").child(uid).child(postID).removeValue();

                                    //Toast.makeText(DriverPostDetails.this, uid, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                //TODO STEP 3: REMOVE POSTS OKKKKK
                mReference.child("request").child(postID).removeValue();
                mReference.child("request_obj").child(postID).removeValue();
                mReference.child("accepted").child(postID).removeValue();
                mReference.child("accepted_obj").child(postID).removeValue();


                /*
                mReference.child("request").child(postID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mReference.child("request").child(riderID).child(postID).child("request_type")
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {}
                                });
                            }
                        });

                mReference.child("request_obj").child(riderID).child(postID).removeValue();


                FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseDatabase.getInstance().getReference().child("accepted").child(riderID).child(postID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {}
                                        });
                            }
                        });

                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(riderID).child(postID).removeValue();
*/

                //REMOVE POST FROM DATABASE
                DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference().child("post").child(postID);
                dbPost.setValue(null);

                //GO BACK TO DRIVER ACTIVITY
                Intent intent = new Intent(DriverPostDetails.this, DriverActivity.class);
                finish();
                startActivity(intent);
            }
        });
        builder.show();
    }
    @Override
    public void onClick(View view) {
        if(view == mDeletePost) {
            deletePost();
        }
        else if(view == showRequested) {
            toggleRequested();
        }
        else if(view == showAccepted) {
            toggleAccepted();
        }
    }

    private void toggleRequested(){
        if(riderRequest.getVisibility() != View.GONE) {
            riderRequest.setVisibility(View.GONE);
        } else if (riderRequest.getVisibility() == View.GONE){
            riderRequest.setVisibility(View.VISIBLE);
        }
    }
    private void toggleAccepted(){
        if(riderAccepted.getVisibility() != View.GONE) {
            riderAccepted.setVisibility(View.GONE);
        } else if (riderAccepted.getVisibility() == View.GONE) {
            riderAccepted.setVisibility(View.VISIBLE);
        }
    }

}
