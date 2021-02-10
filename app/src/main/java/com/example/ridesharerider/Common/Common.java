package com.example.ridesharerider.Common;

import android.util.Log;

import com.example.ridesharerider.Model.Rider;
import com.example.ridesharerider.Remote.FCMClient;
import com.example.ridesharerider.Remote.IFCMService;

import java.util.ArrayList;
import java.util.List;

public class Common {
    public static final String driver_tbl = "Drivers";
    public static final String Chatlist = "Chatlist";
    public static final String Chats = "Chats";
    public static final String booked_driver_tbl = "BookedDrivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String rate_detail_tbl = "RateDetails";
    public static final String notification_ride_share = "ShareRide";

    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String user_field = "rider_usr";
    public static final String pwd_field = "rider_pwd";

    //find driver
    public static boolean isDriverFound = false;
    public static String driverId = "";

    public static Rider currentUser = new Rider();

    public static boolean againLoad = false;

    public static boolean isAccepted = false;

    public static boolean allDrivers = false;

    public static List<String> rejectedDriver = new ArrayList<>();

    public static double base_fare = 30.55;
    public static double time_rate = 10.35;
    public static double distance_rate = 10.75;

    public static double getPrice(double km, int min){
        Log.d("LAPTOP", String.valueOf(km));
        Log.d("LAPTOP", String.valueOf(min));
        return (base_fare + ( time_rate * min) + (distance_rate * km));
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
