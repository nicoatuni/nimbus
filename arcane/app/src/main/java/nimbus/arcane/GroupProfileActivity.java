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

public class GroupProfileActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_group_profile);

        mToolbar = (Toolbar) findViewById(R.id.group_profile_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Group Profile");
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
                mProgress = new ProgressDialog(GroupProfileActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we creating the group");
                mProgress.show();

                String group_name = mDisplayName.getEditText().getText().toString();
                String group_status = mStatus.getEditText().getText().toString();

                Map groupMap = new HashMap();
                groupMap.put("Name", group_name);
                groupMap.put("Status", group_status);
                groupMap.put("Image", "");
                groupMap.put("Thumb_Image", "");
                groupMap.put("State", "created");

                create_group(groupMap);

            }
        });

//        mChangeImageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//
//                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
//
//            }
//        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
//
//            Uri imageUri = data.getData();
//
//            CropImage.activity(imageUri).setAspectRatio(1, 1).setMinCropWindowSize(500, 500).start(this);
//
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (resultCode == RESULT_OK) {
//
//                mProgress = new ProgressDialog(GroupProfileActivity.this);
//                mProgress.setTitle("Uploading Image");
//                mProgress.setMessage("Please wait while we upload and process the image");
//                mProgress.setCanceledOnTouchOutside(false);
//                mProgress.show();
//
//                Uri resultUri = result.getUri();
//
//                File thumb_filePath = new File(resultUri.getPath());
//
//                String current_user_id = mCurrentUser.getUid();
//
//                Bitmap thumb_file = null;
//                try {
//                    thumb_file = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToBitmap(thumb_filePath);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                thumb_file.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                final byte[] thumb_byte = baos.toByteArray();
//
//                StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
//                final StorageReference thumb_file_path = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + "jpg");
//
//                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//
//                        if (task.isSuccessful()) {
//
//                            @SuppressWarnings("VisibleForTests")
//                            final String download_url = task.getResult().getDownloadUrl().toString();
//
//                            UploadTask uploadTask = thumb_file_path.putBytes(thumb_byte);
//                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
//
//                                    @SuppressWarnings("VisibleForTests")
//                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();
//
//                                    if (thumb_task.isSuccessful()) {
//
//                                        Map update_hashMap = new HashMap();
//                                        update_hashMap.put("image", download_url);
//                                        update_hashMap.put("thumb_image", thumb_download_url);
//
//                                        mGroupDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//
//                                                if (task.isSuccessful()) {
//
//                                                    mProgress.dismiss();
//                                                    Toast.makeText(GroupProfileActivity.this, "Success Uploading", Toast.LENGTH_LONG).show();
//
//                                                }
//
//                                            }
//                                        });
//
//                                    } else {
//
//                                        Toast.makeText(GroupProfileActivity.this, "Error in Uploading", Toast.LENGTH_LONG).show();
//                                        mProgress.dismiss();
//
//                                    }
//
//                                }
//                            });
//
//                        } else {
//
//                            Toast.makeText(GroupProfileActivity.this, "Error in Uploading", Toast.LENGTH_LONG).show();
//                            mProgress.dismiss();
//
//                        }
//                    }
//                });
//
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//
//                Exception error = result.getError();
//
//            }
//        }
//    }

    public void create_group(Map groupMap) {

        mGroupDatabase.setValue(groupMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Map adminMap = new HashMap();
                    adminMap.put("Groups/Members/" + mCurrentUser.getUid() + "/date", ServerValue.TIMESTAMP);
                    adminMap.put("Groups/Admin/" + mCurrentUser.getUid(), "");

                    mRootRef.updateChildren(adminMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mProgress.dismiss();
                                Toast.makeText(GroupProfileActivity.this, "Group created", Toast.LENGTH_LONG).show();

                                Intent groupIntent = new Intent(GroupProfileActivity.this, GroupChatActivity.class);
                                startActivity(groupIntent);
                                finish();

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(GroupProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                                mRootRef.child("Groups").removeValue();

                            }
                        }
                    });
                }
            }
        });
    }
}
