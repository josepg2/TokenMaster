package com.citen.sajeer.tokenmaster;

/**
 * Created by josepg4 on 20/5/17.
 */

public class AdSpaceData {
    private String adSpaceName;
    private String adSpaceDiscription;
    private int adSpaceImageID;


    public AdSpaceData(String adSpaceName, String adSpaceDiscription, int adSpaceImageID) {
        this.adSpaceName = adSpaceName;
        this.adSpaceDiscription = adSpaceDiscription;
        this.adSpaceImageID = adSpaceImageID;
    }

    public String getAdSpaceName() {
        return adSpaceName;
    }

    public String getAdSpaceDiscription() {
        return adSpaceDiscription;
    }

    public int getAdSpaceImageID() {
        return adSpaceImageID;
    }
}
