package com.example.ridesharerider;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ridesharerider.Common.Common;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
    String mLocation, mDestination, mDistance, mDuration;

    public static BottomSheetRiderFragment newInstance(String location, String destination, String distance, String duration){
        BottomSheetRiderFragment f = new BottomSheetRiderFragment();
        Bundle args = new Bundle();
        args.putString("location",location);
        args.putString("destination",destination);
        args.putString("distance",distance);
        args.putString("duration",duration);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        mDistance = getArguments().getString("distance");
        mDuration = getArguments().getString("duration");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_rider, container, false);
        TextView start = view.findViewById(R.id.txtLocation);
        TextView destination = view.findViewById(R.id.txtDestination);
        TextView duration = view.findViewById(R.id.txtDuration);

        String substr = mDistance.substring(mDistance.length()-2, mDistance.length());

        String distance = "";

        if(substr.equals("km") || substr.equals("Km")){
            distance = mDistance.substring(0,mDistance.length()-3);
        } else {
            distance = mDistance.substring(0,mDistance.length()-2);
        }

        String dur = "";

        if(mDuration.length() < 6){
            dur = mDuration.substring(0,mDuration.length()-4);
        } else if(mDuration.length() == 6) {
            dur = mDuration.substring(0,mDuration.length()-5);
        }else if(mDuration.length() == 7) {
            dur = mDuration.substring(0,mDuration.length()-5);
        } else {
            long h=0,t_h=0,m=0,t_m=0;
            if(mDuration.length() == 12){
                t_h = Long.parseLong(mDuration.substring(0,mDuration.length()-11));
                t_m = Long.parseLong(mDuration.substring(7,mDuration.length()-4));

                h = t_h*60;
                m = t_m+h;
                dur = String.valueOf(m);
            } else if(mDuration.length() == 13){
                t_h = Long.parseLong(mDuration.substring(0,mDuration.length()-12));
                if(t_h==1){
                    t_m = Long.parseLong(mDuration.substring(7,mDuration.length()-5));

                    h = t_h*60;
                    m = t_m+h;
                    dur = String.valueOf(m);
                } else {
                    t_m = Long.parseLong(mDuration.substring(8,mDuration.length()-4));

                    h = t_h*60;
                    m = t_m+h;
                    dur = String.valueOf(m);
                }

            } else if(mDuration.length() == 14){
                String tem = mDuration.substring(1,mDuration.length()-12);
                if(tem.equals(" ")) {
                    t_h = Long.parseLong(mDuration.substring(0, mDuration.length() - 13));
                    if (t_h == 1) {
                        t_m = Long.parseLong(mDuration.substring(7, mDuration.length() - 5));

                        h = t_h * 60;
                        m = t_m + h;
                        dur = String.valueOf(m);
                    } else {
                        t_m = Long.parseLong(mDuration.substring(8, mDuration.length() - 5));

                        h = t_h * 60;
                        m = t_m + h;
                        dur = String.valueOf(m);
                    }
                } else {
                    t_h = Long.parseLong(mDuration.substring(0, mDuration.length() - 12));
                    t_m = Long.parseLong(mDuration.substring(9, mDuration.length() - 4));

                    h = t_h * 60;
                    m = t_m + h;
                    dur = String.valueOf(m);
                }

            }  else if(mDuration.length() == 15){
                String tem = mDuration.substring(1,mDuration.length()-13);
                if(tem.equals(" ")) {
                    t_h = Long.parseLong(mDuration.substring(0,mDuration.length()-14));
                    t_m = Long.parseLong(mDuration.substring(8,mDuration.length()-5));

                    h = t_h*60;
                    m = t_m+h;
                    dur = String.valueOf(m);
                } else {
                    t_h = Long.parseLong(mDuration.substring(0,mDuration.length()-13));
                    t_m = Long.parseLong(mDuration.substring(9,mDuration.length()-5));

                    h = t_h*60;
                    m = t_m+h;
                    dur = String.valueOf(m);
                }
            } else if (mDuration.length() == 16) {
                t_h = Long.parseLong(mDuration.substring(0, mDuration.length() - 14));
                t_m = Long.parseLong(mDuration.substring(9, mDuration.length() - 5));

                h = t_h * 60;
                m = t_m + h;
                dur = String.valueOf(m);
            }

        }



        Log.d("LAPTOP", String.valueOf(mDuration.length()));

        Log.d("LAPTOP", mDistance);
        Log.d("LAPTOP", mDuration);
        Log.d("LAPTOP", String.valueOf(mDuration.length()));
        Log.d("LAPTOP", substr);
        Log.d("LAPTOP", distance);
        Log.d("LAPTOP", dur);

        double rate = Common.getPrice(Double.parseDouble(distance), Integer.parseInt(dur));
        //double rate = 40.0;
        Log.d("LAPTOP", String.valueOf(rate));
        String whole = mDistance + " + " + mDuration + " = " + rate + "Rs";

        start.setText(mLocation);
        destination.setText(mDestination);
        duration.setText(whole);
        return view;
    }
}
