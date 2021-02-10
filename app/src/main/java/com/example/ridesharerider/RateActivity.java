package com.example.ridesharerider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.Model.Rate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateActivity extends AppCompatActivity {

    Button btnSubmit;
    MaterialRatingBar ratingBar;
    MaterialEditText edtComment;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef, driverInformationRef;

    double ratingStarts = 0.0;

    String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        //init firebase
        database = FirebaseDatabase.getInstance();
        rateDetailRef = database.getReference(Common.rate_detail_tbl);
        driverInformationRef = database.getReference(Common.user_driver_tbl);

        //intit view
        btnSubmit = findViewById(R.id.btnSubmit);
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtComment);

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStarts = rating;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRateDetails(customerId);
            }
        });

         if(getIntent() != null){
             customerId = getIntent().getStringExtra("customerId");
         }
    }

    private void submitRateDetails(final String driverId) {
        final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(RateActivity.this)
                .build();
        waitingDialog.show();

        final Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStarts));
        rate.setComments(edtComment.getText().toString());
        
        //update new value to Firebase
        rateDetailRef.child(driverId)
                .push() // can unique key
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //if upload succeed on firebase, just calculate average and update to driver information
                        rateDetailRef.child(driverId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        double averageStars = 0.0;
                                        int count = 0;
                                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                            Rate rateNew = postSnapshot.getValue(Rate.class);
                                            averageStars+=Double.parseDouble(rateNew.getRates());
                                            count++;
                                        }
                                        double finalAverage = averageStars/count;
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        String valueUpdate = df.format(finalAverage);

                                        //create object update
                                        Map<String, Object> driverUpdateRate = new HashMap<>();
                                        driverUpdateRate.put("rate", valueUpdate);
                                        driverInformationRef.child(driverId)
                                                .updateChildren(driverUpdateRate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        waitingDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "Thank you for submit rate.", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(RateActivity.this, Welcome.class));
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                waitingDialog.dismiss();
                                                Toast.makeText(RateActivity.this, "Rate update but can't write to Driver Information", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RateActivity.this, "Rate failed!", Toast.LENGTH_SHORT).show();
            }
        });
        
    }
}