/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.openig.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Represents a duration in english.
 *
 * <code>
 *     6 days
 *     59 minutes and 1 millisecond
 *     1 minute and 10 seconds
 *     42 millis
 * </code>
 */
public class Duration {

    private Long number;
    private TimeUnit unit;

    /**
     * Builds an empty Duration. This is only used when composing Duration together.
     */
    private Duration() {
        this(null, null);
    }

    /**
     * Builds a new Duration.
     * @param number number of time unit (maybe {@literal null}).
     * @param unit TimeUnit to express the duration in (maybe {@literal null}).
     */
    public Duration(final Long number, final TimeUnit unit) {
        this.number = number;
        this.unit = unit;
    }

    /**
     * Builds a new {@link Duration} that will represents the given duration expressed in english.
     *
     * @param value
     *         natural speech duration
     * @throws IllegalArgumentException
     *         if the input string is incorrectly formatted.
     */
    public Duration(final String value) {
        List<Duration> composite = new ArrayList<Duration>();

        // Split around ',' and ' and ' patterns
        String[] fragments = value.split(",| and ");
        for (String fragment : fragments) {

            fragment = fragment.trim();

            if ("".equals(fragment)) {
                throw new IllegalArgumentException("Cannot parse empty duration, expecting '<value> <unit>' pattern");
            }

            // Parse the number part
            int i = 0;
            StringBuilder numberSB = new StringBuilder();
            while (Character.isDigit(fragment.charAt(i))) {
                numberSB.append(fragment.charAt(i));
                i++;
            }

            // Ignore whitespace
            while (Character.isWhitespace(fragment.charAt(i))) {
                i++;
            }

            // Parse the time unit part
            StringBuilder unitSB = new StringBuilder();
            while ((i < fragment.length()) && Character.isLetter(fragment.charAt(i))) {
                unitSB.append(fragment.charAt(i));
                i++;
            }
            Long number = Long.valueOf(numberSB.toString());
            TimeUnit unit = parseTimeUnit(unitSB.toString());

            composite.add(new Duration(number, unit));
        }

        // Merge components of the composite together
        Duration duration = new Duration();
        for (Duration elements : composite) {
            duration.merge(elements);
        }

        number = duration.number;
        unit = duration.unit;
    }

    /**
     * Aggregates this Duration with the given Duration. Littlest {@link TimeUnit} will be used as a common ground.
     *
     * @param duration
     *         other Duration
     */
    private void merge(final Duration duration) {
        // Very first merge, this was empty
        if (unit == null) {
            unit = duration.unit;
            number = duration.number;
        } else {
            // find littlest unit
            // conversion will happen on the littlest unit otherwise we loose details
            if (unit.ordinal() > duration.unit.ordinal()) {
                // Other duration is smaller than me
                number = duration.unit.convert(number, unit) + duration.number;
                unit = duration.unit;
            } else {
                // Other duration is greater than me
                number = unit.convert(duration.number, duration.unit) + number;
            }
        }
    }

    /**
     * Parse the given input string as a {@link TimeUnit}.
     */
    private static TimeUnit parseTimeUnit(final String unit) {
        String lowercase = unit.toLowerCase();

        // @Checkstyle:off

        if ("days".equals(lowercase)) return TimeUnit.DAYS;
        if ("day".equals(lowercase)) return TimeUnit.DAYS;
        if ("d".equals(lowercase)) return TimeUnit.DAYS;

        if ("hours".equals(lowercase)) return TimeUnit.HOURS;
        if ("hour".equals(lowercase)) return TimeUnit.HOURS;
        if ("h".equals(lowercase)) return TimeUnit.HOURS;

        if ("minutes".equals(lowercase)) return TimeUnit.MINUTES;
        if ("minute".equals(lowercase)) return TimeUnit.MINUTES;
        if ("min".equals(lowercase)) return TimeUnit.MINUTES;
        if ("m".equals(lowercase)) return TimeUnit.MINUTES;

        if ("seconds".equals(lowercase)) return TimeUnit.SECONDS;
        if ("second".equals(lowercase)) return TimeUnit.SECONDS;
        if ("sec".equals(lowercase)) return TimeUnit.SECONDS;
        if ("s".equals(lowercase)) return TimeUnit.SECONDS;

        if ("milliseconds".equals(lowercase)) return TimeUnit.MILLISECONDS;
        if ("millisecond".equals(lowercase)) return TimeUnit.MILLISECONDS;
        if ("millisec".equals(lowercase)) return TimeUnit.MILLISECONDS;
        if ("millis".equals(lowercase)) return TimeUnit.MILLISECONDS;
        if ("milli".equals(lowercase)) return TimeUnit.MILLISECONDS;
        if ("ms".equals(lowercase)) return TimeUnit.MILLISECONDS;

        if ("microseconds".equals(lowercase)) return TimeUnit.MICROSECONDS;
        if ("microsecond".equals(lowercase)) return TimeUnit.MICROSECONDS;
        if ("microsec".equals(lowercase)) return TimeUnit.MICROSECONDS;
        if ("micros".equals(lowercase)) return TimeUnit.MICROSECONDS;
        if ("micro".equals(lowercase)) return TimeUnit.MICROSECONDS;
        if ("us".equals(lowercase)) return TimeUnit.MICROSECONDS;

        if ("nanoseconds".equals(lowercase)) return TimeUnit.NANOSECONDS;
        if ("nanosecond".equals(lowercase)) return TimeUnit.NANOSECONDS;
        if ("nanosec".equals(lowercase)) return TimeUnit.NANOSECONDS;
        if ("nanos".equals(lowercase)) return TimeUnit.NANOSECONDS;
        if ("nano".equals(lowercase)) return TimeUnit.NANOSECONDS;
        if ("ns".equals(lowercase)) return TimeUnit.NANOSECONDS;

        // @Checkstyle:on

        throw new IllegalArgumentException(format("TimeUnit %s is not recognized", unit));
    }

    /**
     * Returns the number of {@link TimeUnit} this duration represents.
     *
     * @return the number of {@link TimeUnit} this duration represents.
     */
    public long getValue() {
        return number;
    }

    /**
     * Returns the {@link TimeUnit} this duration is expressed in.
     *
     * @return the {@link TimeUnit} this duration is expressed in.
     */
    public TimeUnit getUnit() {
        return unit;
    }

}
