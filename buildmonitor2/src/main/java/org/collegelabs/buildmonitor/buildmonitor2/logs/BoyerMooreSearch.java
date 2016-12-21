package org.collegelabs.buildmonitor.buildmonitor2.logs;

/**
 */
public class BoyerMooreSearch {

    // Modified Boyer Moore from Wikipedia http://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string_search_algorithm

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. If it is not a substring, return -1.
     *
     * @param haystack The string to be scanned
     * @param needle The target string to search
     * @return The start index of the substring
     */
    public static LogSearchResults search(BufferedRandomAccessFile haystack, char[] needle) {
        LogSearchResults results = new LogSearchResults();

        if (needle.length == 0) {
            return results;
        }

        for (int i = 0; i < needle.length; i++) {
            needle[i] = Character.toLowerCase(needle[i]);
        }

        final int previewLength = 50;

        int charTable[] = makeCharTable(needle);
        int offsetTable[] = makeOffsetTable(needle);
        for (int i = needle.length - 1, j; i < haystack.length;) {
            for (j = needle.length - 1; needle[j] == Character.toLowerCase(haystack.getChar(i)); --i, --j) {
                if (j == 0) {
                    int offset = Math.max(0, i - previewLength);
                    int charCount = i + previewLength > haystack.length ? (int) (haystack.length - i) : 200;
                    String preview = new String(haystack.getChars(offset, charCount));
                    int offsetInPreview = i > previewLength ? previewLength : i;
                    results.add(i, preview, offsetInPreview, offsetInPreview + needle.length);

                    break;
                }
            }

            // i += needle.length - j; // For naive method
            i += Math.max(offsetTable[needle.length - 1 - j], charTable[Character.toLowerCase(haystack.getChar(i))]);
        }
        return results;
    }

    /**
     * Makes the jump table based on the mismatched character information.
     */
    private static int[] makeCharTable(char[] needle) {
        final int ALPHABET_SIZE = 256;
        int[] table = new int[ALPHABET_SIZE];
        for (int i = 0; i < table.length; ++i) {
            table[i] = needle.length;
        }
        for (int i = 0; i < needle.length - 1; ++i) {
            table[needle[i]] = needle.length - 1 - i;
        }
        return table;
    }

    /**
     * Makes the jump table based on the scan offset which mismatch occurs.
     */
    private static int[] makeOffsetTable(char[] needle) {
        int[] table = new int[needle.length];
        int lastPrefixPosition = needle.length;
        for (int i = needle.length - 1; i >= 0; --i) {
            if (isPrefix(needle, i + 1)) {
                lastPrefixPosition = i + 1;
            }
            table[needle.length - 1 - i] = lastPrefixPosition - i + needle.length - 1;
        }
        for (int i = 0; i < needle.length - 1; ++i) {
            int slen = suffixLength(needle, i);
            table[slen] = needle.length - 1 - i + slen;
        }
        return table;
    }

    /**
     * Is needle[p:end] a PREFIX of needle?
     */
    private static boolean isPrefix(char[] needle, int p) {
        for (int i = p, j = 0; i < needle.length; ++i, ++j) {
            if (needle[i] != needle[j]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the maximum length of the substring ends at p and is a suffix.
     */
    private static int suffixLength(char[] needle, int p) {
        int len = 0;
        for (int i = p, j = needle.length - 1;
             i >= 0 && needle[i] == needle[j]; --i, --j) {
            len += 1;
        }
        return len;
    }
}
