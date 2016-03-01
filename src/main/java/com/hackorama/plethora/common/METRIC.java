package com.hackorama.plethora.common;

public final class METRIC {

    public static enum LEVEL {
        PUBLIC, LIMITED, INTERNAL;

        public static LEVEL defaultLevel() {
            return PUBLIC;
        }
    }

    public static enum TYPE {
        TEXT, NUMBER, BOOLEAN, UNSPECIFIED, INVALID;

        public static TYPE defaultType() {
            return NUMBER;
        }
    }

    public static enum ATTRIBUTE {
        NAME, TYPE, LEVEL, DESCRIPTION
    }

    private METRIC() {
        // no public instances
    }
}