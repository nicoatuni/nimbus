package nimbus.arcane;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class SelectGroupActivity extends AppCompatActivity {
    private ArrayList<String> availableGroupIDList=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);
        availableGroupIDList= (ArrayList<String>) getIntent().getExtras().get("availableGroup");
        //Toast.makeText(this, availableGroupIDList.toString(), Toast.LENGTH_LONG).show();


    }
}
