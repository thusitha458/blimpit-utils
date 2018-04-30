package org.blimpit.utils.filehandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A class which handles the file uploading and downloading operations
 */
public class BlimpItFileHandler implements FileHandler{

    private static  volatile  BlimpItFileHandler blimpItFileHandler;

    public static final String FILE_UPLOAD_MULTIPART_NAME = "file";

    private BlimpItFileHandler(){

    }

    @Override
    public boolean uploadFileToService(String sourcePath, String serviceURL) {
        File inFile = new File(sourcePath);
        try (FileInputStream fileInputStream = new FileInputStream(inFile);
             CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
            HttpPost postRequest = new HttpPost(serviceURL);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart(FILE_UPLOAD_MULTIPART_NAME, new InputStreamBody(fileInputStream, inFile.getName()))
                    .build();
            postRequest.setEntity(entity);
            HttpResponse response = httpClient.execute(postRequest);
            Integer statusCode = response.getStatusLine().getStatusCode();
            return statusCode.equals(HttpStatus.SC_OK);
        } catch (IOException e) {
            // TODO: log errors instead of printing stack trace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean uploadFileToLocalDst(String sourcePath, String destinationPath) {
        return copyFile(sourcePath, destinationPath);
    }

    @Override
    public boolean uploadFileToaRemoteServer(String sourcePath, String destination) {
        // upload files using ftp
        // destination should be of the format - ftp://username:password@host:port/file_path
        FTPClient ftpClient = new FTPClient();
        try (FileInputStream fileInputStream = new FileInputStream(sourcePath)){
            URL url = new URL(destination);
            connectAndLoginToFTPServer(ftpClient, url);
            return ftpClient.storeFile(url.getFile(), fileInputStream);
        } catch (IOException e) {
            // TODO: log errors instead of printing stack trace
            e.printStackTrace();
        } finally {
            logoutAndDisconnectFromFTPServer(ftpClient);
        }
        return false;
    }

    @Override
    public boolean downloadFileFromRemoteService(String downloadServiceURL, String localPath) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet getRequest = new HttpGet(downloadServiceURL);
            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                InputStream inputStream = response.getEntity().getContent();
                FileUtils.copyInputStreamToFile(inputStream, new File(localPath));
                return true;
            }
        } catch (IOException e) {
            // TODO: log errors instead of printing stack trace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean copyFile(String destination, String newLocation) {
        try {
            File targetFile = new File(destination);
            File newFile = new File(newLocation);
            Files.copy(targetFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            // TODO: log errors instead of printing stack trace
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean downloadFileFromRemoteServer(String remoteLocation, String localPath) {
        // download files using ftp
        // destination should be of the format - ftp://username:password@host:port/file_path
        FTPClient ftpClient = new FTPClient();
        try (FileOutputStream fileOutputStream = new FileOutputStream(localPath)){
            URL url = new URL(remoteLocation);
            connectAndLoginToFTPServer(ftpClient, url);
            return ftpClient.retrieveFile(url.getFile(), fileOutputStream);
        } catch (IOException e) {
            // TODO: log errors instead of printing stack trace
            e.printStackTrace();
        } finally {
            logoutAndDisconnectFromFTPServer(ftpClient);
        }
        return false;
    }

    /**
     * Connect and Login to the ftp server.
     * Example url - ftp://user:password@localhost:21/myDir/myFile.txt
     * @param ftpClient
     * @param url
     * @throws IOException
     */
    private void connectAndLoginToFTPServer(FTPClient ftpClient, URL url) throws IOException {
        ftpClient.connect(url.getHost(), url.getPort());
        String[] userInfo = url.getUserInfo().split(":");
        ftpClient.login(userInfo[0], userInfo[1]);
    }

    /**
     * Logout and disconnect from the ftp server.
     * @param ftpClient
     */
    private void logoutAndDisconnectFromFTPServer(FTPClient ftpClient) {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            // TODO: log errors instead of printing stack trace
            e.printStackTrace();
        }
    }

    /**
     * A method which returns instance of a BlimpIt File
     * @return
     */
    public static FileHandler getInstance(){
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
