package org.blimpit.utils.connectors;

import org.blimpit.utils.connectors.mysql.Record;

import java.util.Map;

/**
 * An interface which describes APIs for MySQL DB operations
 */
public interface Connector {


    /**
     * insert values to table
     *
     * @param collectionName Name of the table or collection
     * @param recordMap      filedName and value pairs
     * @return status of the operation
     */
    boolean insert(String collectionName, Map<String, String> recordMap);


    /**
     * Returns records of the table
     * @param table Table Name
     * @return Record
     */
    Record[]  read(String table);
}
