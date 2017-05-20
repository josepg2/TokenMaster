package com.citen.sajeer.tokenmaster;

/**
 * Created by josepg4 on 20/5/17.
 */

public class AdData {

    private String displayName;
    private String fileName;
    private String directoryPath;
    private int adSpaceId;

    AdData(){

    }

    AdData(String displayName, String fileName, String directoryPath, int adSpaceId){
        this.displayName = displayName;
        this.fileName = fileName;
        this.directoryPath = directoryPath;
        this.adSpaceId = adSpaceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public int getAdSpaceId() {
        return adSpaceId;
    }

    public void setAdSpaceId(int adSpaceId) {
        this.adSpaceId = adSpaceId;
    }
}
