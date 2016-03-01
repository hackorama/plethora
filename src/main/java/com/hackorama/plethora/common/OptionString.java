package com.hackorama.plethora.common;

/**
 * 
 * Build and query a character map string holding multiple metric options.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class OptionString {

    private OptionString() {
        // no instances
    }

    /**
     * 
     * Build an 8 char map string of the metric options
     * 
     * 0 - metric type 1 - external write flag 2 - external visibility level 3 - reserved for future use 4 - reserved
     * for future use 5 - reserved for future use 6 - reserved for future use
     * 
     * @return Encoded option char map string
     */
    public static String build(METRIC.TYPE type, METRIC.LEVEL level, boolean writable) {
        char typebit = '0'; // METRIC_TYPE.TEXT
        if (METRIC.TYPE.NUMBER == type) {
            typebit = '1';
        } else if (METRIC.TYPE.BOOLEAN == type) {
            typebit = '2';
        }
        char accessbit = writable ? '1' : '0';
        char levelbit = '0'; // METRIC_LEVEL.PUBLIC
        if (METRIC.LEVEL.LIMITED == level) {
            levelbit = '1';
        } else if (METRIC.LEVEL.INTERNAL == level) {
            levelbit = '2';
        }
        String futurebits = "00000";
        return new StringBuffer().append(typebit).append(accessbit).append(levelbit).append(futurebits).toString();
    }

    public static METRIC.TYPE getType(String options) {
        char typebit = options.charAt(0); // first char
        if (typebit == '1') {
            return METRIC.TYPE.NUMBER;
        } else if (typebit == '2') {
            return METRIC.TYPE.BOOLEAN;
        }
        return METRIC.TYPE.TEXT;
    }

    public static boolean getWritable(String options) {
        char writebit = options.charAt(2); // second char
        if (writebit == '1') {
            return true;
        }
        return false;
    }

    public static METRIC.LEVEL getLevel(String options) {
        char levelbit = options.charAt(3); // third char
        if (levelbit == '1') {
            return METRIC.LEVEL.LIMITED;
        } else if (levelbit == '2') {
            return METRIC.LEVEL.INTERNAL;
        }
        return METRIC.LEVEL.PUBLIC;
    }

}
