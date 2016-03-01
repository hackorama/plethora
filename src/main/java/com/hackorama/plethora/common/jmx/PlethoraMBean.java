package com.hackorama.plethora.common.jmx;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import com.hackorama.plethora.common.Log;
import com.hackorama.plethora.common.OptionString;
import com.hackorama.plethora.common.data.Metric;
import com.hackorama.plethora.common.data.Metrics;

/**
 * This dynamic MBean will be the single public MBean interface for Plethora
 *
 * All the metrics exposed by individual application modules (via JMX for Java and Via HTTP for Python, C++, Go etc.)
 * will be aggregated and exposed out for external monitoring tools by this MBean.
 *
 * This is a dynamic MBean since plethora will not know what metrics will be exposed by each module during compile time.
 * The metrics from each module is discovered at run time.
 *
 * @author Kishan Thomas <kishan.thomas@gmail.com>
 *
 */
public class PlethoraMBean implements DynamicMBean {

    private MBeanInfo mbeanInfo = null;

    protected final Metrics metrics;

    public PlethoraMBean(Metrics metrics) {
        this.metrics = metrics;
        buildMBeanInfo();
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        Object value = getMetric(attribute);
        if (value == null) {
            throw new AttributeNotFoundException("No such metric: " + attribute);
        }
        return value;
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            Object value = getMetric(attribute);
            if (value == null) {
                Log.getLogger().warning("Failed to get value for mbean attribute " + attribute);
            } else {
                list.add(new Attribute(attribute, value));
            }
        }
        return list;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        String name = attribute.getName();
        if (getMetric(name) == null) {
            throw new AttributeNotFoundException(name);
        }
        Object value = attribute.getValue();
        setMetricValue(name, value);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList resultlist = new AttributeList();
        for (Attribute attribute : attributes.asList()) {
            String name = attribute.getName();
            Object value = attribute.getValue();
            if (hasMetric(name)) {
                setMetricValue(name, value);
                resultlist.add(new Attribute(name, value));
            }
        }
        return resultlist;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
            ReflectionException {
        if (actionName.equals("plethora") && params != null && params.length == 1 && signature == null
                || signature.length == 0) {
            plethoraHook(params[0]);
        }
        throw new ReflectionException(new NoSuchMethodException(actionName));
    }

    public boolean refreshMBeanInfo() {
        buildMBeanInfo();
        return mbeanInfo != null;
    }

    protected Object getMetric(String name) {
        return metrics.getValue(name);
    }

    protected boolean setMetricValue(String name, Object value) {
        return metrics.setValue(name, value);
    }

    private void buildMBeanInfo() {
        SortedSet<String> names = new TreeSet<String>();
        for (String name : getMetricNames()) {
            names.add(name);
        }
        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[names.size()];
        Iterator<String> iterator = names.iterator();
        for (int i = 0; i < attributes.length; i++) {
            String name = iterator.next();
            if (getMetricType(name) == null) {
                Log.getLogger().severe("Server MBean : Skipping invalid  attribute " + name);
            } else {
                Log.getLogger().info("Server MBean : Adding attribute " + name);
                attributes[i] = new MBeanAttributeInfo(name, getMetricType(name), buildMetricPropertyDescription(name),
                        true, false, false);
            }
        }

        // TODO FIXME
        @SuppressWarnings("rawtypes")
        Constructor[] constructors = this.getClass().getConstructors();
        MBeanConstructorInfo[] constructorInfo = new MBeanConstructorInfo[1];
        constructorInfo[0] = new MBeanConstructorInfo("ServerMBean(): Default Constructor", constructors[0]);

        MBeanParameterInfo[] params = new MBeanParameterInfo[1];
        params[0] = new MBeanParameterInfo("arg", "java.lang.Object", "plethora command");
        MBeanOperationInfo[] operations = { new MBeanOperationInfo("plethora", "Plethora", params, "void",
                MBeanOperationInfo.ACTION) };

        mbeanInfo = new MBeanInfo(this.getClass().getName(), "Plethora Metrics", attributes, constructorInfo,
                operations, null);
    }

    private String buildMetricPropertyDescription(String name) {
        Metric<?> metric = metrics.getMetric(name);
        return MetricDescriptionBuilder.encodeToJSON(metric.getDisplayName(), metric.getDescription(),
                OptionString.build(metric.getMetricType(), metric.getLevel(), metric.canWrite()));

    }

    private Set<String> getMetricNames() {
        return metrics.getNames();
    }

    private String getMetricType(String name) {
        return metrics.getMetricType(name);
    }

    private boolean hasMetric(String name) {
        return metrics.hasMetric(name);
    }

    private void plethoraHook(Object object) {
        // empty, for future invoke hooks
    }

}
