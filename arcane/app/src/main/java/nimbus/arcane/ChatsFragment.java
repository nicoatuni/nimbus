package nimbus.arcane;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
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

    private View mMainView;

    private TextView mGroupName;
    private TextView mStatus;
    private CircleImageView mImageView;

    private DatabaseReference mRootRef;
    private DatabaseReference mGroupRef;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
              Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mGroupName = (TextView) mMainView.findViewById(R.id.users_display_name);
        mImageView = (CircleImageView) mMainView.findViewById(R.id.users_image);
        mStatus = (TextView) mMainView.findViewById(R.id.users_status);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupRef = mRootRef.child("Groups");

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("name")) {

                    String groupName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                    mGroupName.setText(groupName);
                    mStatus.setText(userStatus);
                    Picasso.with(getContext()).load(userThumbImage).placeholder(R.drawable.default_avatar).into(mImageView);

                } else {

                    mGroupName.setVisibility(View.INVISIBLE);
                    mStatus.setVisibility(View.INVISIBLE);
                    mImageView.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMainView.findViewById(R.id.group_chat_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent groupIntent = new Intent(getContext(), GroupChatActivity.class);
                groupIntent.putExtra("group_name", mGroupName.getText().toString());
                startActivity(groupIntent);

//                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
//
//                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//
//                builder.setTitle("Select Options");
//                builder.setItems(options, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                        // Click Event for each item
//                        if (i == 0) {
//
//                            Intent profileIntent = new Intent(getContext(), GroupProfileActivity.class);
//                            profileIntent.putExtra("group_name", mGroupName.getText().toString());
//                            startActivity(profileIntent);
//
//                        }
//
//                        if (i == 1) {
//
//                            Intent groupIntent = new Intent(getContext(), GroupChatActivity.class);
//                            groupIntent.putExtra("group_name", mGroupName.getText().toString());
//                            startActivity(groupIntent);
//
//                        }
//
//                    }
//                });
            }
        });
    }
}
