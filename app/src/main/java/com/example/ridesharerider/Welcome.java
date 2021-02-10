package com.example.ridesharerider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.Direction.DirectionsJSONParser;
import com.example.ridesharerider.DirectionHelpers.FetchURL;
import com.example.ridesharerider.DirectionHelpers.TaskLoadedCallback;
import com.example.ridesharerider.Helper.CustomInfoWindow;
import com.example.ridesharerider.Model.DataMessage;
import com.example.ridesharerider.Model.FCMResponse;
import com.example.ridesharerider.Model.NotificationShareRide;
import com.example.ridesharerider.Model.Rider;
import com.example.ridesharerider.Model.Token;
import com.example.ridesharerider.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;


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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Welcome extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, PopupMenu.OnMenuItemClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    List<Marker> mMarkers = new ArrayList<Marker>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;

    private MaterialSearchBar materialSearchBarStart, materialSearchBarDestination;
    private View mapView;
    public static Button btnFind, btnShareRide;
    //private RippleBackground rippleBg;

    private LatLng latLngOfPlaceStart, latLngOfPlaceDestination;

    GeoFire geoFire;
    //ImageView rip;

    private final float DEFAULT_ZOOM = 15;

    SupportMapFragment mapFragment;

    //Direction
    private MarkerOptions place1, place2, car;
    private Polyline currentPolyline;

    List<MarkerOptions> markerOptionsList = new ArrayList<>();

    private Marker marker1, marker2, markerCar;

    //Bottomsheet
    ImageView imgExpandable, btnMenu;
    BottomSheetRiderFragment mBottomSheet;


    int radius = 1; //1km
    int distance = 1; //3km
    private static final int LIMIT = 3;

    //send Alert
    IFCMService mService;

    //presense system
    DatabaseReference driverAvailable, notificationRideShare;

    String startingAddress = "", destinationAddress = "";

    boolean reject = false;

    boolean allowDriverDisplay;

    String startAddressLocation, endAddressLocation, riderName;
    private FirebaseDatabase db;

    int childrenCount = 0;

    String driverLat, driverLng;

    public static List<String> notificationList = new ArrayList<>();

    boolean showBottom = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mService = Common.getFCMService();

        materialSearchBarStart = findViewById(R.id.searchBar_starting);
        materialSearchBarDestination = findViewById(R.id.searchBar_destination);
        btnFind = findViewById(R.id.btn_find_user);
        btnShareRide = findViewById(R.id.btn_share_ride);
        btnMenu = findViewById(R.id.btn_menu);
        imgExpandable = findViewById(R.id.imgExpandable);
//        rippleBg = findViewById(R.id.ripple_bg);
//        rip = findViewById(R.id.rip);

        db = FirebaseDatabase.getInstance();
        notificationRideShare = db.getReference(Common.notification_ride_share);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

