package ut.com.contrastsecurity;

import org.junit.Test;
import com.contrastsecurity.api.MyPluginComponent;
import com.contrastsecurity.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}