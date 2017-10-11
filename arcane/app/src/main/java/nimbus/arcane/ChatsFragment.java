package nimbus.arcane;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RelativeLayout mGroupList;
    private RecyclerView mGroupsList;

    private View mMainView;

    private View mLayout;
    private TextView mGroupName;
    private TextView mStatus;
    private CircleImageView mImageView;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;
    private DatabaseReference mGroupRef;
    private DatabaseReference mGroupsDatabase;
    private DatabaseReference mAllGroupsDatabase;

    private Iterable<DataSnapshot> mGroups;
    private String group_id;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
              Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mGroupsList = (RecyclerView) mMainView.findViewById(R.id.groups_list);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupRef = mRootRef.child("Groups");

        mAllGroupsDatabase = mRootRef.child("Groups");
        mAllGroupsDatabase.keepSynced(true);
        mGroupsDatabase = mRootRef.child("Users").child(mCurrentUser.getUid()).child("Groups");
        mGroupsDatabase.keepSynced(true);

        mGroupsList.setHasFixedSize(true);
        mGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Groups, ChatsFragment.ChatsViewHolder> groupsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Groups, ChatsViewHolder>(
                Groups.class, R.layout.group_single_layout, ChatsFragment.ChatsViewHolder.class, mGroupsDatabase) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder chatsViewHolder, Groups groups, int position) {

                final String list_group_id = getRef(position).getKey();

                mAllGroupsDatabase.child(list_group_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String groupName = dataSnapshot.child("name").getValue().toString();
                        String groupStatus = dataSnapshot.child("status").getValue().toString();
                        String groupThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                        chatsViewHolder.setName(groupName);
                        chatsViewHolder.setDisplayImage(groupThumbImage, getContext());
                        chatsViewHolder.setStatus(groupStatus);

                        chatsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Open Chat", "Add to Group"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        // Click Event for each item
                                        if (i == 0) {

                                            Intent profileIntent = new Intent(getContext(), GroupProfileActivity.class);
                                            profileIntent.putExtra("group_id", list_group_id);
                                            startActivity(profileIntent);

                                        }

                                        if (i == 1) {

                                            Intent chatGroupIntent = new Intent(getContext(), GroupChatActivity.class);
                                            chatGroupIntent.putExtra("group_id", list_group_id);
                                            chatGroupIntent.putExtra("group_name", groupName);
                                            startActivity(chatGroupIntent);

                                        }

                                        if (i == 2) {

                                            Intent addToGroupIntent = new Intent(getContext(), AddToGroupActivity.class);
                                            addToGroupIntent.putExtra("group_id", list_group_id);
                                            addToGroupIntent.putExtra("group_name", groupName);
                                            startActivity(addToGroupIntent);

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

        mGroupsList.setAdapter(groupsRecyclerViewAdapter);

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatsViewHolder(View itemView) {
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
    }
}
