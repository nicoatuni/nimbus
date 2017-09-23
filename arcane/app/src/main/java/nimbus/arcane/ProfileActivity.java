package nimbus.arcane;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mDisplayImage;
    private TextView mDisplayName, mDisplayStatus, mDisplayFriendsCounts;
    private Button mProfileSendReqBtn, mProfileDeclineBtn;

    private DatabaseReference mUsersDatabase, mFriendReqDatabase, mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;
    private Toolbar mToolbar;

    private String current_state;
    private int mCurrentState;
    private static final int FRIENDS = 3;
    private static final int REQUEST_RECEIVED = 2;
    private static final int REQUEST_SENT = 1;
    private static final int NO_FRIENDS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_key = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = mRootRef.child("Users").child(user_key);
        mFriendReqDatabase = mRootRef.child("Friend_Request");
        mFriendReqDatabase.keepSynced(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase = mRootRef.child("Users").child(mCurrentUser.getUid()).child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mDisplayImage = (ImageView) findViewById(R.id.profile_display_image);
        mDisplayName = (TextView) findViewById(R.id.profile_display_name);
        mDisplayStatus = (TextView) findViewById(R.id.profile_display_status);
        mDisplayFriendsCounts = (TextView) findViewById(R.id.profile_display_friends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_request_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mToolbar = (Toolbar) findViewById(R.id.profile_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentState = NO_FRIENDS;
        current_state = "not_friends";

        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(display_name);
                mDisplayStatus.setText(display_status);
                Picasso.with(ProfileActivity.this).load(display_image).placeholder(R.drawable.default_avatar).into(mDisplayImage);

                if(mCurrentUser.getUid().equals(user_key)){

                    mProfileDeclineBtn.setEnabled(false);
                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);

                }

                // Friend list or request feature
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_key)) {

                            String req_type = dataSnapshot.child(user_key).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mCurrentState = REQUEST_RECEIVED;
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {

                                mCurrentState = REQUEST_SENT;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_key)) {

                                        mCurrentState = FRIENDS;
                                        mProfileSendReqBtn.setText("UnFriend this Person");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user_id = mCurrentUser.getUid();
                mProfileSendReqBtn.setEnabled(false);

                // not friends
                if (mCurrentState == NO_FRIENDS) {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_key).push();
                    String newNotificationID = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_Request/" + user_id + "/" + user_key + "/request_type", "sent");
                    requestMap.put("Friend_Request/" + user_key + "/" + user_id + "/request_type", "received");
                    requestMap.put("notifications/" + user_key + "/" + newNotificationID, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_LONG).show();

                            } else {

                                mCurrentState = REQUEST_SENT;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }

                // cancel request
                if (mCurrentState == REQUEST_SENT) {

                    Map friendReqMap = new HashMap();
                    friendReqMap.put("Friend_Request/" + user_id + "/" + user_key, null);
                    friendReqMap.put("Friend_Request/" + user_key + "/" + user_id, null);

                    mRootRef.updateChildren(friendReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = NO_FRIENDS;
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

                // accepting a request
                if (mCurrentState == REQUEST_RECEIVED) {

                    Map friendsMap = new HashMap();
//                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_key + "/date", ServerValue.TIMESTAMP);
//                    friendsMap.put("Friends/" +  user_key + "/" + mCurrentUser.getUid() + "/date", ServerValue.TIMESTAMP);
                    friendsMap.put("Users/" + user_id + "/Friends/" + user_key + "/date", ServerValue.TIMESTAMP);
                    friendsMap.put("Users/" + user_key + "/Friends/" + user_id + "/date", ServerValue.TIMESTAMP);
                    friendsMap.put("Friend_Request/" + user_id + "/" + user_key, null);
                    friendsMap.put("Friend_Request/" + user_key + "/" + user_id, null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = FRIENDS;
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

                // unfriend a friend
                if (mCurrentState == FRIENDS) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Users/" + user_key + "/Friends/" + user_id, null);
                    unfriendMap.put("Users/" + user_id + "/Friends/" + user_key, null);
//                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_key, null);
//                    unfriendMap.put("Friends/" + user_key + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mCurrentState = NO_FRIENDS;
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });
                }
            }
        });

        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // declining a request
                if (mCurrentState == REQUEST_RECEIVED) {

                    Map declineMap = new HashMap();
                    declineMap.put("Friend_Request/" + mCurrentUser.getUid() + "/" + user_key, null);
                    declineMap.put("Friend_Request/" + user_key + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = NO_FRIENDS;
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });

    }
}