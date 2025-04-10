package com.example.ptyxiakh.notifications;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=BC39_82523ASTJ3G_E1tDztfo9gWu5Q118LVL4dgF3Qmf8DNFBPaz9KoXzWIDK1AiD1DSHVEmsaykDj2dX7jSM4"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
