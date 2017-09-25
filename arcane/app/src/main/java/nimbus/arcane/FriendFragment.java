package nimbus.arcane;


import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mRootRef;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserID;

    private View mMainView;

    private boolean state;
    private final boolean NO_GROUP = false;
    private final boolean GROUP = true;

    public FriendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friend, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mFriendsDatabase = mRootRef.child("Users").child(mCurrentUserID).child("Friends");
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = mRootRef.child("Users");
        mUsersDatabase.keepSynced(true);

        state = NO_GROUP;

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class, R.layout.users_single_layout, FriendsViewHolder.class, mFriendsDatabase) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);

                        }

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setDisplayImage(userThumbImage, getContext());
                        friendsViewHolder.setStatus(userStatus);

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message", "Add to Group"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // Click Event for each item
                                        if (i == 0) {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }

                                        if (i == 1) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }

                                        if (i == 2) {

                                            addToGroup(list_user_id);


                                        }
                                    }
                                });

                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mFriendsList.setAdapter(friendsRecyclerViewAdapter);

    }

    public void addToGroup(String user_id) {


        Map friendsMap = new HashMap();
        friendsMap.put("Groups/Members/" + user_id + "/date", ServerValue.TIMESTAMP);

        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {

                    Toast.makeText(getContext(), "Friend added to group", Toast.LENGTH_LONG).show();

                } else {

                    String error = databaseError.getMessage();

                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

                }
            }
        });

//        final String user_key = user_id;
//
//        mRootRef.child("Groups").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                if (!(dataSnapshot.child("Friends").hasChild(user_key)) && !state) {
//
//                    state = GROUP;
//
//                    Map friendsMap = new HashMap();
//                    friendsMap.put("Groups/Members/" + user_key + "/date", ServerValue.TIMESTAMP);
//
//                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
//                        @Override
//                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//
//                            if (databaseError == null) {
//
//                                Toast.makeText(getContext(), "Friend added to group", Toast.LENGTH_LONG).show();
//
//                            } else {
//
//                                String error = databaseError.getMessage();
//
//                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
//
//                            }
//                        }
//                    });
//                } else if (dataSnapshot.child("Friends").hasChild(user_key)) {
//
//                    Toast.makeText(getContext(), "Friend already in group", Toast.LENGTH_LONG).show();
//
//                } else {
//
//                    Toast.makeText(getContext(), "No Group Exists", Toast.LENGTH_LONG).show();
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setStatus(String status) {

            TextView userStatusView = (TextView) mView.findViewById(R.id.users_status);
            userStatusView.setText(status);

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.users_display_name);
            userNameView.setText(name);

        }

        public void setDisplayImage(String thumb_image, Context ctx) {

            CircleImageView userImage_view = (CircleImageView) mView.findViewById(R.id.users_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage_view);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.users_online_icon);
            if (online_status.equals("true")) {

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }

    }
}
