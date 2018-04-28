package org.blimpit.utils.connectors.mysql;

import org.blimpit.utils.connectors.Connector;

import java.util.Map;

/**
 * A Class which handles MySQL operations
 */
public class MySQLConnector implements Connector {

    private static volatile MySQLConnector mySQLConnector;
    private static MySQLConnection mySQLConnection;

    private MySQLConnector(String ip, String port, String dbName,
                           String username, String password) {

        this.mySQLConnection = new MySQLConnection(ip, port, dbName, username, password);

    }

    /**
     * Returns MySQLConnector connector object
     *
     * @param ip       ip of the DB Server
     * @param port     port of the DB Server
     * @param dbName   Database name that wants to connect
     * @param username username of the DB
     * @param password password of the DB
     * @return MySQLConnector
     *
     */
    public static Connector getInstance(String ip, String port, String dbName,
                                              String username, String password) {
        if (mySQLConnector == null) {
            synchronized (MySQLConnector.class) {
                if (mySQLConnector == null) {
                    mySQLConnector = new MySQLConnector(ip, port, dbName, username, password);
                }
            }
        }
        return mySQLConnector;
    }

    public boolean insert(String collectionName, Map<String, String> recordMap) {
        return false;
    }

    public Record[] read(String table) {
        return new Record[0];
    }


}
