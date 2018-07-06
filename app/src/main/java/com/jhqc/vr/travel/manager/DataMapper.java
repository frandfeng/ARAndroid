package com.jhqc.vr.travel.manager;

import com.jhqc.vr.travel.model.MScenicSpot;
import com.jhqc.vr.travel.struct.PScenicSpot;
import com.jhqc.vr.travel.util.OtherUtils;

import java.util.ArrayList;

/**
 * Created by Solomon on 2017/10/17 0017.
 */

public class DataMapper {

    public ArrayList<MScenicSpot> convertP2M(ArrayList<PScenicSpot> spots) {
        ArrayList<MScenicSpot> list = new ArrayList<>(spots.size());
        MScenicSpot spot;
        for (PScenicSpot scenicSpot : spots) {
            spot = new MScenicSpot();
            spot.setId(scenicSpot.getId());
            spot.setName(scenicSpot.getName());
            spot.setIconFileName(scenicSpot.getIcon());
            spot.setDes(scenicSpot.getDetail());
            spot.setDeviceID(scenicSpot.getDeviceID());
            spot.setUuid(scenicSpot.getUuid());

            float[] locs = OtherUtils.splitLocation(scenicSpot.getLocation());
            if (locs != null) {
                spot.setLatitude(locs[0]);
                spot.setLongitude(locs[1]);
            }
            list.add(spot);
        }

        return list;
    }

}
