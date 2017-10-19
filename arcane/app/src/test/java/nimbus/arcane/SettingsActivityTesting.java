package nimbus.arcane;

import android.app.Activity;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)

/* Unit testing class for Setting Activity class */

public class SettingsActivityTesting {

    @Mock
    private DatabaseReference mockedDatabaseReference;

    private StorageReference mockImageStorage;

    private SettingsActivity mockSettingClass;

    @Before
    /* Setting up what needs to be mocked i.e. the database references */
    public void setUp() {

        mockedDatabaseReference = Mockito.mock(DatabaseReference.class);

        FirebaseDatabase mockedFirebaseDatabase = Mockito.mock(FirebaseDatabase.class);
        when(mockedFirebaseDatabase.getReference()).thenReturn(mockedDatabaseReference);

        mockSettingClass = Mockito.mock(SettingsActivity.class);
        mockSettingClass.setDataBase(mockedFirebaseDatabase.getReference());
    }


    @Test
    /* Test all the functions within the class */
    public void testOnActivityResult() {
        int mockSuccessRequestCode = CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
        int mockSuccessResultCode = Activity.RESULT_OK;
        Intent mockData = new Intent();

        mockSettingClass.onActivityResult(mockSuccessRequestCode, mockSuccessResultCode, mockData);

        verify(mockSettingClass).onActivityResult(mockSuccessRequestCode,
                mockSuccessResultCode, mockData);

    }
}