//        if(isDriverFound){
//            btnFind.setText("Call Driver");
//        } else {
//            btnFind.setText("Find Driver");
//        }

        FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            btnFind.setText("Call Driver");
                        }else {
                            btnFind.setText("Find Driver");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//        if(Common.againLoad){
//            mMap.clear();
//
//            markerOptionsList.clear();
//            if(car != null && Common.isAccepted){
//                car = null;
//            }
//
//            if(mLastKnownLocation != null) {
//
//
//
//                place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
//                markerOptionsList.add(place1);
//                marker1 = mMap.addMarker(place1);
//                loadAllAvaliableDriver();
//
//            }
//
//            if(latLngOfPlaceDestination != null){
//                place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
//                markerOptionsList.add(place2);
//                marker2 = mMap.addMarker(place2);
//
//                showAllMarkers();
//            }
//            Common.againLoad = false;
//        }

//        if(mLastKnownLocation != null){
            //presense system
            driverAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
            driverAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                        if(!Common.isAccepted) {
                            //if have any change from Driver table, we will reload aal driver available

                            childrenCount = (int) dataSnapshot.getChildrenCount();
                    if (mLastKnownLocation != null) {
                        loadAllAvaliableDriver();
                    }
                            //Toast.makeText(Welcome.this, "test", Toast.LENGTH_SHORT).show();
//                    if(markerCar != null ){
//                        markerCar.remove();
//                        markerOptionsList.clear();
//                        loadAllAvaliableDriver();
//                            mMap.clear();
//
//                            markerOptionsList.clear();
//                            if (car != null) {
//                                car = null;
//                            }
//
//                            if (mLastKnownLocation != null) {
//
//
//                                place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
//                                markerOptionsList.add(place1);
//                                marker1 = mMap.addMarker(place1);
//
//                                loadAllAvaliableDriver();
//                            }
//
//                            if (latLngOfPlaceDestination != null) {
//                                place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
//                                markerOptionsList.add(place2);
//                                marker2 = mMap.addMarker(place2);
//
//                                showAllMarkers();
//                            }
//                        }else {
//                            childrenCount = (int) dataSnapshot.getChildrenCount();
//                            Toast.makeText(Welcome.this, "test", Toast.LENGTH_SHORT).show();
////                    if(markerCar != null ){
////                        markerCar.remove();
////                        markerOptionsList.clear();
////                        loadAllAvaliableDriver();
//                            mMap.clear();
//
//                            markerOptionsList.clear();
//                            if (car != null) {
//                                car = null;
//                                car = new MarkerOptions().position(new LatLng(Double.parseDouble(driverLat), Double.parseDouble(driverLng))).flat(true)
//                                        .title(riderName)
//                                        .snippet(""+dataSnapshot.getKey())
//                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//                                markerOptionsList.add(car);
//                                mMap.addMarker(car);
//                            }
//
//                            if (mLastKnownLocation != null) {
//
//
//                                place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
//                                markerOptionsList.add(place1);
//                                marker1 = mMap.addMarker(place1);
//                            }
//
//                            if (latLngOfPlaceDestination != null) {
//                                place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
//                                markerOptionsList.add(place2);
//                                marker2 = mMap.addMarker(place2);
//
//                                showAllMarkers();
//                            }
//                        }




//                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        //}

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Welcome.this);
        Places.initialize(Welcome.this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBarStart.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer

                    Toast.makeText(Welcome.this, "BUTTON_NAVIGATION", Toast.LENGTH_SHORT).show();
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBarStart.disableSearch();
                    materialSearchBarStart.clearSuggestions();
                }
            }
        });

        materialSearchBarStart.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("pk")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBarStart.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBarStart.isSuggestionsVisible()) {
                                    materialSearchBarStart.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBarStart.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBarStart.getLastSuggestions().get(position).toString();
                materialSearchBarStart.setText(suggestion);
                startingAddress = suggestion;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBarStart.clearSuggestions();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBarStart.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("LAPTOP", "Place found: " + place.getName());
                        //startingAddress = place.getName();

                        latLngOfPlaceStart = place.getLatLng();
                        showBottom = false;
                        Log.d("LAPTOP", String.valueOf(latLngOfPlaceStart));
                        if (latLngOfPlaceStart != null) {
                            mLastKnownLocation.setLatitude(latLngOfPlaceStart.latitude);
                            mLastKnownLocation.setLongitude(latLngOfPlaceStart.longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlaceStart, DEFAULT_ZOOM));
//                            if(markerOptionsList != null){
//                                marker1.remove();
//                                marker2.remove();
//                                markerOptionsList.clear();
//                            }

                            if(marker1 != null){
                                marker1.remove();
                                markerOptionsList.remove(0);
                            }

                            if(currentPolyline != null)
                                currentPolyline.remove();

                            place1 = new MarkerOptions().position(new LatLng(latLngOfPlaceStart.latitude, latLngOfPlaceStart.longitude)).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                            markerOptionsList.add(place1);
                            marker1 = mMap.addMarker(place1);
                            if(marker2 != null) {
                                showAllMarkers();
                                loadAllAvaliableDriver();
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });



        /////////////////////////

        materialSearchBarDestination.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    //materialSearchBarDestination.disableSearch();
                    materialSearchBarDestination.disableSearch();
                    materialSearchBarDestination.clearSuggestions();
                }
            }
        });

        materialSearchBarDestination.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("pk")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBarDestination.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBarDestination.isSuggestionsVisible()) {
                                    materialSearchBarDestination.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBarDestination.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBarDestination.getLastSuggestions().get(position).toString();
                materialSearchBarDestination.setText(suggestion);
                destinationAddress = suggestion;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBarDestination.clearSuggestions();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBarDestination.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("mytag", "Place found: " + place.getName());
                        //destinationAddress = place.getName();
                        latLngOfPlaceDestination = place.getLatLng();
                        Log.d("LAPTOP", String.valueOf(latLngOfPlaceDestination));
                        if (latLngOfPlaceDestination != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlaceDestination, DEFAULT_ZOOM));
