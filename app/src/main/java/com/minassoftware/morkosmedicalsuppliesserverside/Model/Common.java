package com.minassoftware.morkosmedicalsuppliesserverside.Model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.minassoftware.morkosmedicalsuppliesserverside.Remote.APIService;
import com.minassoftware.morkosmedicalsuppliesserverside.Remote.FCMRetrofitClient;
import com.minassoftware.morkosmedicalsuppliesserverside.Remote.IGeoCoordinates;
import com.minassoftware.morkosmedicalsuppliesserverside.Remote.RetrofitClient;

/**
 * Created by Mina on 6/4/2018.
 */

public class Common {
    public static User CurrentUser;
    public static Request CurrentRequest;

    public static final String UPDATE="Update";
    public static final String DELETE="Delete";

    public static final String baseUrl="https://maps.googleapis.com";

    public static final String fcmURL="https://fcm.googleapis.com/";

    public static String convertCode(String code){
        if(code.equals("0")){
            return "submitted";
        }else if(code.equals("1")){
            return "Out for delivery";
        }else{
            return "Delivered";
        }
    }

    public static IGeoCoordinates getGeoCodeService(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static APIService getFCMClient(){
        return FCMRetrofitClient.getClient(fcmURL).create(APIService.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap,int newWidth,int newHeight){
        Bitmap scaledBitmap=Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);

        float scaleX=newWidth/(float)bitmap.getWidth();
        float scaleY=newHeight/(float)bitmap.getHeight();

        float pivotX=0,pivotY=0;

        Matrix scaleMatrix=new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas=new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
}
