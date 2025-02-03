package com.example.back.service.impl;

public class RouteServiceImpl {
    private static final String BASE_URL = "https://api.odsay.com/v1/api/";
    private static final String ODsay_APIKEY = BuildConfig.ODsay_APIKEY;
    private final Gson gson = new Gson();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
}