//                            if(markerOptionsList != null){
//                                marker1.remove();
//                                marker2.remove();
//                                markerOptionsList.clear();
//                            }


                                if(marker2 != null){
                                    marker2.remove();
                                }

                            if(currentPolyline != null)
                                currentPolyline.remove();


                            place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                            markerOptionsList.add(place2);
                            marker2 = mMap.addMarker(place2);

                            showAllMarkers();
                            loadAllAvaliableDriver();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });



        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!Common.isDriverFound){
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    //Toast.makeText(Welcome.this, "req:"+Common.driverId, Toast.LENGTH_SHORT).show();
                } else {
                    sendRequestToDriver(Common.driverId);
                    Toast.makeText(Welcome.this, "Calling", Toast.LENGTH_SHORT).show();
                    btnFind.setEnabled(false);
                    //Toast.makeText(Welcome.this, "send:"+Common.driverId, Toast.LENGTH_SHORT).show();
                }

//                ////////////////      for rquesting to pick up marker
//                requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                /////////////////

                //////////////////
//                this is for the locating and polyline
//                new FetchURL(Welcome.this)
//                        .execute(getUrl(place1.getPosition(),place2.getPosition(),"driving"),"driving");
                /////////////////

//                LatLng currentMarkerLocation = mMap.getCameraPosition().target;
//                rippleBg.startRippleAnimation();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        rippleBg.stopRippleAnimation();
//                        //startActivity(new Intent(Welcome.this, TempActivity.class));
//                        //finish();
//                    }
//                }, 3000);

            }
        });

        btnShareRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Common.isAccepted){

                    showRateAndSeatDialog();
                    btnShareRide.setVisibility(View.GONE);

                }
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Welcome.this, v);
                popupMenu.setOnMenuItemClickListener(Welcome.this);
                popupMenu.inflate(R.menu.menu_welcome);
                popupMenu.show();
            }
        });

//        if(currentPolyline == null){
//            btnFind.setVisibility(View.GONE);
//        } else {
//            btnFind.setVisibility(View.VISIBLE);
//        }

//        if (Common.isAccepted){
//            btnFind.setVisibility(View.GONE);
//            btnShareRide.setVisibility(View.VISIBLE);
//        }

//        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(boolean isOnline) {
//                if(isOnline){
//                    rip.setVisibility(View.VISIBLE);
//                    mMap.setMyLocationEnabled(true);
//                    getDeviceLocation();
//                    Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_SHORT)
//                            .show();
//                } else {
////                    if(mCurrent != null)
////                        mCurrent.remove();
//                    mMap.clear();
//                    rip.setVisibility(View.GONE);
//                    mMap.setMyLocationEnabled(false);
//                    Snackbar.make(mapFragment.getView(), "You are offline", Snackbar.LENGTH_SHORT)
//                            .show();
//                }
//
//            }
//        });

        //Geofire to store data
//        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl));

