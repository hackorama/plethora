package com.hackorama.plethora.common.data;

import com.hackorama.plethora.common.Util;

public final class NameResolver {

    private static final int MODULE_INDEX = 1;
    private static final int METRIC_INDEX = 2;

    private static final String NAME_DELIMITER = ".";
    private static final String ROOT_ELEMENT = "hackorama";
    private static final int ROOT_INDEX = 0;

    public NameResolver() {

    }

    public String resolveMetric(String name) {
        if (validName(name)) {
            return getStringElementsAfter(name, NAME_DELIMITER, METRIC_INDEX);
        }
        return null;
    }

    public String resolveMetricName(String name, String module) {
        if (Util.validNonEmpty(name) && Util.validNonEmpty(module)) {
            return ROOT_ELEMENT + NAME_DELIMITER + module + NAME_DELIMITER + name;
        }
        return null;
    }

    public String resolveModule(String name) {
        if (validName(name)) {
            return getElementAt(name, MODULE_INDEX);
        }
        return null;
    }

    public boolean validName(String name) {
        if (Util.invalidEmpty(name)) {
            return false;
        }
        String root = getElementAt(name, ROOT_INDEX);
        if (Util.invalidEmpty(root)) {
            return false;
        }
        return root.equals(ROOT_ELEMENT);
    }

    private String getElementAt(String text, int index) {
        if (Util.invalidEmpty(text) || Util.invalidIndex(index)) {
            return null;
        }
        // double escape one for java one for regex
        String[] tokens = text.split("\\" + NAME_DELIMITER);
        if (tokens.length > index) { // zero indexed
            return tokens[index];
        }
        return null;
    }

    private String getStringElementsAfter(String mulitElementString, String elementSeparator, int elementIndex) {
        // if delimiter is ".", do double escape, one for java one for regex
        String elementSeparatorRegex = ".".equals(elementSeparator) ? "\\" + elementSeparator : elementSeparator;
        String[] tokens = mulitElementString.split(elementSeparatorRegex);
        StringBuilder result = new StringBuilder();
        for (int i = elementIndex; i < tokens.length; i++) {
            result.append(tokens[i]);
            if (i + 1 < tokens.length) { // skip last one
                result.append(elementSeparator);
            }
        }
        return result.toString();
    }
}
