package nimbus.arcane;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Observable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.reactivestreams.Subscriber;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    private Toolbar mToolbar;

    // Progress Dialog
    private ProgressDialog mRegProgress;

    // Related to Firebase Authentication
    private FirebaseAuth mAuth;

    // Related to Firebase Real Time Database
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Registeration Field
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    mRegProgress.setTitle("Signing In");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name, email, password);

                } else {

                    if (TextUtils.isEmpty(display_name)) {
                        Toast.makeText(RegisterActivity.this, "The Field Display Name cannot be empty", Toast.LENGTH_LONG).show();
                    }

                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(RegisterActivity.this, "The Field Email cannot be empty", Toast.LENGTH_LONG).show();
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(RegisterActivity.this, "The Field Password cannot be empty", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }

    public void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If sign in succeeds move to the main activity. If sign in fails, display
                // a message to the user.
                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> userMap = new HashMap<String, String>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there, I'm using ARCANE App !");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("device_token", deviceToken);
                    userMap.put("last_seen", "");
                    userMap.put("online", "false");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                mRegProgress.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }
                        }
                    });

                } else {

                    mRegProgress.hide();
//                            Toast.makeText(RegisterActivity.this, "Cannot Sign in. Please check the form and try again", Toast.LENGTH_LONG).show();

                    String error = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        error = "Weak Password!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error = "Invalid Email!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        error = "Existing Account!";
                    } catch (Exception e) {
                        error = "Unknown Error!";
                        e.printStackTrace();
                    }
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();

                }
            }
        });
    }
}
