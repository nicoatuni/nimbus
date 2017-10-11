package nimbus.arcane;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.Image;
import android.media.MediaPlayer;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
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

import static nimbus.arcane.R.id.map;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

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
        setContentView(R.layout.activity_group_chat);

        String group_name = getIntent().getStringExtra("group_name");
        final String group_key = getIntent().getStringExtra("group_id");

        mToolbar = (Toolbar) findViewById(R.id.chat_group_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(group_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatSendBtn = (ImageButton) findViewById(R.id.chat_group_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_group_field);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.chat_group_message_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        gpsTracker = new GPSTracker(GroupChatActivity.this);

        loadMessages(group_key);

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage(group_key);

            }
        });
    }

    private void loadMessages(String group_key) {

        DatabaseReference group_database = mRootRef.child("Groups");

        group_database.child(group_key).child("Chats").addChildEventListener(new ChildEventListener() {
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

    private void sendMessage(final String group_key) {

        final String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            DatabaseReference user_message_push = mRootRef.child("Groups").child("Chats").push();
            final String push_id = user_message_push.getKey();

            mRootRef.child("Users").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String userName = dataSnapshot.child("name").getValue().toString();

                    Map messageMap = new HashMap();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);
                    messageMap.put("type", "text");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", mCurrentUserId);
                    messageMap.put("name", userName);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put("Groups/" + group_key + "/Chats/" + push_id, messageMap);

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

                @Override
                public void onCancelled(DatabaseError databaseError) {

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
            check_permission = MapFragment.hasPermissions(GroupChatActivity.this, permissions);
            if (check_permission == 1) {

                ActivityCompat.requestPermissions(GroupChatActivity.this, permissions, permission_all);

            }

            mLocation = gpsTracker.getLocation();

            if (mLocation != null) {

                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
                mUserLocation = new LatLng(latitude, longitude);
                Toast.makeText(GroupChatActivity.this, mUserLocation.toString(), Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(GroupChatActivity.this, "no location available", Toast.LENGTH_LONG).show();

            /*
                get location from database
             */

            }

        }

        if (item.getItemId() == R.id.user_menu_destination) {

            Intent mapIntent = new Intent(GroupChatActivity.this, MapActivity.class);
            startActivity(mapIntent);

        }

//        if (item.getItemId() == R.id.main_all_btn) {
//
//            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
//            startActivity(usersIntent);
//
//        }

        return true;
    }

}
