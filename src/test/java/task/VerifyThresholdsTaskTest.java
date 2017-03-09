package task;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.model.Finding;
import com.contrastsecurity.task.VerifyThresholdsTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class VerifyThresholdsTaskTest {


    @Test
    public void severityEnumTest(){
        RuleSeverity[] severities = {RuleSeverity.MEDIUM, RuleSeverity.HIGH, RuleSeverity.CRITICAL};
        assertArrayEquals(severities, VerifyThresholdsTask.getSeverityList("Medium").toArray());
        RuleSeverity[] severities2 = {RuleSeverity.NOTE,RuleSeverity.LOW,  RuleSeverity.MEDIUM, RuleSeverity.HIGH, RuleSeverity
                .CRITICAL};
        assertArrayEquals(severities2, VerifyThresholdsTask.getSeverityList("Note").toArray());
    }

}
