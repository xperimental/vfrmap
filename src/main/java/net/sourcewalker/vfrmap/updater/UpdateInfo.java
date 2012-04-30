package net.sourcewalker.vfrmap.updater;

public class UpdateInfo {

    private static final String DOWNLOAD_URI_ERROR = "null://";
    private static final String DOWNLOAD_URI_NONE = "none://";
    private static final String UNKNOWN_NAME = "UNKNOWN";

    private final int myVersion;
    private final int currentVersion;
    private final String myVersionName;
    private final String currentVersionName;
    private final String downloadUri;

    public int getMyVersion() {
        return myVersion;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public String getMyVersionName() {
        return myVersionName;
    }

    public String getCurrentVersionName() {
        return currentVersionName;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public boolean hasError() {
        return DOWNLOAD_URI_ERROR.equals(downloadUri);
    }

    public boolean hasUpdate() {
        return myVersion < currentVersion;
    }

    public UpdateInfo(int myVersion, int currentVersion, String myVersionName, String currentVersionName, String downloadUri) {
        this.myVersion = myVersion;
        this.currentVersion = currentVersion;
        this.myVersionName = myVersionName;
        this.currentVersionName = currentVersionName;
        this.downloadUri = downloadUri;
    }

    public UpdateInfo(int myVersion, String myVersionName) {
        this(myVersion, 0, myVersionName, UNKNOWN_NAME, DOWNLOAD_URI_NONE);
    }

    public UpdateInfo() {
        this(1, 1, UNKNOWN_NAME, UNKNOWN_NAME, DOWNLOAD_URI_ERROR);
    }
}
