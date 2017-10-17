package nimbus.arcane;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Richard Aldrich on 21/9/2017.
 *
 * Last edited by Richard Aldrich 14/10/2017
 *
 * this class functions as a personal chat container
 */
public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;
    private String userName;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private TextView mLastSeenView;
    private TextView mTitleView;
    private CircleImageView mProfileImage;

    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private GPSTracker gpsTracker;
    private Location mLocation;
    private double latitude;
    private double longitude;
    private LatLng mUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        getSupportActionBar().setTitle("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_field);

        mAdapter = new MessageAdapter(messagesList, mChatUser);

        mMessagesList = (RecyclerView) findViewById(R.id.chat_message_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        gpsTracker = new GPSTracker(ChatActivity.this);

        loadMessages();

        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                if (online.equals("true")) {

                    mLastSeenView.setText("Online");

                } else {

//                    GetTimeAgo getTimeAgo = new GetTimeAgo();
//                    long lastTime = Long.parseLong(online);
//                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

//                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
//                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT LOG", databaseError.getMessage().toLowerCase());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage("text", null);

            }
        });
    }

    /**
     * load all the message that the current user and the user chosen and show the message.
     */
    public void loadMessages() {

        DatabaseReference friends_database = mRootRef.child("Users").child(mCurrentUserId).child("Friends");

        friends_database.child(mChatUser).child("Chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * save the message sent to the database and show it in the chat message.
     * @param type the type of message to be sent
     * @param msg the message that the user type on the input text layout
     */
    public void sendMessage(String type, String msg) {

        String message;

        if (type.equals("text")) {

            message = mChatMessageView.getText().toString();

        } else {

            message = msg;

        }

        if (!TextUtils.isEmpty(message)) {

            DatabaseReference user_message_push = mRootRef.child("Users").child(mCurrentUserId).child("Friends").child(mChatUser).child("Chat").push();
            String push_id = user_message_push.getKey();

            String current_user_ref = "Users/" + mCurrentUserId + "/Friends/" + mChatUser + "/Chats";
            String chat_user_ref = "Users/" + mChatUser + "/Friends/" + mCurrentUserId + "/Chats";

            // a new map that will be put into database
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", type);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.user_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.user_menu_location) {

            gpsTracker.checkGPS();

            int permission_all = 1;
            int check_permission;
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

            // checking permission
            check_permission = MapFragment.hasPermissions(ChatActivity.this, permissions);
            if (check_permission == 1) {

                ActivityCompat.requestPermissions(ChatActivity.this, permissions, permission_all);

            }

            mLocation = gpsTracker.getLocation();

            if (mLocation != null) {

                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
                mUserLocation = new LatLng(latitude, longitude);
//                Toast.makeText(ChatActivity.this, mUserLocation.toString(), Toast.LENGTH_LONG).show();
                sendMessage("location", mUserLocation.toString());

            } else {

                Toast.makeText(ChatActivity.this, "no location available", Toast.LENGTH_LONG).show();

            /*
                get location from database
             */

            }

        }

//        if (item.getItemId() == R.id.user_menu_destination) {
//
//            Intent mapIntent = new Intent(ChatActivity.this, MapActivity.class);
//            startActivity(mapIntent);
//
//        }

        return true;
    }

    public void sendToMap(View view) {

        Intent mapIntent = new Intent(ChatActivity.this, MapActivity.class);
        mapIntent.putExtra("user", mChatUser);
        startActivity(mapIntent);

//        Log.d("LOCATION FROM Chat", mChatUser);

    }

    public void setDataBase(DatabaseReference dr ){
        mRootRef = dr;
    }
}
