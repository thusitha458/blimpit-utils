package org.blimpit.utils.filehandler;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlimpItFileHandlerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String HTTP_FILE_UPLOAD_URL = "http://localhost:8080/upload";
    private static final String HTTP_FILE_DOWNLOAD_URL = "http://localhost:8080/download";
    private static final String FTP_FILE_UPLOAD_URL = "ftp://user:password@localhost:21/uploadedFile.txt";
    private static final String FTP_FILE_DOWNLOAD_URL = "ftp://user:password@localhost:21/fileToDownload.txt";

    private FileHandler fileHandler;
    private FakeFtpServer fakeFtpServer;
    private String uploadFilePath;
    private String copyFilePath;
    private String downloadedFilePath;
    private String pathForTheCopiedFile;

    @Before
    public void setUp() throws IOException {
        fileHandler = BlimpItFileHandler.getInstance();
        uploadFilePath = temporaryFolder.newFile("uploadFile.txt").getAbsolutePath();
        copyFilePath = temporaryFolder.newFile("fileToCopy.txt").getAbsolutePath();
        downloadedFilePath = temporaryFolder.newFolder().getAbsolutePath() + File.separator + "downloaded.txt";
        pathForTheCopiedFile = temporaryFolder.newFolder().getAbsolutePath() + File.separator + "copiedFile.txt";

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "c:\\data"));

        FileSystem fileSystem = new WindowsFakeFileSystem();
        fileSystem.add(new DirectoryEntry("c:\\data"));
        fileSystem.add(new FileEntry("c:\\data\\fileToDownload.txt", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();
    }

    @After
    public void tearDown() {
        fakeFtpServer.stop();
    }

    @Test
    public void uploadsFileToRestService() {
        wireMockRule.stubFor(requestMatching(request -> {
            boolean isCorrectUrl = request.getUrl().equals("/upload");
            boolean isPostRequest = request.getMethod().equals(RequestMethod.POST);
            boolean isMultipart = request.isMultipart();
            Request.Part part = request.getPart(BlimpItFileHandler.FILE_UPLOAD_DEFAULT_MULTIPART_NAME);
            boolean bodyExists = part.getBody().isPresent();
            boolean uploadedFileExists = part.getHeader("content-disposition")
                    .hasValueMatching(new ContainsPattern("uploadFile.txt"));
            return MatchResult.of(isCorrectUrl && isPostRequest && isMultipart && bodyExists && uploadedFileExists);
        }).willReturn(aResponse()));

        boolean success = fileHandler.uploadFileToService(uploadFilePath, HTTP_FILE_UPLOAD_URL);
        assertTrue(success);
    }

    @Test
    public void receivesWrongStatusWhenUploadingFileToRestService() {
        stubFor(post(urlEqualTo("/upload")).willReturn(aResponse().withStatus(500)));
        boolean success = fileHandler.uploadFileToService(uploadFilePath, HTTP_FILE_UPLOAD_URL);
        assertFalse(success);
    }

    @Test
    public void cannotFindFileWhenUploadingFileToRestService() {
        stubFor(post(urlEqualTo("/upload")).willReturn(aResponse()));
        boolean success = fileHandler.uploadFileToService("non_existing_file", HTTP_FILE_UPLOAD_URL);
        assertFalse(success);
    }

    @Test
    public void receivesFaultWhenUploadingFileToRestService() {
        stubFor(post(urlEqualTo("/upload"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        boolean success = fileHandler.uploadFileToService(uploadFilePath, HTTP_FILE_UPLOAD_URL,
                1000, BlimpItFileHandler.FILE_UPLOAD_DEFAULT_MULTIPART_NAME);
        assertFalse(success);
    }

    @Test
    public void downloadsFileFromRestService() {
        stubFor(get(urlEqualTo("/download"))
                .willReturn(aResponse().withStatus(200).withBodyFile("downloadFile.txt")));
        boolean success = fileHandler.downloadFileFromRemoteService(HTTP_FILE_DOWNLOAD_URL, downloadedFilePath);
        assertTrue(success);
        assertTrue(new File(downloadedFilePath).exists());
    }

    @Test
    public void receivedWrongStatusWhenDownloadingFileFromRestService() {
        stubFor(get(urlEqualTo("/download")).willReturn(aResponse().withStatus(500)));
        boolean success = fileHandler.downloadFileFromRemoteService(HTTP_FILE_DOWNLOAD_URL, downloadedFilePath);
        assertFalse(success);
        assertFalse(new File(downloadedFilePath).exists());
    }

    @Test
    public void invalidFilePathWhenDownloadingFileFromRestService() {
        stubFor(get(urlEqualTo("/download"))
                .willReturn(aResponse().withStatus(200).withBodyFile("downloadFile.txt")));
        String invalidFilePath = ":";
        boolean success = fileHandler.downloadFileFromRemoteService(HTTP_FILE_DOWNLOAD_URL, invalidFilePath);
        assertFalse(success);
        assertFalse(new File(invalidFilePath).exists());
    }

    @Test
    public void receivesFaultWhenDownloadingFileFromRestService() {
        stubFor(get(urlEqualTo("/download"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        boolean success = fileHandler.downloadFileFromRemoteService(HTTP_FILE_DOWNLOAD_URL, downloadedFilePath, 1000);
        assertFalse(success);
        assertFalse(new File(downloadedFilePath).exists());
    }

    @Test
    public void copiesFileToNewLocation() {
        boolean success = fileHandler.copyFile(copyFilePath, pathForTheCopiedFile);
        assertTrue(success);
        assertTrue(new File(pathForTheCopiedFile).exists());
    }

    @Test
    public void cannotFindFileWhenCopyingFileToNewLocation() {
        String fileWhichCannotBeFound = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "does_not_exist.txt";
        boolean success = fileHandler.copyFile(fileWhichCannotBeFound, pathForTheCopiedFile);
        assertFalse(success);
        assertFalse(new File(pathForTheCopiedFile).exists());
    }

    @Test
    public void uploadsFileToLocalDestination() {
        boolean success = fileHandler.uploadFileToLocalDst(uploadFilePath, pathForTheCopiedFile);
        assertTrue(success);
        assertTrue(new File(pathForTheCopiedFile).exists());
    }

    @Test
    public void cannotFindFileWhenUploadingFileToLocalDestination() {
        String fileWhichCannotBeFound = temporaryFolder.getRoot().getAbsolutePath() + File.separator + "does_not_exist.txt";
        boolean success = fileHandler.uploadFileToLocalDst(fileWhichCannotBeFound, pathForTheCopiedFile);
        assertFalse(success);
        assertFalse(new File(pathForTheCopiedFile).exists());
    }

    @Test
    public void uploadsFileToFtpServer() {
        boolean success = fileHandler.uploadFileToaRemoteServer(uploadFilePath, FTP_FILE_UPLOAD_URL);
        assertTrue(success);
        assertTrue(fakeFtpServer.getFileSystem().exists("c:\\data\\uploadedFile.txt"));
    }

    @Test
    public void cannotFindFileWhenUploadingFileToFtpServer() {
        boolean success = fileHandler.uploadFileToaRemoteServer("non_existing_file", FTP_FILE_UPLOAD_URL);
        assertFalse(success);
    }

    @Test
    public void invalidUserInfoWhenUploadingFileToFtpServer() {
        boolean success =
                fileHandler.uploadFileToaRemoteServer(uploadFilePath, "ftp://user:wrong_password@localhost:21/uploadedFile.txt");
        assertFalse(success);
    }

    @Test
    public void downloadsFileFromFtpServer() {
        boolean success =
                fileHandler.downloadFileFromRemoteServer(FTP_FILE_DOWNLOAD_URL, downloadedFilePath);
        assertTrue(success);
        assertTrue(new File(downloadedFilePath).exists());
    }

    @Test
    public void invalidFileWhenDownloadingFileFromFtpServer() {
        boolean success =
                fileHandler.downloadFileFromRemoteServer("ftp://user:password@localhost:21/invalid_file.txt", downloadedFilePath);
        assertFalse(success);
        assertFalse(new File(downloadedFilePath).exists());
    }

    @Test
    public void invalidLocalPathWhenDownloadingFileFromFtpServer() {
        boolean success =
                fileHandler.downloadFileFromRemoteServer(FTP_FILE_DOWNLOAD_URL, ":");
        assertFalse(success);
        assertFalse(new File(downloadedFilePath).exists());
    }

    @Test
    public void invalidUserInfoWhenDownloadingFileFromFtpServer() {
        boolean success =
                fileHandler.downloadFileFromRemoteServer("ftp://user:wrong_password@localhost:21/fileToBeDownloaded.txt", downloadedFilePath);
        assertFalse(success);
        assertFalse(new File(downloadedFilePath).exists());
    }
}
