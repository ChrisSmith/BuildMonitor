package org.collegelabs.buildmonitor.buildmonitor2.logs;

import java.util.ArrayList;

/**
 */
public class LogSearchResults {
    public ArrayList<LogSearchResult> Results = new ArrayList<LogSearchResult>();

    public void add(long offset, String preview, int previewMatchStart, int previewMatchEnd) {
        LogSearchResult result = new LogSearchResult();
        result.offset = offset;
        result.preview = preview;
        result.previewMatchStart = previewMatchStart;
        result.previewMatchEnd = previewMatchEnd;
        Results.add(result);
    }

    public static class LogSearchResult {
        public String preview;
        public long offset;
        public int previewMatchStart, previewMatchEnd;
    }
}

