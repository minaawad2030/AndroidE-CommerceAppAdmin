package com.minassoftware.morkosmedicalsuppliesserverside.Remote;

import com.minassoftware.morkosmedicalsuppliesserverside.Model.MyResponse;
import com.minassoftware.morkosmedicalsuppliesserverside.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Mina on 9/19/2018.
 */

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAck1syRI:APA91bGDvqqfVzHnFa4bQdRSrlFd_Qd0I2Ab1XRPzEBUmtqj8dVlxomjt-7XcXoy9Mc0_gxqoNXz1XBOaajHK9yakuYXgyX7H3IbS-KQ8M2Ia92nZaomCC_-j_hBSL0cri34bU3ROye_"

            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
