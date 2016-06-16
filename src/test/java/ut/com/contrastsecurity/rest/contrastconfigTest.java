package ut.com.contrastsecurity.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.contrastsecurity.rest.contrastconfig;
import com.contrastsecurity.rest.contrastconfigModel;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.GenericEntity;

public class contrastconfigTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {
        contrastconfig resource = new contrastconfig();

        Response response = resource.getMessage();
        final contrastconfigModel message = (contrastconfigModel) response.getEntity();

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
