package de.uni_kl.hci.abbas.touchauth.Model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TouchEvent {
    public String[][] prevData;
    public String[][] postData;
    public String[][][] midData;

    public int start;
    public int end;


    public String[] strings;

    public static TouchEvent getTouchEventFromString(String raw) {

        Pattern rawPattern = Pattern.compile(rawPatternString);
        Matcher rawMatcher = rawPattern.matcher(raw);
        if (!rawMatcher.find()) return null;

        TouchEvent event = new TouchEvent();
        //event.start = rawMatcher.start();
        //event.end = rawMatcher.end();

        event.start = rawMatcher.regionStart();
        event.end = rawMatcher.regionEnd();

        raw = raw.substring(event.start, event.end);

        String prev = raw.substring(0, 37 * 7);
        String post = raw.substring(raw.length() - 37 * 3);
        String[] mid = null;
        int numOfMid = (raw.length() / 37 - 10) / 6;
        if (numOfMid > 0) {
            mid = new String[numOfMid];
            int k = 37 * 7;
            //int k = 36;
            for (int i = 0; i < numOfMid; ++i) {
                mid[i] = raw.substring(k + i * 37 * 6, k + (i + 1) * 37 * 6);
            }
        }



        Pattern prevPattern = Pattern.compile(prevPatternString);
        Matcher prevMatcher = prevPattern.matcher(prev);

        int count = 0;
        int c = 0;
        while (prevMatcher.find(c)) {
            count++;
            c = prevMatcher.start() + 1;
        }

        event.prevData = new String[count][4];
        prevMatcher = prevPattern.matcher(prev);
        if (prevMatcher.find()) {
            for (int i = 0; i < count; ++i) {
                for (int j = 0; j < 4; ++j) {
                    event.prevData[i][j] = prevMatcher.group(j + 1);
                }
            }
        }


        Pattern postPattern = Pattern.compile(postPatternString);
        Matcher postMatcher = postPattern.matcher(post);

        count = 0;
        c = 0;
        while (postMatcher.find(c)) {
            count++;
            c = postMatcher.start() + 1;
        }

        event.postData = new String[count][4];
        postMatcher = postPattern.matcher(post);
        if (postMatcher.find()) {
            for (int i = 0; i < count; ++i) {
                for (int j = 0; j < 4; ++j) {
                    event.postData[i][j] = postMatcher.group(j + 1);
                }
            }
        }

        event.midData = new String[numOfMid][6][4];
        if (mid != null) {
            Pattern midPattern = Pattern.compile(midPatternString);
            for (int n = 0; n < numOfMid; ++n) {
                Matcher midMatcher = midPattern.matcher(mid[n]);
                if (midMatcher.find()) {
                    for (int i = 0; i < 6; ++i) {
                        for (int j = 0; j < 4; ++j) {
                            event.midData[n][i][j] = midMatcher.group(j + 1);
                        }
                    }
                }
            }
        }

        return event;
    }

    private final static String prevPatternString
            = "\\[\\s+(\\d+\\.\\d+)\\]\\s+(000[0,1,3])\\s+(003a|0035|0036|0039|0002|014a|0000)\\s+([\\w\\d]+)";
    private final static String midPatternString
            = "\\[\\s+(\\d+\\.\\d+)\\]\\s+(000[0,3])\\s+(003a|0035|0036|0039|0002|014a|0000)\\s+([\\w\\d]+)";
    private final static String postPatternString
            = "\\[\\s+(\\d+\\.\\d+)\\]\\s+(000[0,1])\\s+(0000|0002|014a)\\s+([\\w\\d]+)";

    private final static String rawPatternString
            = "\\[\\s+(\\d+\\.\\d+)\\]\\s+(\\w+)\\s+(\\w+)\\s+([\\w\\d]+)";
}
