package it.com.contrastsecurity.rest;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.contrastsecurity.rest.contrastconfig;
import com.contrastsecurity.rest.contrastconfigModel;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class contrastconfigFuncTest {

    @Before
    public void setup() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void messageIsValid() {

        String baseUrl = System.getProperty("baseurl");
        String resourceUrl = baseUrl + "/rest/contrastconfig/1.0/message";

        RestClient client = new RestClient();
        Resource resource = client.resource(resourceUrl);

        contrastconfigModel message = resource.get(contrastconfigModel.class);

        assertEquals("wrong message","Hello World",message.getMessage());
    }
}
