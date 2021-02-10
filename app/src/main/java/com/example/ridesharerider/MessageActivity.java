package com.example.ridesharerider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharerider.Adapter.MessageAdapter;
import com.example.ridesharerider.Common.Common;
import com.example.ridesharerider.Model.Chat;
import com.example.ridesharerider.Model.DataMessage;
import com.example.ridesharerider.Model.FCMResponse;
import com.example.ridesharerider.Model.Rider;
import com.example.ridesharerider.Model.Token;
import com.example.ridesharerider.Remote.IFCMService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView username;

    private FirebaseUser fuser;
    private DatabaseReference reference;

    private Intent intent;

    private ImageButton btn_send;
    private EditText text_send;

    private MessageAdapter messageAdapter;
    private List<Chat> mchat;

    private RecyclerView recyclerView;

    private ValueEventListener seenListener;
    private String userid;
    private boolean notify = false;
    private IFCMService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.message_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apiService = Common.getFCMService();

        recyclerView = findViewById(R.id.Message_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.message_profile_image);
        username = findViewById(R.id.message_username);
        btn_send = findViewById(R.id.Message_btn_send);
        text_send = findViewById(R.id.Message_text_send);



        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        final Drawable myDrawable = getResources().getDrawable(R.drawable.ic_action_userprofile);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Rider user = dataSnapshot.getValue(Rider.class);
                username.setText(user.getName());
                profile_image.setImageDrawable(myDrawable);


                readMessage(fuser.getUid(), userid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        seenMessage(userid);
    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference(Common.Chats);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, final String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child(Common.Chats).push().setValue(hashMap);

        //add sender user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(Common.Chatlist)
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //add receiver user to chat fragment
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference(Common.Chatlist)
                .child(userid)
                .child(fuser.getUid());

        chatRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(fuser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        //DatabaseReference database = FirebaseDatabase.getInstance().getReference("User").child(sender);
        reference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Rider user = dataSnapshot.getValue(Rider.class);

                if (notify){
                    sendNotification(receiver, user.getName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String receiver, final String username, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Query query = allTokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);

                    Map<String, String> content = new HashMap<>();
                    content.put("user", fuser.getUid());
                    content.put("title", "New Message");
                    content.put("message", username+":"+message);
                    content.put("sented", receiver);
                    DataMessage dataMessage = new DataMessage(token.getToken(), content);

                    apiService.sendMessage(dataMessage)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                                    if (response.code() == 200){
//                                        if (response.body())
//                                    }
                                    Toast.makeText(MessageActivity.this, "Message send: "+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String myid, final String userid){

        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference(Common.Chats);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(new Chat(chat.getSender(), chat.getReceiver(),chat.getMessage(), chat.isIsseen()));
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, "default");
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("userlogindetail",MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }
    private void status(String status){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }
    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("online");
        currentUser("none");
    }
}