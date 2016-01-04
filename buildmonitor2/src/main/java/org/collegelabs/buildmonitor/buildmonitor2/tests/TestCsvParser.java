package org.collegelabs.buildmonitor.buildmonitor2.tests;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.collegelabs.buildmonitor.buildmonitor2.util.Ex;
import org.collegelabs.buildmonitor.buildmonitor2.util.Linq;

import timber.log.Timber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 */
public class TestCsvParser {

    public ArrayList<TestResult> parse(InputStream in) throws IOException {
        long start = System.currentTimeMillis();
        Timber.d("starting parsing test csv");

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        reader.readLine(); // headers Order#,Test Name,Status,Duration(ms)
        CSVParser parser = CSVFormat.DEFAULT.parse(reader);

        ArrayList<TestResult> testResults = Linq.toList(parser, TestCsvParser::lift);

        Timber.d("done parsing test csv " + (System.currentTimeMillis() - start) + "ms " + testResults.size() + " rows");

        return testResults;
    }

    private static TestResult lift(CSVRecord csvRecord){
        return new TestResult(
                Ex.tryParse(csvRecord.get(0)),
                csvRecord.get(1),
                csvRecord.get(2),
                Ex.tryParse(csvRecord.get(3))
        );
    }

}
