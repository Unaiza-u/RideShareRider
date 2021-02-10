package com.example.ridesharerider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.DirectionHelpers.FetchURL;
import com.example.ridesharerider.DirectionHelpers.TaskLoadedCallback;
import com.example.ridesharerider.Model.NotificationShareRide;
import com.example.ridesharerider.Model.Rider;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationPostDetail extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private String userId;
    private GoogleMap mMap;
    Double latStart, lngStart, latEnd, lngEnd;
    private TextView notificationPostDetailRatePerSeat, notificationPostDetailNumOfSeats, notificationPostDetailStartLocation,
            notificationPostDetailEndLocation;
    private SupportMapFragment notificationPostDetailMap;
    private Button btnContactRider;
    private View mapView;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    public static Location mLastKnownLocation = null;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 15;

    //Direction
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    List<MarkerOptions> markerOptionsList = new ArrayList<>();

    private Marker marker1, marker2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_post_detail);

        notificationPostDetailRatePerSeat = findViewById(R.id.notificationPostDetailRatePerSeat);
        notificationPostDetailNumOfSeats = findViewById(R.id.notificationPostDetailNumOfSeats);
        notificationPostDetailStartLocation = findViewById(R.id.notificationPostDetailStartLocation);
        notificationPostDetailEndLocation = findViewById(R.id.notificationPostDetailEndLocation);
        btnContactRider = findViewById(R.id.btnContactRider);

        notificationPostDetailMap = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.notificationPostDetailMap);
        notificationPostDetailMap.getMapAsync(this);
        mapView = notificationPostDetailMap.getView();
        notificationPostDetailRatePerSeat = findViewById(R.id.notificationPostDetailRatePerSeat);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(NotificationPostDetail.this);

        if (getIntent() != null){

            userId = getIntent().getStringExtra("userId");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.notification_ride_share).child(userId);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        NotificationShareRide notificationShareRide = dataSnapshot.getValue(NotificationShareRide.class);

                        latStart = Double.parseDouble(notificationShareRide.getLatStart());
                        lngStart = Double.parseDouble(notificationShareRide.getLngStart());
                        latEnd = Double.parseDouble(notificationShareRide.getLatEnd());
                        lngEnd = Double.parseDouble(notificationShareRide.getLngEnd());

                        notificationPostDetailRatePerSeat.setText(notificationShareRide.getRatePerSeat());
                        notificationPostDetailNumOfSeats.setText(notificationShareRide.getNumOfSeats());
                        notificationPostDetailStartLocation.setText(notificationShareRide.getStartLocation());
                        notificationPostDetailEndLocation.setText(notificationShareRide.getEndLocation());


                        if(marker1 != null || marker2 != null){
                            marker1.remove();
                            marker2.remove();
                            markerOptionsList.clear();
                        }

                        place1 = new MarkerOptions().position(new LatLng(latStart, lngStart)).title("Start").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                        markerOptionsList.add(place1);
                        marker1 = mMap.addMarker(place1);

                        place2 = new MarkerOptions().position(new LatLng(latEnd, lngEnd)).title("End").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                        markerOptionsList.add(place2);
                        marker2 = mMap.addMarker(place2);

                        showAllMarkers();
                        showPolyline();

                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        btnContactRider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationPostDetail.this, MessageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userid",userId);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            layoutParams.setMargins(0, 450, 80, 0);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(NotificationPostDetail.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(NotificationPostDetail.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(NotificationPostDetail.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(NotificationPostDetail.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(NotificationPostDetail.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showAllMarkers() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(MarkerOptions m : markerOptionsList){
            builder.include(m.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }

    private void showPolyline() {
        new FetchURL(NotificationPostDetail.this)
                .execute(getUrl(place1.getPosition(),place2.getPosition(),"driving"),"driving");

    }

    private String getUrl(LatLng origin, LatLng destination, String directionMode) {
        String str_origin = "origin=" + origin.latitude + ","+ origin.longitude;

        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;

        String mode = "mode=" + directionMode;

        String parameter = mode + "&" + str_origin + "&" + str_dest  ;

        String format = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + format + "?"
                + parameter + "&key="+getResources().getString(R.string.google_direction_api);

        Log.d("ERROR",url);

        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline != null)
            currentPolyline.remove();

        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}