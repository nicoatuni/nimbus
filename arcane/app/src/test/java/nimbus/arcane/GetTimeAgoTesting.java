package nimbus.arcane;

import android.location.Location;

import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Richard Aldrich on 12/10/2017.
 */

public class GetTimeAgoTesting {

    @Test
    public void testWhenTimeIs1507601954372L_shouldReturnJustNow() throws Exception {

        GetTimeAgo getTime = new GetTimeAgo(1507601954372L, 1507602004372L);

        assertEquals("just now", getTime.getTimeAgo(1507601954372L, 1507602004372L));
    }

    @Test
    public void testWhenTimeIs1507601904372L_shouldReturnAMinuteAgo() throws Exception {

        GetTimeAgo getTime = new GetTimeAgo(1507601904372L, 1507602004372L);

        assertEquals("a minute ago", getTime.getTimeAgo(1507601904372L, 1507602004372L));
    }

    @Test
    public void testWhenTimeIs1507597004372L_shouldReturnAnHourAgo() throws Exception {

        GetTimeAgo getTime = new GetTimeAgo(1507597004372L, 1507602004372L);

        assertEquals("an hour ago", getTime.getTimeAgo(1507597004372L, 1507602004372L));
    }

    @Test
    public void testWhenTimeIs1507432004372L_shouldReturnYesterday() throws Exception {

        GetTimeAgo getTime = new GetTimeAgo(1507432004372L, 1507602004372L);

        assertEquals("yesterday", getTime.getTimeAgo(1507432004372L, 1507602004372L));
    }
}
