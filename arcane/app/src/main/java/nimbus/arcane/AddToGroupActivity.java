package nimbus.arcane;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
 * Created by Richard Aldrich on 10/10/2017.
 *
 * This class functions to add one or more friends to a specific group
 */
public class AddToGroupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;

    private DatabaseReference mRootRef;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;

    private Button mAddToGroupBtn;

    private String group_key;
    private String group_name;

    private ArrayList<String> mSelect = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_group);

        mToolbar = (Toolbar) findViewById(R.id.add_to_group_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Add to Group");

        mAddToGroupBtn = (Button) findViewById(R.id.add_to_group_btn);

        group_key = getIntent().getStringExtra("group_id");
        group_name = getIntent().getStringExtra("group_name");

        mUsersList = (RecyclerView) findViewById(R.id.all_users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = mRootRef.child("Users");
        mFriendsDatabase = mRootRef.child("Users").child(mCurrentUser.getUid()).child("Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        // get all of the groups that the current user is/are member(s) of
        FirebaseRecyclerAdapter<Friends, AddToGroupActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, AddToGroupActivity.UsersViewHolder>(
                Friends.class, R.layout.users_single_layout, AddToGroupActivity.UsersViewHolder.class, mFriendsDatabase) {
            @Override
            protected void populateViewHolder(final AddToGroupActivity.UsersViewHolder usersViewHolder, Friends friends, int position) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {

                         final String userName = dataSnapshot.child("name").getValue().toString();
                         String userStatus = dataSnapshot.child("status").getValue().toString();
                         String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                         usersViewHolder.setDisplayName(userName);
                         usersViewHolder.setDisplayImage(userThumbImage, getApplicationContext());
                         usersViewHolder.setDisplayStatus(userStatus);
                         usersViewHolder.setCheckBoxVisibility(true);

                         // add or remove the user when the check box is check or unchecked
                         usersViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {

                                 usersViewHolder.checkBox.setChecked(usersViewHolder.checkBox.isChecked());
//                                 Toast.makeText(AddToGroupActivity.this, "checkbox", Toast.LENGTH_LONG).show();
                                 addToSelection(list_user_id, usersViewHolder.checkBox());
//                                 Log.d("SetCheckBox", "checkbox");

                             }
                         });

                         // add or remove the user when the user is check or unchecked
                         usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View view) {

                                 usersViewHolder.setCheckBox();
//                                 Toast.makeText(AddToGroupActivity.this, list_user_id, Toast.LENGTH_LONG).show();
                                 addToSelection(list_user_id, usersViewHolder.checkBox());
//                                 Log.d("SetCheckBox", "users");

                             }
                         });

                     }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

        mAddToGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSelect != null) {

                    mProgress = new ProgressDialog(AddToGroupActivity.this);
                    mProgress.setTitle("Adding Friend(s) to Group");
                    mProgress.setMessage("Please wait while we add your friend(s) to group");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    for (String selection : mSelect) {

                        addToGroup(selection, group_key);

                    }

                    mProgress.dismiss();

                    Intent groupIntent = new Intent(AddToGroupActivity.this, GroupChatActivity.class);
                    groupIntent.putExtra("group_id", group_key);
                    groupIntent.putExtra("group_name", group_name);
                    startActivity(groupIntent);

                } else {

                    Toast.makeText(AddToGroupActivity.this, "Please select user(s) you want to add to group", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    // a view holder of one user that will be used in the recycler view
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CheckBox checkBox;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            checkBox = (CheckBox) mView.findViewById(R.id.users_check_box);

        }

        public void setDisplayName(String name) {

            TextView userName_view = (TextView) mView.findViewById(R.id.users_display_name);
            userName_view.setText(name);

        }

        public void setDisplayStatus(String status) {

            TextView userStatus_view = (TextView) mView.findViewById(R.id.users_status);
            userStatus_view.setText(status);

        }

        public void setDisplayImage(String thumb_image, Context ctx) {

            CircleImageView userImage_view = (CircleImageView) mView.findViewById(R.id.users_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage_view);

        }

        public void setCheckBoxVisibility(boolean value) {

            if (value) {

                checkBox.setVisibility(View.VISIBLE);

            }
        }

        public void setCheckBox() {

            if (checkBox.isChecked()) {

                checkBox.setChecked(false);

            } else {

                checkBox.setChecked(true);

            }
        }

        public boolean checkBox() {

            if (checkBox.isChecked()) {

                return true;

            } else {

                return false;

            }

        }
    }

    /**
     *    this method will add a user to an array list if the check variable is true and remove the
     *    user from the array list if the check variable is false
     *    @param user_id the user id that will be added or removed from the array list.
     *    @param check the boolean value that will specify whether the user will be added or removed.
     */
    public void addToSelection(String user_id, boolean check) {

        if (check) {

            mSelect.add(user_id);

        } else {

            mSelect.remove(user_id);

        }

    }

    /**
     *    this method will add a specific user to be added to a specific group.
     *    @param user_id the user id that will be added to a group.
     *    @param group_id the group id that belong to the group that the user will be added to.
     */
    public void addToGroup(final String user_id, final String group_id) {

        mRootRef.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String user_name = dataSnapshot.child("name").getValue().toString();

                Map friendsMap = new HashMap();
                friendsMap.put("Groups/" + group_id + "/Members/" + user_id + "/date", ServerValue.TIMESTAMP);
                friendsMap.put("Groups/" + group_id + "/Members/" + user_id + "/name", user_name);
                friendsMap.put("Users/" + user_id + "/Groups/" + group_id + "/date", ServerValue.TIMESTAMP);

                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null) {

                            Toast.makeText(AddToGroupActivity.this, "Friend added to group", Toast.LENGTH_LONG).show();

                        } else {

                            String error = databaseError.getMessage();

                            Toast.makeText(AddToGroupActivity.this, error, Toast.LENGTH_SHORT).show();

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
