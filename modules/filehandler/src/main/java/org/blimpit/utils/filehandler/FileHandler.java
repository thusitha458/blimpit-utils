package org.blimpit.utils.filehandler;

/**
 * An interface having the file handling APIs.
 */
public interface FileHandler {

    /**
     * Upload file to a given service running on the
     * @param sourcePath
     * @param
     * @return status of the uploading
     */
  boolean uploadFileToService(String sourcePath, String serviceURL);

    /**
     * Upload file to a local destination
     * @param sourcePath
     * @param destinationPath
     * @return status of the file uploading
     */
  boolean uploadFileToLocalDst(String sourcePath, String destinationPath);

  /**
   * Upload file to a remote location
   * @param sourcePath
   * @param destination
   * @return status of the file uploading
   */
  boolean uploadFileToaRemoteServer(String sourcePath, String destination);

    /**
     * Download file from remote location by calling remote web service
     * @param downloadServiceURL
     * @param localPath
     * @return status of the file downloading
     */
  boolean downloadFileFromRemoteService(String downloadServiceURL, String localPath);

    /**
     * Copy file from one location to another location locally
     * @param destination
     * @param newLocation
     * @return status of the file downloading
     */
  boolean copyFile(String destination, String newLocation);


    /***
     * Download file from remote server
     * @param remotelocation
     * @param localPath
     * @return status of the file downloading
     */
  boolean downloadFileFromRemoteServer(String remotelocation, String localPath);
}