//        mBottomSheet = BottomSheetRiderFragment.newInstance(startingAddress, destinationAddress);
//        imgExpandable.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
//            }
//        });
        updateFirebaseToken();
    }


    private void showRateAndSeatDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Post");
        dialog.setMessage("Please post your notification");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_notification_share_ride, null);

        final MaterialEditText num_of_seats = login_layout.findViewById(R.id.num_of_seats);
        final MaterialEditText rate_per_seat = login_layout.findViewById(R.id.rate_per_seat);


        dialog.setView(login_layout);

        dialog.setPositiveButton("POST", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                if(TextUtils.isEmpty(num_of_seats.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter no of seats available", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(rate_per_seat.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter rate per seat", Toast.LENGTH_SHORT).show();
                    return;
                }

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for Posting")
                        .setCancelable(false)
                        .setContext(Welcome.this)
                        .build();
                waitingDialog.show();

                NotificationShareRide notificationShareRide = new NotificationShareRide();

                notificationShareRide.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                notificationShareRide.setName(riderName);
                notificationShareRide.setLatStart(String.valueOf(mLastKnownLocation.getLatitude()));
                notificationShareRide.setLngStart(String.valueOf(mLastKnownLocation.getLongitude()));
                notificationShareRide.setLatEnd(String.valueOf(latLngOfPlaceDestination.latitude));
                notificationShareRide.setLngEnd(String.valueOf(latLngOfPlaceDestination.longitude));
                notificationShareRide.setStartLocation(startAddressLocation);
                notificationShareRide.setEndLocation(endAddressLocation);
                notificationShareRide.setNumOfSeats(num_of_seats.getText().toString());
                notificationShareRide.setRatePerSeat(rate_per_seat.getText().toString());


                notificationRideShare.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(notificationShareRide)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                waitingDialog.dismiss();
                                btnShareRide.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Toast.makeText(Welcome.this, "Can't Post your notification right now.", Toast.LENGTH_SHORT).show();
                                btnShareRide.setVisibility(View.VISIBLE);
                            }
                        });

            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
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
        if (latLngOfPlaceDestination != null) {
//            mMap.clear();

            markerOptionsList.clear();
            if (car != null) {
                car = null;
            }

            if (mLastKnownLocation != null) {

                place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                markerOptionsList.add(place1);
                marker1 = mMap.addMarker(place1);

                place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                markerOptionsList.add(place2);
                marker2 = mMap.addMarker(place2);

                showAllMarkers();
                loadAllAvaliableDriver();
            }

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            status("online");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            status("offline");
        }
    }

    private void updateFirebaseToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        reference.child(user.getUid()).setValue(token);
    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference().child(Common.token_tbl);

        Query query = tokens.orderByKey().equalTo(driverId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot postSnapShot : dataSnapshot.getChildren()){
                        Token token = postSnapShot.getValue(Token.class); // get Token object from database with key

                        //Make raw payload - convert LatLng to json
                        String json_lat_lng = new Gson().toJson(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
                        String dest_lat_lng = new Gson().toJson(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude));
                        String riderToken = FirebaseInstanceId.getInstance().getToken();

                        Log.d("LAPTOP", String.valueOf(latLngOfPlaceDestination.latitude));
                        Log.d("LAPTOP", String.valueOf(latLngOfPlaceDestination.longitude));
                        Toast.makeText(Welcome.this, "send", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(Welcome.this, "Sending request "+response.message(), Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(Welcome.this, "Failed !", Toast.LENGTH_LONG).show();
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
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists()){
//                            for (DataSnapshot postSnapShot : dataSnapshot.getChildren()){
//                                Token token = postSnapShot.getValue(Token.class); // get Token object from database with key
//
//                                //Make raw payload - convert LatLng to json
//                                //String json_lat_lng = new Gson().toJson(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()));
//                                Notification data = new Notification("RIDESHARE", "json_lat_lng"); // send it to driver app and we will deserialize it again
//                                Sender content = new Sender(token.getToken(),data); // send this data to token
//
//                                mService.sendMessage(content)
//                                        .enqueue(new Callback<FCMResponse>() {
//                                            @Override
//                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                                                if(response.body().success == 1) {
//                                                    Toast.makeText(Welcome.this, "Request sent!", Toast.LENGTH_LONG).show();
//                                                }
//                                                else {
//                                                    Toast.makeText(Welcome.this, "Failed !", Toast.LENGTH_LONG).show();
//                                                }
//
//                                            }
//
//                                            @Override
//                                            public void onFailure(Call<FCMResponse> call, Throwable t) {
//                                                Log.e("ERROR",t.getMessage());
//                                            }
//                                        });
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
    }

    private void requestPickupHere(String uid) {

        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if(marker1 != null)
                    marker1.remove();
                marker1 = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                 .snippet("")
                .position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                marker1.showInfoWindow();
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                btnFind.setText("Getting your Driver");

                radius = 1;
                
                findDriver();

            }
        });

    }

    private void findDriver() {



        final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gfDrivers = new GeoFire(drivers);

        final GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!key.isEmpty() && !key.equals("")) {
                    Log.d("LAPTOP", "key="+key);
                    //if found
                    if (!Common.isDriverFound) {

                        if (Common.rejectedDriver != null) {

                            for (String rejected : Common.rejectedDriver) {
                                Log.d("LAPTOP", rejected);
                                if (rejected.equals(key)) {
                                    reject = true;
                                    btnFind.setText("Find Driver");
                                    radius++;
                                    break;
                                } else {
                                    reject = false;

                                }
                            }
                        }

                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.booked_driver_tbl)
                                .child(firebaseUser.getUid());
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                notificationList.clear();
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                                    notificationList.add(snapshot.getKey());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        for (String rejected : notificationList) {
                            Log.d("LAPTOP", rejected);
                            if (rejected.equals(key)) {
                                reject = true;
                                btnFind.setText("Find Driver");
                                radius++;
                                break;
                            } else {
                                reject = false;

                            }
                        }

                        if (!reject) {
                            Common.isDriverFound = true;
                            Common.driverId = key;
                            btnFind.setText("Call Driver");


                            geoQuery.removeAllListeners();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Common.user_rider_tbl)
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                Rider newRider = dataSnapshot.getValue(Rider.class);
                                                riderName = newRider.getName();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                            Toast.makeText(Welcome.this, "Driver Found\n"+Common.driverId+" is on the way", Toast.LENGTH_SHORT).show();
                            Log.d("LAPTOP", String.valueOf(location.latitude));
                            Log.d("LAPTOP", String.valueOf(location.longitude));
                            driverLat = String.valueOf(location.latitude);
                            driverLng = String.valueOf(location.longitude);

                            //Toast.makeText(Welcome.this, "" + key, Toast.LENGTH_SHORT).show();
                        } else {
                            if(childrenCount == Common.rejectedDriver.size()){
                                geoQuery.removeAllListeners();
                                Common.allDrivers = false;
                                FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(Welcome.this, "No available any driver near you", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                findDriver();
                            }
                        }
                    }
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!Common.isDriverFound && radius <= LIMIT){
                    radius++;


                    if(radius == LIMIT){

                        btnFind.setText("Find Driver");
                        geoQuery.removeAllListeners();
                        Common.allDrivers = false;
                        FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Welcome.this, "No available any driver near you", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        findDriver();
                    }
                } else {
//                    if(!Common.isDriverFound && Common.allDrivers){
//                        Toast.makeText(Welcome.this, "No available any driver near you", Toast.LENGTH_SHORT).show();
//                        btnFind.setText("Find Driver");
//                        geoQuery.removeAllListeners();
//                        Common.allDrivers = false;
//                        FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
//                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Toast.makeText(Welcome.this, "pick up deleted" , Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat:
                //Toast.makeText(this, "Chat History selected", Toast.LENGTH_SHORT).show();
                Intent intent_1 = new Intent(Welcome.this, ChatsFragment.class);
                startActivity(intent_1);
                return true;
            case R.id.seeNotificationPosts:
                //Toast.makeText(this, "Notification Posts selected", Toast.LENGTH_SHORT).show();
                if(Common.isAccepted){
                    Toast.makeText(this, "You are already is a ride", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent_2 = new Intent(Welcome.this, NotificationPosts.class);
                    startActivity(intent_2);
                }
                return true;
            case R.id.updateInformation:
                //Toast.makeText(this, "Update Information selected", Toast.LENGTH_SHORT).show();
                showDialogUpdateInformation();
                return true;
            case R.id.changePassword:
                //Toast.makeText(this, "Change Password selected", Toast.LENGTH_SHORT).show();
                showDialogChangePwd();
                return true;
//            case R.id.help:
//                //Toast.makeText(this, "Help selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.setting:
//                //Toast.makeText(this, "Setting selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.signOut:
                //Toast.makeText(this, "Sign Out selected", Toast.LENGTH_SHORT).show();
                signOut();
                return true;
            default:
                return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat:
                //Toast.makeText(this, "Chat History selected", Toast.LENGTH_SHORT).show();
                Intent intent_1 = new Intent(Welcome.this, ChatsFragment.class);
                startActivity(intent_1);
                return true;
            case R.id.seeNotificationPosts:
                //Toast.makeText(this, "Notification Posts selected", Toast.LENGTH_SHORT).show();
                if(Common.isAccepted){
                    Toast.makeText(this, "You are already is a ride", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent_2 = new Intent(Welcome.this, NotificationPosts.class);
                    startActivity(intent_2);
                }
                return true;
            case R.id.updateInformation:
                //Toast.makeText(this, "Update Information selected", Toast.LENGTH_SHORT).show();
                showDialogUpdateInformation();
                return true;
            case R.id.changePassword:
                //Toast.makeText(this, "Change Password selected", Toast.LENGTH_SHORT).show();
                showDialogChangePwd();
                return true;
//            case R.id.help:
//                //Toast.makeText(this, "Help selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.setting:
//                //Toast.makeText(this, "Setting selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.signOut:
                //Toast.makeText(this, "Sign Out selected", Toast.LENGTH_SHORT).show();
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogUpdateInformation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Welcome.this);
        alertDialog.setTitle("CHANGE INFORMATION");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_info, null);
        //Toast.makeText(this, "g", Toast.LENGTH_SHORT).show();

        final MaterialEditText edtNameInfo = layout_pwd.findViewById(R.id.edtNameInfo);
        final MaterialEditText edtPhoneInfo = layout_pwd.findViewById(R.id.edtPhoneInfo);

        alertDialog.setView(layout_pwd);

        //setButton
        alertDialog.setPositiveButton("CHANGE INFORMATION", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {



                if(TextUtils.isEmpty(edtNameInfo.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtPhoneInfo.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialogInterface.dismiss();

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for Information Reset")
                        .setCancelable(false)
                        .setContext(Welcome.this)
                        .build();
                waitingDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("name", edtNameInfo.getText().toString());
                reference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Welcome.this, "name Updated Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Welcome.this, "name Update UnSuccessful", Toast.LENGTH_SHORT).show();
                                }
                                waitingDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Welcome.this, "name Failed to Update", Toast.LENGTH_SHORT).show();
                        waitingDialog.dismiss();
                    }
                });

                DatabaseReference referenceNew = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                HashMap<String, Object> hashMapNew = new HashMap<>();
                hashMapNew.put("phone", edtPhoneInfo.getText().toString());
                referenceNew.updateChildren(hashMapNew)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Welcome.this, "phone Updated Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Welcome.this, "phone Update UnSuccessful", Toast.LENGTH_SHORT).show();
                                }
                                waitingDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Welcome.this, "phone Failed to Update", Toast.LENGTH_SHORT).show();
                        waitingDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showDialogChangePwd() {
        //Toast.makeText(this, "g", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Welcome.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null);
        //Toast.makeText(this, "g", Toast.LENGTH_SHORT).show();

        final MaterialEditText edtPassword = layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatNewPassword = layout_pwd.findViewById(R.id.edtRepeatNewPassword);

        alertDialog.setView(layout_pwd);

        //setButton
        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {



                if(TextUtils.isEmpty(edtPassword.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Old Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtNewPassword.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter New Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtRepeatNewPassword.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Confirm Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialogInterface.dismiss();

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for Password Reset")
                        .setCancelable(false)
                        .setContext(Welcome.this)
                        .build();
                waitingDialog.show();

                if(edtNewPassword.getText().toString().equals(edtRepeatNewPassword.getText().toString())){

                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    //Get auth credentials from the user for re-authentication.
                    //Example with only email
                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatNewPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){

                                                            //Update Driver information password column
                                                            updateProfile(edtRepeatNewPassword.getText().toString());
                                                            waitingDialog.dismiss();

                                                        } else {
                                                            waitingDialog.dismiss();
                                                            Toast.makeText(Welcome.this, "Password doesn't change", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Welcome.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else {
                    waitingDialog.dismiss();
                    Toast.makeText(Welcome.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void updateProfile(String password){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("password", password);
        reference.updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Welcome.this, "Password was Changed!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Welcome.this, "Password was changed but not Updated in Database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signOut() {
        //Reset remember value
        Paper.init(this);
        Paper.book().destroy();

        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Welcome.this, MainActivity.class);
        startActivity(intent);
        finish();
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

        new FetchURL(Welcome.this)
                .execute(getUrl(place1.getPosition(),place2.getPosition(),"driving"),"driving");

        getDirection(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude);


        
    }

    private void getDirection(double latitude, double longitude, double latitude1, double longitude1) {

        String requestApi = null;
        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ latitude+","+longitude+"&"+
                    "destination="+latitude1+","+longitude1+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("RIDE", requestApi);//print url for debug
            //Toast.makeText(Welcome.this, "han g 1", Toast.LENGTH_LONG).show();

            DownloadTask downloadTask = new DownloadTask();
            // Start downloading json data from Google Directions API
            downloadTask.execute(requestApi);


        } catch (Exception e){
            e.printStackTrace();
            Log.d("NEW_TOKEN", "ERROR: 3");
            //Toast.makeText(Welcome.this, "han g 3", Toast.LENGTH_LONG).show();
        }
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        //for custom window
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mMap.getUiSettings().setZoomGesturesEnabled(true);
        if(!showBottom) {
            mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
            showBottom = true;
        }
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

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

        SettingsClient settingsClient = LocationServices.getSettingsClient(Welcome.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(Welcome.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(Welcome.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(Welcome.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
//                if (materialSearchBarStart.isSuggestionsVisible())
//                    materialSearchBarStart.clearSuggestions();
//                if (materialSearchBarStart.isSearchEnabled())
//                    materialSearchBarStart.disableSearch();
//
//                /////////////
//
//                if (materialSearchBarDestination.isSuggestionsVisible())
//                    materialSearchBarDestination.clearSuggestions();
//                if (materialSearchBarDestination.isSearchEnabled())
//                    materialSearchBarDestination.disableSearch();
//
                if(marker1 != null){
                    marker1.remove();
                    //markerOptionsList.clear();
                }
//
//                if(marker2 != null){
//                    marker2.remove();
//                    markerOptionsList.clear();
//                }
//
//                if(currentPolyline != null)
//                    currentPolyline.remove();
//
//                if(!startingAddress.isEmpty()){
//                    startingAddress = "";
//                }
//
//                if(!destinationAddress.isEmpty()){
//                    destinationAddress = "";
//                }
                if(currentPolyline != null)
                    currentPolyline.remove();

                getDeviceLocation();


                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latLngOfPlaceDestination = latLng;
                showBottom = false;
                if (latLngOfPlaceDestination != null) {
                    Log.d("LAPTOP", String.valueOf(latLngOfPlaceDestination));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlaceDestination, DEFAULT_ZOOM));
//                            if(markerOptionsList != null){
//                                marker1.remove();
//                                marker2.remove();
//                                markerOptionsList.clear();
//                            }


                    if(marker2 != null){
                        marker2.remove();
                        place2 = null;
                        markerOptionsList.remove(markerOptionsList.size()-1);

                        place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                        markerOptionsList.add(place2);
                        marker2 = mMap.addMarker(place2);
                    } else {
                        place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                        markerOptionsList.add(place2);
                        marker2 = mMap.addMarker(place2);
                    }

                    destinationAddress = "";

                    if(currentPolyline != null)
                        currentPolyline.remove();

                    showAllMarkers();
                    loadAllAvaliableDriver();
                }
            }
        });


        mMap.setOnInfoWindowClickListener(this);
        
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
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

//                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                    @Override
//                                    public void onComplete(String key, DatabaseError error) {
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                    }
//                                });

                                place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                markerOptionsList.add(place1);
                                marker1 = mMap.addMarker(place1);

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                loadAllAvaliableDriver();
                                if(marker2 != null) {
                                    showAllMarkers();
                                }
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

//                                        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                            @Override
//                                            public void onComplete(String key, DatabaseError error) {
//                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                            }
//                                        });

                                        place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                        markerOptionsList.add(place1);
                                        marker1 = mMap.addMarker(place1);

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        loadAllAvaliableDriver();
                                        if(marker2 != null) {
                                            showAllMarkers();
                                        }
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(Welcome.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadAllAvaliableDriver() {

        //First we need delete all markers on map (include our location marker and available drivers marker)

        //mMap.clear();
        //After that, just add our location again
        //mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).
                //title("You"));
        //load all avaliable driver in distance 3km

//        if(markerCar != null ){
//            markerCar.remove();
//
//
//            if (car != null) {
//                car = null;
//            }

//            if (mLastKnownLocation != null) {
//                mMap.clear();
//                Toast.makeText(this, "Clear", Toast.LENGTH_SHORT).show();
//
////                markerOptionsList.clear();
////                place1 = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
////                markerOptionsList.add(place1);
////                marker1 = mMap.addMarker(place1);
////
////                if (latLngOfPlaceDestination != null) {
////                    place2 = new MarkerOptions().position(new LatLng(latLngOfPlaceDestination.latitude, latLngOfPlaceDestination.longitude)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
////                    markerOptionsList.add(place2);
////                    marker2 = mMap.addMarker(place2);
////
////                    showAllMarkers();
////                }
//
//            }

        for (Marker marker: mMarkers) {
            marker.remove();
        }
        mMarkers.clear();


//        }

        allowDriverDisplay = false;

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                //use key to get email from table Users
                //Table Users is table when driver register account and update information
                Log.d("LAPTOP", key);
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){
                                    Rider rider = dataSnapshot.getValue(Rider.class);



                                    if(Common.rejectedDriver != null && !Common.rejectedDriver.isEmpty()){

                                        for (String rejected : Common.rejectedDriver) {
                                            Log.d("LAPTOP",rejected);
                                            if(rejected.equals(dataSnapshot.getKey())){
                                                 allowDriverDisplay = false;
                                                 break;
                                            } else {
                                                allowDriverDisplay = true;

                                            }
                                        }

                                        if(allowDriverDisplay){
//                                            car = new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).flat(true)
//                                                    .title(rider.getName())
//                                                    .snippet(""+dataSnapshot.getKey())
//                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//                                            markerOptionsList.add(car);
//                                            mMap.addMarker(car);
//                                            car = null;
                                        }
                                    } else {
                                        car = new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).flat(true)
                                                .title(rider.getName())
                                                .snippet(""+dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                                        markerOptionsList.add(car);
                                        markerCar = mMap.addMarker(car);
                                        mMarkers.add(markerCar);
                                        car = null;
                                    }
//                                    car = new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).flat(true)
//                                            .title(rider.getName())
//                                            .snippet(dataSnapshot.getKey())
//                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//                                    markerOptionsList.add(car);
//                                    mMap.addMarker(car);
//                                    car = null;



                                    //add driver to map
//                                    mMap.addMarker(new MarkerOptions()
//                                    .position(new LatLng(location.latitude, location.longitude))
//                                    .flat(true)
//                                    .title(rider.getName())
//                                    .snippet("Phone : "+rider.getPhone())
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

//                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
//                        .child(key)
//                        .addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                if(dataSnapshot.exists()) {
//                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                        Rider rider = snapshot.getValue(Rider.class);
//
//                                        car = new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).flat(true)
//                                                .title(rider.getName())
//                                                .snippet("Phone : " + rider.getPhone())
//                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//                                        markerOptionsList.add(car);
//                                        mMap.addMarker(car);
//
//                                        //add driver to map
////                                    mMap.addMarker(new MarkerOptions()
////                                    .position(new LatLng(location.latitude, location.longitude))
////                                    .flat(true)
////                                    .title(rider.getName())
////                                    .snippet("Phone : "+rider.getPhone())
////                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
//                                    }
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                            }
//                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance <= LIMIT){  // distance just find for 3km
                    distance++;
                    loadAllAvaliableDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolyline != null)
            currentPolyline.remove();

        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        if (btnShareRide.getVisibility() == View.VISIBLE){

        } else {
            btnFind.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if(latLngOfPlaceDestination != null) {

            //if marker info window is your location, don't use this event
            if (marker.getTitle().equals("You")) {


            }else if (marker.getTitle().equals("Pickup Here")){

            } else {
                Common.driverId =  marker.getSnippet().replace("\\D", "");
                Common.isDriverFound = true;
                //Call to new activity : CallDriver
                Intent intent = new Intent(Welcome.this, CallDriver.class);
                //Send information to new activity
                intent.putExtra("driverId", marker.getSnippet().replace("\\D", ""));
                Log.d("Problem", marker.getSnippet().replace("\\D", ""));
                intent.putExtra("lat_start", mLastKnownLocation.getLatitude());
                intent.putExtra("lng_start", mLastKnownLocation.getLongitude());
                intent.putExtra("lat_end", latLngOfPlaceDestination.latitude);
                intent.putExtra("lng_end", latLngOfPlaceDestination.longitude);

                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "First select destination.", Toast.LENGTH_SHORT).show();
        }
    }


    //////////////////// for getting duration time etc

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            Log.d("result", result.toString());

            for (int i = 0; i < result.size(); i++) {


                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    String  distance = point.get("distance");
                    String  duration = point.get("duration");
                    startingAddress = point.get("start_address");
                    destinationAddress = point.get("end_address");

//                    txtDistance.setText(distance);
//                    txtAddress.setText(end_address);
//                    txtTime.setText(duration);

                    if(startingAddress.isEmpty()){
                        startingAddress = "Your Location";
                    }

                    startAddressLocation = startingAddress;
                    endAddressLocation = destinationAddress;

                    mBottomSheet = BottomSheetRiderFragment.newInstance(startingAddress, destinationAddress, distance, duration);
                    if(!showBottom) {
                        mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                    }
                }


            }

        }
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
            Log.d("data", data);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


}