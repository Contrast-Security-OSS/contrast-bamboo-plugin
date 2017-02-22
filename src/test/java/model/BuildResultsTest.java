package model;


import com.contrastsecurity.model.BuildResults;

import com.contrastsecurity.model.Finding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class BuildResultsTest {
    BuildResults results;

    @Before
    public void setUp() throws Exception {
        Finding finding1 = mock(Finding.class);
        when(finding1.getSeverity()).thenReturn("Medium");
        when(finding1.getType()).thenReturn("authorization-missing-deny");
        when(finding1.getBuildId()).thenReturn("com.contrastsecurity.bambooplugin:PROJ-PLAN-1");

        //assertEquals("1", finding1.getSeverity());
        results = new BuildResults("com.contrastsecurity.bambooplugin:PROJ-PLAN-1", finding1);
    }

    @Test
    public void testId(){
        assertEquals("1", results.getBuildId());
    }

    @Test
    public void testVulnCount(){
        assertEquals(1, results.getMediumCount());
    }

    @Test
    public void addingVulns(){
        Finding finding2 = mock(Finding.class);
        when(finding2.getSeverity()).thenReturn("Low");
        when(finding2.getType()).thenReturn("authorization-missing-deny");
        when(finding2.getBuildId()).thenReturn("com.contrastsecurity.bambooplugin:PROJ-PLAN-1");
        Finding finding3 = mock(Finding.class);
        when(finding3.getSeverity()).thenReturn("Low");
        when(finding3.getType()).thenReturn("authorization-missing-deny");
        when(finding3.getBuildId()).thenReturn("com.contrastsecurity.bambooplugin:PROJ-PLAN-1");
        results.addFinding(finding2);
        assertEquals(1, results.getLowCount());
        results.addFinding(finding3);
        assertEquals(2, results.getLowCount());
    }

    @Test
    public void validJSON(){
        boolean valid;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(results.toString());
            valid = true;
        } catch(Exception e){
            valid = false;
        }
        assertTrue(valid);
    }


}
