package nimbus.arcane;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ProfileActivity extends AppCompatActivity {

    private ImageView mDisplayImage;
    private TextView mDisplayName, mDisplayStatus, mDisplayFriendsCounts;
    private Button mProfileSendReqBtn, mProfileDeclineBtn;

    private DatabaseReference mUsersDatabase, mFriendReqDatabase, mFriendDatabase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

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

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_key);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayImage = (ImageView) findViewById(R.id.profile_display_image);
        mDisplayName = (TextView) findViewById(R.id.profile_display_name);
        mDisplayStatus = (TextView) findViewById(R.id.profile_display_status);
        mDisplayFriendsCounts = (TextView) findViewById(R.id.profile_display_friends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_request_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mCurrentState = 0;

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

                // Friend list or request feature
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_key)) {

                            String req_type = dataSnapshot.child(user_key).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mCurrentState = REQUEST_RECEIVED;
                                mProfileSendReqBtn.setText("Accept Friend Request");

                            } else if (req_type.equals("sent")) {

                                mCurrentState = REQUEST_SENT;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_key)) {

                                        mCurrentState = FRIENDS;
                                        mProfileSendReqBtn.setText("UnFriend this Person");

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

                mProfileSendReqBtn.setEnabled(false);

                // not friends
                if (mCurrentState == NO_FRIENDS) {

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_key).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mFriendReqDatabase.child(user_key).child(mCurrentUser.getUid()).child("request_type").setValue("receive").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mProfileSendReqBtn.setEnabled(true);
                                        mCurrentState = REQUEST_SENT;
                                        mProfileSendReqBtn.setText("Cancel Friend Request");

                                    }
                                });

                            } else {

                                Toast.makeText(ProfileActivity.this, "Failed Sending Requests", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }

                // cancel request
                if (mCurrentState == REQUEST_SENT) {

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = NO_FRIENDS;
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                }
                            });

                        }
                    });
                }

                // accept request
                if (mCurrentState == REQUEST_RECEIVED) {

                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_key).setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_key).child(mCurrentUser.getUid()).setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {


                                            mFriendReqDatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrentState = FRIENDS;
                                                    mProfileSendReqBtn.setText("UnFriend this Person");

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                // Unfriend a friend
                if (mCurrentState == FRIENDS) {

                    mFriendDatabase.child(user_key).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(mCurrentUser.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = NO_FRIENDS;
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
