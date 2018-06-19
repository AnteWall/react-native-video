package com.brentvatne.exoplayer.util;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.TypedByteArray;

public interface DefaultDrmService {

    @POST("/{href}")
    WidevineDrmLicense getDrmLicense(@Path(encode = false, value = "href") String videoId, @Body TypedByteArray typedByteArray);

    @POST("/{href}")
    byte[] sendData(@Path(encode = false, value = "href") String videoId, @Body TypedByteArray data);
}