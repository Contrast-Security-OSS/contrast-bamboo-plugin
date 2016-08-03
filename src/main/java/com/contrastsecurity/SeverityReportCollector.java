package com.contrastsecurity;

import com.atlassian.bamboo.build.test.TestCollectionResult;
import com.atlassian.bamboo.build.test.TestCollectionResultBuilder;
import com.atlassian.bamboo.build.test.TestReportCollector;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.tests.TestState;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SeverityReportCollector implements TestReportCollector
{
    public TestCollectionResult collect(File file) throws Exception
    {
        TestCollectionResultBuilder builder = new TestCollectionResultBuilder();

        Collection<TestResults> successfulTestResults = Lists.newArrayList();
        Collection<TestResults> failingTestResults = Lists.newArrayList();

        List<String> lines = Files.readLines(file, Charset.forName("UTF-8"));

        for (String line : lines)
        {
            String[] atoms = StringUtils.split(line, '|');
            String suiteName = atoms[0];
            String testName = atoms[1];
            String durationInSeconds = atoms[2];
            String status = atoms[3];

            Double duration = (Double.parseDouble(durationInSeconds) * 1000);

            TestResults testResults = new TestResults(suiteName, testName, duration.toString());
            if ("SUCCESS".equals(status))
            {
                testResults.setState(TestState.SUCCESS);
                successfulTestResults.add(testResults);
            }
            else
            {
                testResults.setState(TestState.FAILED);
                failingTestResults.add(testResults);
            }
        }

        return builder
                .addSuccessfulTestResults(successfulTestResults)
                .addFailedTestResults(failingTestResults)
                .build();
    }

    public Set<String> getSupportedFileExtensions()
    {
        return Sets.newHashSet("tssev"); // this will collect all *.tsresult files
    }
}