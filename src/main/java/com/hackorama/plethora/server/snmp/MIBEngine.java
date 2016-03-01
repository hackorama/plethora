package com.hackorama.plethora.server.snmp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.METRIC;
import com.hackorama.plethora.common.Util;
import com.hackorama.plethora.common.data.MetricProperties;
import com.hackorama.plethora.server.MetricService;
import com.hackorama.plethora.server.io.SecureFileWriter;

/**
 * Internal metric model to valid RFC 1155 defined MIB format conversion engine, using template transformation.
 * 
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 * 
 */
public final class MIBEngine {

    private static final String MIB_OID_ENTERPRISE_NAME = "enterprises";
    private static final String MIB_OID_NAME_DEFAULT = "NOT_PROVIDED";
    private static final String MIB_OID_PLETHORA_SUBTREE_ROOT = "metrics";
    private static final int MIB_OID_COMMON_ROOT_COUNT = 6;
    private static final String MIB_OID_DELIMITER = "\\."; // double escaped '.'

    private Map<Integer, String> oidMap = new HashMap<Integer, String>();
    private List<Integer> oidList = new ArrayList<Integer>();
    private final MetricService metricService;
    private final SecureFileWriter secureFileWriter;
    private final String mibUrl;
    private final String webRoot;
    private final String productName;

    private static final Map<METRIC.TYPE, String> METRIC_SYNTAX_MAP = new HashMap<METRIC.TYPE, String>();
    static {
        // Map each metric type to an appropriate syntax.
        // RFC 1155 : The syntax for an object type defines the abstract data
        // structure corresponding to that object type.
        METRIC_SYNTAX_MAP.put(METRIC.TYPE.TEXT, "DisplayString");
        METRIC_SYNTAX_MAP.put(METRIC.TYPE.NUMBER, "INTEGER");
        METRIC_SYNTAX_MAP.put(METRIC.TYPE.BOOLEAN, "INTEGER  {true(1), false(2)}");
    }

    public MIBEngine(MetricService metricService, SecureFileWriter secureFileWriter, String mibUrl, String webRoot,
            String productName) {
        this.metricService = metricService;
        this.secureFileWriter = secureFileWriter;
        this.mibUrl = mibUrl;
        this.webRoot = webRoot;
        this.productName = productName;
    }

    public boolean generateMib(String oidNumbers, String oidNames) {
        makeOidMap(oidNumbers, oidNames);
        return writeMib(buildMib(new Transformer()));
    }

    private void makeOidMap(String oidNumbers, String oidNames) {
        oidMap = new TreeMap<Integer, String>();
        oidList = new ArrayList<Integer>();
        List<Integer> numbers = new ArrayList<Integer>();
        for (String number : oidNumbers.split(MIB_OID_DELIMITER)) {
            numbers.add(Integer.valueOf(number));
        }
        List<String> names = new ArrayList<String>();
        if (oidNames != null) {
            Collections.addAll(names, oidNames.split(MIB_OID_DELIMITER));
        }
        if (numbers.size() > MIB_OID_COMMON_ROOT_COUNT) {
            for (int i = MIB_OID_COMMON_ROOT_COUNT; i < numbers.size(); i++) {
                if (i < names.size()) {
                    oidMap.put(numbers.get(i), names.get(i));
                } else {
                    oidMap.put(numbers.get(i), MIB_OID_NAME_DEFAULT);
                }
                oidList.add(numbers.get(i));
            }
        }
    }

    private String buildMib(Transformer transformer) {
        // header
        transformer.transformHeaderSection(productName, getDate(), mibUrl);
        // object identifier sequence of the private enterprise
        String parent = MIB_OID_ENTERPRISE_NAME;
        for (Integer key : oidList) {
            transformer.transformOidLine(oidMap.get(key), parent, key);
            parent = oidMap.get(key);
        }
        // the metrics sub tree object identifier root
        transformer.transformOidLine(MIB_OID_PLETHORA_SUBTREE_ROOT, parent, 1);
        // the metric modules under the metrics sub tree
        buildModules(transformer, MIB_OID_PLETHORA_SUBTREE_ROOT);
        // add all the metric nodes
        for (String module : metricService.getModuleList()) {
            int id = 1;
            for (String metric : metricService.getModuleMetricNames(module)) {
                buildMetric(transformer, metric, metricService.getMetricProperties(module, metric), module, id++);
            }
        }
        transformer.transformEndLine();
        return transformer.getContents();
    }

    private void buildModules(Transformer transformer, String parent) {
        int id = 1;
        for (String module : metricService.getModuleList()) {
            transformer.transformOidLine(module, parent, id++);
        }
        transformer.transformAddLine();
    }

    private void buildMetric(Transformer transformer, String name, MetricProperties properties, String parent, int id) {
        String description = Util.invalidEmpty(properties.getDescription()) ? "Metric for " + name : properties
                .getDescription();
        transformer.append(transformer.getObjectSectionTransformer().name(name)
                .synatx(getSyntaxByType(properties.getType())).description(description).parent(parent).id(id)
                .transform());
    }

    private String getSyntaxByType(METRIC.TYPE type) {
        return METRIC_SYNTAX_MAP.get(type);
    }

    private boolean writeMib(String content) {
        String filename = buildFileName();
        try {
            secureFileWriter.write(filename, content);
            Log.getLogger().info("SNMP MIB description written to : " + filename);
            return true;
        } catch (FileNotFoundException e) {
            Log.getLogger().log(Level.WARNING, "SNMP MIB write error " + filename, e);
        } catch (IOException e) {
            Log.getLogger().log(Level.WARNING, "SNMP MIB write IO error " + filename, e);
        }
        return false;
    }

    private String buildFileName() {
        return webRoot + File.separatorChar + "snmp" + File.separatorChar + "mib";
    }

    private String getDate() {
        return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date());
    }
}
