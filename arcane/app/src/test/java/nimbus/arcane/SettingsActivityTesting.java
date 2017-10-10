package nimbus.arcane;

import android.app.Application;
import android.location.Location;
import android.test.ApplicationTestCase;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Text;

import java.lang.*;

import de.hdodenhof.circleimageview.CircleImageView;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Richard Aldrich on 10/10/2017.
 */
@RunWith(MockitoJUnitRunner.class)

public class SettingsActivityTesting {

    private TextView displayName;
    private TextView displayStatus;
    private CircleImageView displayImage;

    private Button imageBtn;
    private Button statusBtn;

    private DatabaseReference mRootRef;

    @Before
    public void setUp() throws Exception {

        mRootRef = FirebaseDatabase.getInstance().getReference();

//        displayName = (TextView) R.id.settings_display_name;
//
//        srcPostCode.setText("3055");
//        destPostCode.setText("3010");
//
//        UI.addWidget("CALCULATE_BUTTON", new Button());
//        UI.addWidget("COST_LABEL", costLabel);
//        UI.addWidget("SOURCE_POST_CODE", srcPostCode);
//        UI.addWidget("DESTINATION_POST_CODE", destPostCode);
    }

}
