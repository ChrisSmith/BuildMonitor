package org.collegelabs.buildmonitor.buildmonitor2.util;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 */
public class TimeUtil {

    private static final List<Long> unitsInMs = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1)
    );

    private static final List<String> humanUnits = Arrays.asList(
            "year",
            "month",
            "day",
            "hour",
            "minute",
            "second"
    );

    private static final List<String> pluralHumanUnits = Arrays.asList(
            "years",
            "months",
            "days",
            "hours",
            "minutes",
            "seconds"
    );

    public static String human(Date date) {
        if(date == null){
            return "";
        }

        long ms = new Date().getTime() - date.getTime();
        return human(ms);
    }

    public static String human(long duration) {
        if(duration < 1000){
            return "less than a second ago";
        }

        for (int i = 0; i < unitsInMs.size(); i++) {
            Long current = unitsInMs.get(i);
            long temp = duration / current;

            if (temp > 0) {
                String unit = temp != 1 ? pluralHumanUnits.get(i) : humanUnits.get(i);

                return new StringBuilder(unit.length() + 10)
                        .append(temp)
                        .append(" ")
                        .append(unit)
                        .append(" ago")
                        .toString();
            }
        }

        return duration + " milliseconds ago";
    }
}
