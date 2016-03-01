package com.hackorama.plethora.server.snmp;

/**
 * Transforms the MIB template. The <LABELS> should be in sync with template definition <LABELS>
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class Transformer {

    private final StringBuilder buffer;

    public Transformer() {
        buffer = new StringBuilder();
    }

    public String transformHeaderSection(String name, String date, String url) {
        String urlMessage = url == null ? "" : "at " + url;
        return buffer.append(
                Template.HEADER_SECTION.replace("<NAME>", Template.validDescriptor(name).toUpperCase())
                        .replace("<DATE>", date).replace("<URL>", urlMessage)).toString();
    }

    public ObjectSectionTransformer getObjectSectionTransformer() {
        return new ObjectSectionTransformer();
    }

    public String transformOidLine(String name, String parent, int id) {
        return buffer.append(
                Template.OID_LINE.replace("<NAME>", name).replace("<PARENT>", parent)
                        .replace("<ID>", String.valueOf(id))).toString();
    }

    public String transformEndLine() {
        return buffer.append(Template.END_LINE).toString();
    }

    public String transformAddLine() {
        return buffer.append(Template.EOL).toString();
    }

    public String getContents() {
        return buffer.toString();
    }

    public void append(String transformed) {
        buffer.append(transformed);
    }
}
