package servlet;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.contrastsecurity.model.BuildResults;

import com.contrastsecurity.model.Finding;
import com.contrastsecurity.servlet.PostBuildServlet;
import com.contrastsecurity.util.KeyGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.java.ao.Query;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PostBuildServletTest {
    ArrayList<BuildResults> results;

    @Mock
    private ActiveObjects activeObjects;
    @Mock
    private TemplateRenderer templateRenderer;

    @InjectMocks
    @Resource
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
        servlet = new PostBuildServlet(templateRenderer, activeObjects);


    }

    @Test
    public void testLimit(){
       assertEquals(10, servlet.limit(results).size());
    }
    @Test
    public void testSmallSetLimit(){
        assertEquals(3, servlet.limit(results.subList(0,3)).size());
    }

    @Test
    public void testSort(){
        Collections.reverse(results);
        servlet.sort(results);
        assertEquals("12", results.get(0).getBuildId());
        assertEquals("1", results.get(results.size()-1).getBuildId());
    }
}
