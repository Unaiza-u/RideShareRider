package com.example.ridesharerider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.Model.DataMessage;
import com.example.ridesharerider.Model.FCMResponse;
import com.example.ridesharerider.Model.Rider;
import com.example.ridesharerider.Model.Token;
import com.example.ridesharerider.Remote.IFCMService;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallDriver extends AppCompatActivity {

    CircleImageView avatar_image;
    TextView txt_name, txt_phone, txt_rate;
    Button btn_call_driver;
    //Button btn_call_driver_phone;

    String driverId;
    Location mLastLocation;
    double lat_start, lng_start, lat_end, lng_end;

    IFCMService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        mService = Common.getFCMService();

        //init view
        avatar_image = findViewById(R.id.avatar_image);
        txt_name = findViewById(R.id.txt_name);
        txt_phone = findViewById(R.id.txt_phone);
        txt_rate = findViewById(R.id.txt_rate);
        btn_call_driver = findViewById(R.id.btn_call_driver);
        //btn_call_driver_phone = findViewById(R.id.btn_call_driver_phone);

        btn_call_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Here we will use function sendNotificationToDriver from Welcome activity
                if (driverId != null && !driverId.isEmpty()) {
                    sendRequestToDriver(driverId);
                    Welcome.btnFind.setEnabled(false);
                }
            }
        });

//        btn_call_driver_phone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_CALL);
//                intent.setData(Uri.parse("tel:"+txt_phone.getText().toString()));
//                startActivity(intent);
//            }
//        });

        if(getIntent() != null){
            driverId = getIntent().getStringExtra("driverId");
            lat_start = getIntent().getDoubleExtra("lat_start", -1.0);
            lng_start = getIntent().getDoubleExtra("lng_start", -1.0);

            lat_end = getIntent().getDoubleExtra("lat_end", -1.0);
            lng_end = getIntent().getDoubleExtra("lng_end", -1.0);
            
            loadDriverInfo(driverId);
        }
    }

    private void loadDriverInfo(String driverId) {

        FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl)
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Rider driverUser = dataSnapshot.getValue(Rider.class);

                        Picasso.get()
                                .load(R.drawable.car)
                                .into(avatar_image);

                        Log.d("CALL",driverUser.getName());
                        Log.d("CALL",driverUser.getPhone());
                        Log.d("CALL",driverUser.getRate());

                        txt_name.setText(driverUser.getName());
                        txt_phone.setText(driverUser.getPhone());
                        txt_rate.setText(driverUser.getRate());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        Query query = tokens.orderByKey().equalTo(driverId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()){
                        Token token = postSnapShot.getValue(Token.class); // get Token object from database with key

                        //Make raw payload - convert LatLng to json
                        String json_lat_lng = new Gson().toJson(new LatLng(lat_start, lng_start));
                        String dest_lat_lng = new Gson().toJson(new LatLng(lat_end, lng_end));
                        String riderToken = FirebaseInstanceId.getInstance().getToken();
//                        Data data = new Data(riderToken , json_lat_lng, dest_lat_lng); // send it to driver app and we will deserialize it again
//                        Sender content = new Sender(token.getToken(),data); // send this data to token

                        Map<String, String> content = new HashMap<>();
                        content.put("title", riderToken);
                        content.put("message", json_lat_lng);
                        content.put("desti", dest_lat_lng);
                        DataMessage dataMessage = new DataMessage(token.getToken(), content);

                        mService.sendMessage(dataMessage)
                                .enqueue(new Callback<FCMResponse>() {
                                    @Override
                                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                        if(response.body().success == 1) {
                                            Toast.makeText(CallDriver.this, "Calling... "+response.message(), Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(CallDriver.this, "Failed !", Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                                        Log.e("ERROR",t.getMessage());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}