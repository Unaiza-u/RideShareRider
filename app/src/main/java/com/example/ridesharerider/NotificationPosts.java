package com.example.ridesharerider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.ridesharerider.Adapter.NotificationAdapter;
import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.Model.NotificationShareRide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NotificationPosts extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationShareRide> notificationList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_posts);

        recyclerView = findViewById(R.id.NotificationRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(notificationAdapter);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.notification_ride_share);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    notificationList.add(new NotificationShareRide(snapshot.child("id").getValue().toString(), snapshot.child("name").getValue().toString(),
                            snapshot.child("latStart").getValue().toString(), snapshot.child("lngStart").getValue().toString(),snapshot.child("latEnd").getValue().toString(),
                            snapshot.child("lngEnd").getValue().toString(), snapshot.child("startLocation").getValue().toString(), snapshot.child("endLocation").getValue().toString(),
                            snapshot.child("numOfSeats").getValue().toString(),snapshot.child("ratePerSeat").getValue().toString()));
                }

                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            status("online");
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            status("online");
        }
    }
}