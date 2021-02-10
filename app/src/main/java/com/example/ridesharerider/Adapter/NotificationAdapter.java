package com.example.ridesharerider.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridesharerider.Model.NotificationShareRide;
import com.example.ridesharerider.NotificationPostDetail;
import com.example.ridesharerider.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{



    private Context mContext;
    private List<NotificationShareRide> mNotification;

    public NotificationAdapter(Context mContext, List<NotificationShareRide> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_notification_posts , parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NotificationShareRide notification = mNotification.get(position);

        holder.notificationNumOfSeats.setText("Seats: "+notification.getNumOfSeats());
        holder.notificationRatePerSeat.setText("__Rate: "+notification.getRatePerSeat());
        holder.notificationStartAddress.setText("Start: "+notification.getStartLocation());
        holder.notificationEndAddress.setText("End: "+notification.getEndLocation());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = notification.getId();
                //Toast.makeText(mContext, userId, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, NotificationPostDetail.class);
                intent.putExtra("userId", userId);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView notificationNumOfSeats, notificationRatePerSeat, notificationStartAddress, notificationEndAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            notificationNumOfSeats = itemView.findViewById(R.id.notificationNumOfSeats);
            notificationRatePerSeat = itemView.findViewById(R.id.notificationRatePerSeat);
            notificationStartAddress = itemView.findViewById(R.id.notificationStartAddress);
            notificationEndAddress = itemView.findViewById(R.id.notificationEndAddress);

        }
    }


}
