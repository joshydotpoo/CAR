package com.car.carsquad.carapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Objects;

public class DriverPostDetails extends AppCompatActivity implements View.OnClickListener{

    private Button mDeletePost;
    private DatabaseReference mRequestDatabase, mRiderRef, mFriendsRef;
    String postID;
    private String myID;
    RecyclerView riderRequest, riderAccepted;
    String riderFirstName;
    String riderLastName;
    User requestingRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_post_details);
        mDeletePost = (Button) findViewById(R.id.delete_ride_button);
        mDeletePost.setOnClickListener(this);

        getIncomingIntent();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("request").child(postID);
        mRiderRef = FirebaseDatabase.getInstance().getReference().child("user");
        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("friends");
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
                        (User.class, R.layout.ride_request_cardview, DriverPostDetails.RequestViewHolder.class, requestUserRef/*mRequestDatabase*/){
                    @Override
                    protected void populateViewHolder(DriverPostDetails.RequestViewHolder viewHolder, final User model, int position){
                        String name = model.getFirstName() + " " + model.getLastName();
                        viewHolder.setRiderName(name);
                        final String riderID = model.getUserID();

                        //ACCEPT REQUEST
                        viewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                //Toast.makeText(DriverPostDetails.this, "Accept button Clicked", Toast.LENGTH_LONG).show();

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
                                                        Toast.makeText(DriverPostDetails.this, "rejected successfully", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("request_obj").child(postID).child(riderID).removeValue();
                            }
                        });
                        //REJECT REQUEST
                        viewHolder.btnReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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
                            }
                        });
                    }
                };
        riderRequest.setAdapter(firebaseRecyclerAdapter);


        //ACCEPTED RIDERS
        DatabaseReference acceptedUserRef = FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID);
        FirebaseRecyclerAdapter<User,DriverPostDetails.RequestViewHolder> firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<User, DriverPostDetails.RequestViewHolder>
                        (User.class, R.layout.ride_accepted_cardview, DriverPostDetails.RequestViewHolder.class, acceptedUserRef/*mRequestDatabase*/){
                    @Override
                    protected void populateViewHolder(DriverPostDetails.RequestViewHolder viewHolder, final User model, int position){
                        String name = model.getFirstName() + " " + model.getLastName();
                        viewHolder.setRiderName(name);
                        final String riderID = model.getUserID();

                        //ACCEPT REQUEST
                        viewHolder.btnMessage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                Toast.makeText(DriverPostDetails.this, "message button Clicked", Toast.LENGTH_LONG).show();


                            }
                        });
                        //REJECT REQUEST
                        viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(DriverPostDetails.this, "reject button Clicked", Toast.LENGTH_LONG).show();

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
                                                        Toast.makeText(DriverPostDetails.this, "rejected successfully", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(riderID).removeValue();
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

        //private Button viewRideButton;
        public RequestViewHolder(View itemView){
            super(itemView);
            mView = itemView;

            btnAccept = (Button) itemView.findViewById(R.id.button_accept_request);
            btnReject = (Button) itemView.findViewById(R.id.button_reject_request);

            btnMessage = (Button) itemView.findViewById(R.id.button_message);
            btnRemove = (Button) itemView.findViewById(R.id.button_remove);
        }
        public void setRiderName(String name){
            TextView rider_name = (TextView)mView.findViewById(R.id.rider_name);
            rider_name.setText(name);
        }
    }

    private void acceptRider() {

        //TODO DEBUG
        Toast.makeText(DriverPostDetails.this, "ACCEPTED TO FIREBASE", Toast.LENGTH_LONG).show();

        FirebaseDatabase.getInstance().getReference().child("accepted").child(postID).child(myID).child("accept_type")
                .setValue("accepted_rider").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                FirebaseDatabase.getInstance().getReference().child("accepted").child(myID).child(postID).child("accept_type")
                        .setValue("accepted_post").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) { }
                });
            }
        });

        mRiderRef.child(myID).child("lastName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                riderLastName = dataSnapshot.getValue(String.class);
                requestingRider = new User(myID, riderFirstName, riderLastName, "","",0.0);
                FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(myID).setValue(requestingRider);

                FirebaseDatabase.getInstance().getReference().child(myID).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        riderFirstName = dataSnapshot.getValue(String.class);
                        requestingRider.setFirstName(riderFirstName);
                        FirebaseDatabase.getInstance().getReference().child("accepted_obj").child(postID).child(myID).setValue(requestingRider);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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

                DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference().child("request").child(postID);
                dbRequest.setValue(null);

                DatabaseReference dbPost = FirebaseDatabase.getInstance().getReference().child("post").child(postID);
                dbPost.setValue(null);

                Intent intent = new Intent(DriverPostDetails.this, DriverActivity.class);
                finish();
                startActivity(intent);
            }
        });
        builder.show();
    }
    @Override
    public void onClick(View view) {
        deletePost();
    }

}
