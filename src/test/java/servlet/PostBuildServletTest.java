package servlet;
import com.contrastsecurity.model.BuildResults;

import com.contrastsecurity.model.Finding;
import com.contrastsecurity.servlet.PostBuildServlet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PostBuildServletTest {
    ArrayList<BuildResults> results;

    PostBuildServlet servlet;
    @Before
    public void setUp() throws Exception {
        results = new ArrayList<BuildResults>();
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-1", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-2", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-3", null));

        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-11", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-12", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-4", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-5", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-6", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-7", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-8", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-9", null));
        results.add(new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-10", null));
        servlet = new PostBuildServlet(null, null);
    }

    @Test
    public void testLimit(){
       assertEquals(10, servlet.limit(results).size());
    }

    @Test
    public void testSort(){
        Collections.reverse(results);
        servlet.sort(results);
        assertEquals("12", results.get(0).getBuildId());
        assertEquals("1", results.get(results.size()-1).getBuildId());
    }
}
