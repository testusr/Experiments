package smeo.experiments.monitoring.platform.jvm;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JmxValueRecorder {
    final List<JmxObjectValueRecorder> valueRecorders = new ArrayList<>();

    public JmxObjectValueRecorder recordObjValues(String jmxObjecName) {
        Optional<JmxObjectValueRecorder> first = valueRecorders.stream().filter(recorder -> recorder.nameAsString.equals(jmxObjecName)).findFirst();
        if (first.isPresent()){
            return first.get();
        } else {
            try {
                JmxObjectValueRecorder newRecorder = new JmxObjectValueRecorder(jmxObjecName);
                valueRecorders.add(newRecorder);
                return newRecorder;
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }


    public void recordValues(Map<String, ValueRecord> data, MBeanServerConnection serverConnection) {
        for (JmxObjectValueRecorder valueRecorder : valueRecorders) {
            valueRecorder.recordValues(data, serverConnection);
        }
    }

    public static class JmxObjectValueRecorder extends AttributeContainer {
        final ObjectName jmxObjectName;
        final List<JmxCompositeDataValueRecord> combinedDataRecords = new ArrayList<>();
        final String nameAsString;

        public JmxObjectValueRecorder(String jmxObjectName) throws MalformedObjectNameException {
            this.jmxObjectName = new ObjectName(jmxObjectName);
            this.nameAsString = jmxObjectName;
        }

        public JmxCompositeDataValueRecord composite(String compositeName) {
            Optional<JmxCompositeDataValueRecord> first = combinedDataRecords.stream()
                    .filter(record -> record.name.equals(compositeName)).findFirst();
            if (first.isPresent()){
                return first.get();
            } else {
                JmxCompositeDataValueRecord newRecord = new JmxCompositeDataValueRecord(compositeName);
                combinedDataRecords.add(newRecord);
                return newRecord;
            }
        }

        public void recordValues(Map<String, ValueRecord> data, MBeanServerConnection serverConnection) {
            List<JmxAttributeValueRecorder> recorders = getRecorders();
            for (JmxAttributeValueRecorder recorder : recorders) {
                recorder.recordAttributeValue(serverConnection, jmxObjectName, data);
            }

            for (JmxCompositeDataValueRecord combinedDataRecord : combinedDataRecords) {
                combinedDataRecord.recordValues(data, serverConnection, jmxObjectName);
            }

        }
    }

    public static class JmxCompositeDataValueRecord extends AttributeContainer {
        final String name;

        public JmxCompositeDataValueRecord(String name) {
            this.name = name;
        }


        public void recordValues(Map<String, ValueRecord> data, MBeanServerConnection serverConnection, ObjectName jmxObjectName) {
            try {
                CompositeData compositeData = (CompositeData) serverConnection.getAttribute(jmxObjectName, name);
                List<JmxAttributeValueRecorder> attributeRecorders = getRecorders();
                for (JmxAttributeValueRecorder attributeRecorder : attributeRecorders) {
                    attributeRecorder.recordAttributeValue(compositeData, data);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static class JmxAttributeValueRecorder {
        final String name;
        final AttributeContainer parentContainer;
        private final ValueRecord.ValueType valueType;
        private String tag;

        public JmxAttributeValueRecorder(String name, ValueRecord.ValueType valueType, AttributeContainer parent) {
            this.name = name;
            this.valueType = valueType;
            this.parentContainer = parent;
        }

        /**
         *
         * @param measurementTag
         * @return the parent container to add more attributes if necessary
         */
        public AttributeContainer as(String measurementTag){
            this.tag = measurementTag;
            return parentContainer;
        }

        public void recordAttributeValue(MBeanServerConnection serverConnection, ObjectName jmxObjectName, Map<String, ValueRecord> data) {
            try {
                Object attribute = serverConnection.getAttribute(jmxObjectName, name);
                ValueRecord valueRecord = data.computeIfAbsent(tag, e-> new ValueRecord());
                if (valueRecord != null){
                    valueRecord.update(valueType, attribute);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void recordAttributeValue(CompositeData compositeData, Map<String, ValueRecord> data) {
            Object value = compositeData.get(name);
            ValueRecord record = data.computeIfAbsent(tag, e-> new ValueRecord());
            try {
                record.update(valueType, value);
            } catch (ClassCastException e){
                System.err.println("wrong class type for '"+name+"'");
                e.printStackTrace();
            }
        }
    }

    public static abstract class AttributeContainer {
        List<JmxAttributeValueRecorder> attributeRecorders = new ArrayList<>();

        public JmxAttributeValueRecorder attribute(String name, ValueRecord.ValueType valueType) {
            Optional<JmxAttributeValueRecorder> first = attributeRecorders.stream().filter(attr -> attr.name.equals(name)).findFirst();
            if (first.isPresent()){
                return first.get();
            } else {
                JmxAttributeValueRecorder newAttributeValueRecorder = new JmxAttributeValueRecorder(name, valueType, this);
                this.attributeRecorders.add(newAttributeValueRecorder);
                return newAttributeValueRecorder;
            }
        }

        List<JmxAttributeValueRecorder> getRecorders(){
            return attributeRecorders;
        }
    }
}
