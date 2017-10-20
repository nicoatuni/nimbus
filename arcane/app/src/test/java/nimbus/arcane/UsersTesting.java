package nimbus.arcane;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)

public class UsersTesting {

    final private String NAMETEST = "unittest";
    final private String STATUS = "stats";
    final private String IMG = "image";
    final private String THUMB = "thumbnail";

    private Users utest;

    @Before
    public void setUp(){
        utest = new Users(NAMETEST,STATUS,IMG,THUMB);
    }

    @Test
    public void testName(){
        assertEquals(utest.getName(),NAMETEST);
    }
    @Test
    public void testStats(){
        assertEquals(utest.getStatus(),STATUS);
    }
    @Test
    public void testImage(){
        assertEquals(utest.getImage(),IMG);
    }
    @Test
    public void testThumb(){
        assertEquals(utest.getThumbImage(),THUMB);
    }
}
