package nimbus.arcane;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

/**
 * Created by Richard Aldrich on 16/09/2017.
 *
 * Last edited by Richard Aldrich on 10/10/2017
 *
 * this class functions when the user want to change the user's profile status
 */
public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSavebtn;

    // Firebase
    private DatabaseReference mRootRef;
    private DatabaseReference mStatusDatabase;
    private DatabaseReference mGroupDatabase;
    private FirebaseUser mCurrentUser;

    // Progress Dialog
    private ProgressDialog mProgressDialog;

    private String group_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStatusDatabase = mRootRef.child("Users").child(current_uid);
        mGroupDatabase = mRootRef.child("Groups");

        // Progress Dialog
        mProgressDialog = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");
        final String database_used = getIntent().getStringExtra("database");
        if (database_used == "group") {

            group_key = getIntent().getStringExtra("group_id");

        }

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mStatus.getEditText().setText(status_value);
        mSavebtn = (Button) findViewById(R.id.status_change_status_btn);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Progress Dialog
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("Please wait while we save the changes");
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();

                if (database_used.equals("user")) {
                    mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mProgressDialog.dismiss();

                                Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                                finish();

                            } else {

                                Toast.makeText(getApplicationContext(), "There was some error in saving Changes", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }

                if (database_used.equals("group")) {
                    mGroupDatabase.child(group_key).child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mProgressDialog.dismiss();

                                Intent groupProfileIntent = new Intent(StatusActivity.this, GroupProfileActivity.class);
                                groupProfileIntent.putExtra("group_id", group_key);
                                startActivity(groupProfileIntent);
                                finish();

                            } else {

                                Toast.makeText(getApplicationContext(), "There was some error in saving Changes", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
            }
        });
    }
}
