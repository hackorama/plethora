package com.hackorama.plethora.server.snmp;

import java.util.Arrays;

/**
 * Transforms the object section defined in the MIB template. The <LABELS> should be in sync with template definition
 * <LABELS>
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class ObjectSectionTransformer {

    private String template = Template.OBJECT_SECTION;

    public ObjectSectionTransformer() {
    }

    public ObjectSectionTransformer name(String name) {
        template = template.replace("<NAME>", Template.validDescriptor(name));
        return this;
    }

    public ObjectSectionTransformer synatx(String syntax) {
        template = template.replace("<SYNTAX>", syntax);
        return this;
    }

    public ObjectSectionTransformer description(String description) {
        template = template.replace("<DESCRIPTION>", prettyFormat(description));
        return this;
    }

    public ObjectSectionTransformer parent(String parent) {
        template = template.replace("<PARENT>", Template.validDescriptor(parent));
        return this;
    }

    public ObjectSectionTransformer id(int id) {
        template = template.replace("<ID>", String.valueOf(id));
        return this;
    }

    public String transform() {
        return template;
    }

    private static String prettyFormat(String description) {
        // add leading offset align for multi-line strings
        String regex = Template.EOL + "(?!" + Template.EOL + "$)";
        return description.replaceAll(regex, getOffset(Template.TAB_SIZE, ' '));
    }

    private static String getOffset(int length, char filler) {
        char[] offset = new char[length];
        Arrays.fill(offset, filler);
        return new String(offset);
    }

}
