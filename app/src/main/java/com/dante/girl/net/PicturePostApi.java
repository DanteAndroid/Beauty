package com.dante.girl.net;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by yons on 16/12/16.
 */

public interface PicturePostApi {
    //Post示例  http://www.apic.in/anime/33089.htm
    @GET("{type}/page/{page}/")
    Observable<ResponseBody> getPosts(@Path("type") String type, @Path("page") int page);

    @GET("/{post}/{page}")
    Observable<ResponseBody> getPictures(@Path("post") String postUrl, @Path("page")int page);

}
