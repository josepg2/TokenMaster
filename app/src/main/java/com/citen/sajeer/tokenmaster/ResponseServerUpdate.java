package com.citen.sajeer.tokenmaster;

import java.util.List;

/**
 * Created by josepg4 on 11/6/17.
 */

public class ResponseServerUpdate {

    private int status;
    private int adSpaceId;
    private List<String> unknownFiles;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAdSpaceId() {
        return adSpaceId;
    }

    public void setAdSpaceId(int adSpaceId) {
        this.adSpaceId = adSpaceId;
    }

    public List<String> getUnknownFiles() {
        return unknownFiles;
    }

    public void setUnknownFiles(List<String> unknownFiles) {
        this.unknownFiles = unknownFiles;
    }
}
