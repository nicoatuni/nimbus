package nimbus.arcane;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        if (mCurrentUser != null) {

            mUserRef = mRootRef.child("Users").child(mCurrentUser.getUid());

        }

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("AR_CNE");

        // Tabs
        mViewPager = (ViewPager) findViewById(R.id.main_tab_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tab);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();

        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            send_to_start();

        } else {

            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//            mUserRef.child("online").setValue("false");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {

            FirebaseAuth.getInstance().signOut();
            send_to_start();

        }

        if (item.getItemId() == R.id.main_settings_btn) {

            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);

        }

        if (item.getItemId() == R.id.main_all_btn) {

            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);

        }

        if (item.getItemId() == R.id.main_group_btn) {

            Intent groupIntent = new Intent(MainActivity.this, CreateGroupActivity.class);
            startActivity(groupIntent);

        }

        return true;
    }

    private void send_to_start() {

        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();

    }
}
