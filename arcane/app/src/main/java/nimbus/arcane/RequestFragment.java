package nimbus.arcane;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Nico Dinata
 */
public class RequestFragment extends Fragment {

    private RecyclerView mRequestList;

    private DatabaseReference mRootRef;
    private DatabaseReference mRequestDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserID;

    private View mMainView;

    private ArrayList<String> availableUserIDList=new ArrayList<String>();

    public RequestFragment() {
        // required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestList = (RecyclerView) mMainView.findViewById(R.id.request_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRequestDatabase = mRootRef.child("Users").child(mCurrentUserID).child("request");
        mRequestDatabase.keepSynced(true);
        mUsersDatabase = mRootRef.child("Users");
        mUsersDatabase.keepSynced(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // render all available requests from friends
        FirebaseRecyclerAdapter<Request, RequestFragment.RequestViewHolder> requestRecyclerViewAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class, R.layout.request_single_layout, RequestFragment.RequestViewHolder.class, mRequestDatabase) {
            @Override
            protected void populateViewHolder(final RequestFragment.RequestViewHolder requestViewHolder, Request request, int position) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                        requestViewHolder.setName(userName);
                        requestViewHolder.setDisplayImage(userThumbImage, getContext());

                        requestViewHolder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Map friendsMap = new HashMap();
                                friendsMap.put("Users/" + mCurrentUserID + "/Friends/" + list_user_id + "/date", ServerValue.TIMESTAMP);
                                friendsMap.put("Users/" + list_user_id + "/Friends/" + mCurrentUserID + "/date", ServerValue.TIMESTAMP);
                                friendsMap.put("Friend_Request/" + mCurrentUserID + "/" + list_user_id, null);
                                friendsMap.put("Friend_Request/" + list_user_id + "/" + mCurrentUserID, null);
                                friendsMap.put("Users/" + mCurrentUserID + "/request/" + list_user_id, null);

                                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        if (databaseError == null) {

                                            Toast.makeText(getContext(), "accept", Toast.LENGTH_SHORT).show();

                                        } else {

                                            String error = databaseError.getMessage();

                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                            }
                        });

                        requestViewHolder.declineBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Map friendReqMap = new HashMap();
                                friendReqMap.put("Friend_Request/" + mCurrentUserID + "/" + list_user_id, null);
                                friendReqMap.put("Friend_Request/" + list_user_id + "/" + mCurrentUserID, null);
                                friendReqMap.put("Users/" + mCurrentUserID + "/request/" + list_user_id, null);

                                mRootRef.updateChildren(friendReqMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        if (databaseError == null) {

                                            Toast.makeText(getContext(), "decline", Toast.LENGTH_LONG).show();

                                        } else {

                                            String error = databaseError.getMessage();

                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mRequestList.setAdapter(requestRecyclerViewAdapter);

    }

    // a view holder for each request that will be used in the recycler view
    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageButton acceptBtn, declineBtn;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            acceptBtn = (ImageButton) mView.findViewById(R.id.request_accept);
            declineBtn = (ImageButton) mView.findViewById(R.id.request_cancel);

        }

        public void setName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.request_display_name);
            userNameView.setText(name);

        }

        public void setDisplayImage(String thumb_image, Context ctx) {

            CircleImageView userImage_view = (CircleImageView) mView.findViewById(R.id.request_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage_view);

        }

    }
}
