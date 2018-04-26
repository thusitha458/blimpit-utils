package org.blimpit.utils.connectors.mysql;

import java.util.HashMap;
import java.util.Map;

/**
 *A  class represents a record of a table in DB
 */
public class Record {

    private int index;

    private Map<String, String> recordAttributes;


    public Record(int index) {
        this.recordAttributes = new HashMap<String, String>();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Map<String, String> getRecordAttributes() {
        return recordAttributes;
    }

    public void addRecordAttribute(String fieldName, String value) {
        this.recordAttributes.put(fieldName, value);
    }
}
