package org.blimpit.utils.filehandler;

/**
 * A class which handles the file uploading and downloading operations
 */
public class BlimpItFileHandler implements FileHandler{

    private static  volatile  BlimpItFileHandler blimpItFileHandler;


    private BlimpItFileHandler(){

    }

    @Override
    public boolean uploadFileToService(String sourcePath, String serviceURL) {
        return false;
    }

    @Override
    public boolean uploadFileToLocalDst(String sourcePath, String destinationPath) {
        return false;
    }

    @Override
    public boolean uploadFileToaRemoteServer(String sourcePath, String destination) {
        return false;
    }

    @Override
    public boolean downloadFileFromRemoteService(String downloadServiceURL, String localPath) {
        return false;
    }

    @Override
    public boolean copyFile(String destination, String newLocation) {
        return false;
    }

    @Override
    public boolean downloadFileFromRemoteServer(String remotelocation, String localPath) {
        return false;
    }

    /**
     * A method which returns instance of a BlimpIt File
     * @return
     */
    private static FileHandler getInstance(){
        if (blimpItFileHandler == null){
            synchronized (BlimpItFileHandler.class){
                if(blimpItFileHandler == null){
                    blimpItFileHandler = new BlimpItFileHandler();
                }
            }
        }
        return blimpItFileHandler;
    }
}
