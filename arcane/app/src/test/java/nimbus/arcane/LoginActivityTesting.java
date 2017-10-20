package nimbus.arcane;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)

/* Testuing unit for Login Activity class */

public class LoginActivityTesting {

    private DatabaseReference mockedDatabaseReference;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private Task<AuthResult> mockAuthTask;

    @Mock
    private AuthResult mockAuthResult;

    @Captor
    private ArgumentCaptor<OnCompleteListener> testOnCompleteListener;

    @Before
    /* Mocking all nessecary database references */
    public void setUp() throws Exception {
        setupTask(mockAuthTask);

        mockedDatabaseReference = Mockito.mock(DatabaseReference.class);

        mockAuth = Mockito.mock(FirebaseAuth.class);

        FirebaseDatabase mockedFirebaseDatabase = Mockito.mock(FirebaseDatabase.class);
        when(mockedFirebaseDatabase.getReference()).thenReturn(mockedDatabaseReference);
        when(mockAuth.createUserWithEmailAndPassword("email", "password")).thenReturn(mockAuthTask);
        mockAuthTask = Mockito.mock(Task.class);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
    }

    private <T> void setupTask(Task<T> task) {
        when(task.addOnCompleteListener(testOnCompleteListener.capture())).thenReturn(task);
    }

    @Test
    /* Testing the functions within the class */
    public void createNewUser() throws Exception {
        String mockEmail = "email";
        String mockPassword= "pass";
        LoginActivity loginActivity = Mockito.mock(LoginActivity.class);
        loginActivity.setFireBaseAuth(mockAuth);
        loginActivity.login_user( mockEmail, mockPassword);
        verify(loginActivity).login_user(mockEmail, mockPassword);
    }
}
