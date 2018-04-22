package org.blimpit.utils.loghandler;

/**
 * An interface which describes Log handling APIs.
 */
public interface LogHandler {

    /**
     * Log the given activity to the under directory name=username
     * given base file path
     *
     * @param userName
     * @param activity
     */
    void logMessage(String userName, String activity);

    /***
     *Store the activity in the given DB
     * @param username
     */
    void storeLogInDB(String username, String activity);

    /**
     * Read the log files and retrieve the contain in String format
     *
     * @param beginTimeStamp
     * @param endTimeStamp
     * @param userName
     * @return
     */
    String getLogs(double beginTimeStamp, double endTimeStamp, String userName);


    /**
     * Get Logs from DB
     *
     * @param beginTimeStamp
     * @param endTimeStamp
     * @param userName
     * @return
     */
    String getLogsFromDB(double beginTimeStamp, double endTimeStamp, String userName);
}
