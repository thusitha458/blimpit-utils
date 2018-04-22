package org.blimpit.utils.connectors.mysql;

import org.blimpit.utils.connectors.Connection;

/**
 * Represents actual connection to the DB
 */
class MySQLConnection implements Connection {

    MySQLConnection(String ip, String port, String dbName,
                    String username, String password) {

    }


    public boolean isOpen() {
        return false;
    }
}
