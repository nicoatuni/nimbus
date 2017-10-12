package nimbus.arcane;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

/**
 * Created by Richard Aldrich on 04/10/2017
 *
 */
public class CreateGroupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mDisplayName;
    private TextInputLayout mStatus;
    private CircleImageView mDisplayImage;

    private Button mChangeImageBtn;
    private Button mCreateGroupBtn;

    private static final int GALLERY_PICK = 1;

    private FirebaseUser mCurrentUser;

    // Related to Firebase Realtime Database
    private DatabaseReference mRootRef;
    private DatabaseReference mGroupDatabase;

    // Related to Firebase Storage
    private StorageReference mImageStorage;

    // Progress Dialog
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mToolbar = (Toolbar) findViewById(R.id.group_profile_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Account Settings
        mDisplayName = (TextInputLayout) findViewById(R.id.group_name_input);
        mStatus = (TextInputLayout) findViewById(R.id.group_status_input);
        mDisplayImage = (CircleImageView) findViewById(R.id.group_profile_image);
        mCreateGroupBtn = (Button) findViewById(R.id.group_create_btn);
        mChangeImageBtn = (Button) findViewById(R.id.group_change_image_btn);

        // Progress Dialog
        mProgress = new ProgressDialog(this);

        // Firebase Storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Firebase Real Time Database
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupDatabase = mRootRef.child("Groups");
        mGroupDatabase.keepSynced(true);

        mCreateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Progress Dialog
                mProgress = new ProgressDialog(CreateGroupActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we creating the group");
                mProgress.show();

                String group_name = mDisplayName.getEditText().getText().toString();
                String group_status = mStatus.getEditText().getText().toString();

                DatabaseReference group_push = mGroupDatabase.push();
                String push_id = group_push.getKey();

                Map groupMap = new HashMap();
                groupMap.put("name", group_name);
                groupMap.put("status", group_status);
                groupMap.put("image", "default");
                groupMap.put("thumb_image", "default");

                create_group(groupMap, push_id, group_name);

            }
        });
    }

    /**
     *    this method will create a new group based on the parameter.
     *    @param groupMap the basic variable needed by the group such as name, status, image, etc.
     *    @param push_id the id of the group on the database.
     *    @param group_name the name of the group.
     */
    public void create_group(Map groupMap, final String push_id, final String group_name) {

        mGroupDatabase.child(push_id).setValue(groupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    mRootRef.child("Users").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String user_name = dataSnapshot.child("name").getValue().toString();

                            Map adminMap = new HashMap();
                            adminMap.put("Groups/" + push_id + "/Members/" + mCurrentUser.getUid() + "/date", ServerValue.TIMESTAMP);
                            adminMap.put("Groups/" + push_id + "/Members/" + mCurrentUser.getUid() + "/name", user_name);
                            adminMap.put("Groups/" + push_id + "/Admin/" + mCurrentUser.getUid(), "");
                            adminMap.put("Users/" + mCurrentUser.getUid() + "/Groups/" + push_id + "/date", ServerValue.TIMESTAMP);

                            mRootRef.updateChildren(adminMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if (databaseError == null) {

                                        mProgress.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, "Group created", Toast.LENGTH_LONG).show();

                                        Intent groupIntent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
                                        startActivity(groupIntent);
                                        finish();

                                    } else {

                                        String error = databaseError.getMessage();

                                        Toast.makeText(CreateGroupActivity.this, error, Toast.LENGTH_SHORT).show();

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
        });
    }
}
