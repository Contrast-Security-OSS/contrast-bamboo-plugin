package util;

import com.contrastsecurity.util.KeyGenerator;
import org.junit.Test;

import static org.junit.Assert.*;


public class KeyGeneratorTest {

    @Test
    public void generateFromNormalKey(){
        String test = "PROJ-PLAN-JOB1-20";
        String result = KeyGenerator.generate(test);
        assertEquals("PROJ-PLAN-", result);
    }
    @Test
    public void generateFromLongPlan(){
        String test = "PROJ-PLANPLANPLAN-JOB1-20";
        String result = KeyGenerator.generate(test);
        assertEquals("PROJ-PLANPLANPLAN-", result);
    }

}