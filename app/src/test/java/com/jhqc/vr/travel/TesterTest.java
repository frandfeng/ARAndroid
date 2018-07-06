package com.jhqc.vr.travel;

import android.content.Context;
import android.test.mock.MockContext;

import com.google.gson.Gson;
import com.jhqc.vr.travel.algorithm.LocationAler;
import com.jhqc.vr.travel.manager.DataLoader;
import com.jhqc.vr.travel.model.MConfig;
import com.jhqc.vr.travel.unity.UnityBridgeHandler;
import com.jhqc.vr.travel.unity.UnityConstants;
import com.jhqc.vr.travel.unity.model.UGpsLocationInfo;
import com.jhqc.vr.travel.unity.model.UIntelligentState;
import com.jhqc.vr.travel.util.OtherUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by Solomon on 2017/10/17 0017.
 */
public class TesterTest {
    Context context;

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
//        context.mo
    }

    @Test
    public void test() {
//        System.out.print(111);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis());
                System.out.println(context);
//                System.out.println(ConfigManager.get(context).loadConfig());


            }
        });
        thread.start();

        MConfig mConfig = new MConfig();
        mConfig.setConfigID("yiheyuan");
        mConfig.setMapFileName("yiheyuan.jpg");
        mConfig.setLocTopL(new float[]{116.267344f, 40.009828f});
        mConfig.setLocTopR(new float[]{116.289739f,40.009828f});
        mConfig.setLocBottomL(new float[]{116.267344f, 39.985787f});
        mConfig.setLocBottomR(new float[]{116.289739f,39.985787f});

        Gson gson = new Gson();
        ArrayList list = new ArrayList();
        list.add(mConfig);
        String j = gson.toJson(list);
        System.out.println(j);



//        String json = "{\"params\":{\"state\":\"kCLAuthorizationStatusAuthorizedAlways\"}}";
//        UGpsLocationInfo info = new UGpsLocationInfo();
//        info.latitude = (float) 40.25463155f;
//        info.longitude = (float) 104.2354157f;
//        System.out.println(OtherUtils.packUnityJsonData(DataLoader.classToJson(info)).toString());
//        System.out.println(OtherUtils.packUnityJsonData(DataLoader.classToJson(new UIntelligentState(false))).toString());


        try{

            float point[]=new float[3];
            LocationAler loc = new LocationAler();

            //获得坐标
            point[0] = 200;
            point[1] = 200;
            point[2] = (float) 1.5f;
            loc.set_point(point,0);

            point[0] = 210;
            point[1] = 550;
            point[2] = 1.0f;
            loc.set_point(point,1);

            point[0] = 500;
            point[1] = 200;
            point[2] = 1.5f;
            loc.set_point(point,2);

            point[0] = 500;
            point[1] = 200;
            point[2] = 2.5f;
            loc.set_point(point,3);

            //distance
            loc.set_distance(4.5f,0);
            loc.set_distance(5.92f,1);
            loc.set_distance(3.15f,2);
            loc.set_distance(1.6f,3);

            //calc
            float x[] = loc.calc();
            if (x == null)
            {
                System.out.println("fail");
            }
            else
            {
                System.out.println(x[0]+","+x[1]+","+ x[2]);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }

        UUID u = UUID.randomUUID();
        System.out.println("原："+u);
        UUID uuid = UUID.fromString(u.toString());
        System.out.println("后："+uuid);
    }

}