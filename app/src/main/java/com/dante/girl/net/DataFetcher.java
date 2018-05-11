package com.dante.girl.net;

import android.text.TextUtils;
import android.util.Log;

import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.picture.CustomPictureFragment;
import com.dante.girl.utils.DateUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.schedulers.Schedulers;

import static com.dante.girl.net.API.A_BASE;
import static com.dante.girl.net.API.MZ_BASE;
import static com.dante.girl.net.API.TYPE_DB_RANK;

/**
 * Help to get&fetch image resources from api
 */

public class DataFetcher {
    private static final String TAG = "DataFetcher";
    private final NetService netService;
    public String url; //source url
    public String type; //picture type
    private int page;

    public DataFetcher(String url, String type, int page) {
        this.url = url;
        this.type = type;
        this.page = page;
        netService = NetService.getInstance(url);

    }

    public Observable<List<Image>> getGank() {
        return netService.getGankApi().get(CustomPictureFragment.LOAD_COUNT, page)
                .subscribeOn(Schedulers.computation())
                .filter(listResult -> !listResult.error)
                .map(listResult -> {
                    List<Image> data = new ArrayList<>();
                    for (Image image :
                            listResult.results) {
                        if (!DataBase.hasImage(image.url)) {
                            data.add(image);
                        } else {

                        }
                    }
                    return data;
                });
    }

    public Observable<List<Image>> getDouban() {
        Observable<ResponseBody> data;
        if (TYPE_DB_RANK.equals(type)) {
            data = netService.getDbApi().getRank(page);
        } else {
            data = netService.getDbApi().get(type, page);
        }
        return data
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
//                            Elements elements = document.select("div[class=thumbnail] > div[class=img_single] > a > img");
                        Elements elements = document.select("div[class=thumbnail] div[class=img_single] img");
                        final int size = elements.size();
                        for (int i = 0; i < size; i++) {
                            String src = elements.get(i).attr("src").trim();
                            if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                                continue;
                            }
                            images.add(new Image(src, type));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });
    }

    private boolean isPictureUrl(String src) {
        if (TextUtils.isEmpty(src)) {
            return false;
        }
        return src.contains(".jpg") || src.contains(".png");
    }


    public Observable<List<Image>> getPosts(String baseUrl) {
        //get all posts' pictures
        return netService.getPicturePostApi().getPosts(type, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
                        switch (baseUrl) {
                            case A_BASE:
                                parseAPosts(images, document);
                                break;
                            case MZ_BASE:
                                parseMzPosts(images, document);
                                break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });

    }

    private void parseMzPosts(List<Image> images, Document document) {
        Elements posts = document.select("div[class=postlist] li");
        for (int i = 0; i < posts.size(); i++) {
            Element a = posts.get(i).selectFirst("a");
            String postUrl = a.attr("href").replace(API.MZ_BASE, "");
            Element element = a.selectFirst("img");
            String title = element.attr("alt");
            String src = element.attr("data-original");
            int width = Integer.parseInt(element.attr("width"));
            int height = Integer.parseInt(element.attr("height"));
            String time = posts.get(i).selectFirst("span[class=time]").text();
            Date date = DateUtil.parseStandardDate(time);
            if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                continue;
            }
            Image image = new Image(src, type);
            image.setInfo(postUrl);
            image.setWidth(width);
            image.setHeight(height);
            image.setPublishedAt(date);
            image.setReferer(String.format("%s%s/page/%s", MZ_BASE, type, page));
            image.setTitle(title);
            images.add(image);
        }

    }

    private void parseAPosts(List<Image> images, Document document) {
        Elements posts = document.select("div[ref=postList] div[class=pos-r pd10 post-list box mar10-b content-card grid-item]");

//                            final int size = elements.size();
        Log.i(TAG, "getPosts: " + type + posts.size());
//                            String url = elements.last().select("img").first().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getPosts: find saved image!");
//                                return null;//最后一个元素在数据库里已经保存了，那么不需要继续解析。
//                            }

        for (int i = 0; i < posts.size(); i++) {
            Element post = posts.get(i);
            Element thumb = post.selectFirst("div[style]");
            Element link = post.selectFirst("h2[class=entry-title]");
            if (thumb == null || link == null) {
                continue;
            }
            String src = thumb.attr("style").split("url\\(\'")[1].replace("')", "");
            src = dealWithImgUrl(src);
            String title = link.selectFirst("a").text();
            String postUrl = link.selectFirst("a").attr("href");

            if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                continue;
            }

            Image image = new Image(src, type);
            image.setInfo(postUrl.replace(API.A_BASE, ""));
            image.setReferer(postUrl);
            image.setTitle(title);
            images.add(image);
        }
    }

    public Observable<List<Image>> getPicturesOfPost(String baseUrl, String info) {
        //get all images in this post
        return netService.getPicturePostApi().getPictures(info, page)
                .subscribeOn(Schedulers.computation())
                .map(responseBody -> {
                    List<Image> images = new ArrayList<>();
                    try {
                        Document document = Jsoup.parse(responseBody.string());
                        switch (baseUrl) {
                            case A_BASE://a区
                                parseA(info, images, document);
                                break;
                            case MZ_BASE://妹子图
                                parseMZ(info, images, document);
                                break;
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return images;
                });

    }

    private void parseMZ(String postUrl, List<Image> images, Document document) {
        Elements aElements = document.select("div[class=pagenavi] a");
        int totalPage = 1;
        if (aElements != null && aElements.size() > 3) {
            String pageStr = aElements.get(aElements.size() - 2).text();
            if (!TextUtils.isEmpty(pageStr) && TextUtils.isDigitsOnly(pageStr)) {
                totalPage = Integer.parseInt(pageStr);
            }
        }

        String src = document.getElementsByClass("main-image").first().selectFirst("img").attr("src");
        for (int index = 1; index < totalPage + 1; index++) {
            String url;
            if (index < 10) {
                url = src.replace("01.", "0" + index + ".");
            } else {
                url = src.replace("01.", "" + index + ".");
            }
            String refer = String.format(Locale.getDefault(), "%s%s/%d", API.MZ_BASE, postUrl, index);
            Image image = new Image(url, type);
            image.setTotalPage(1);//无需loadmore
            image.setReferer(refer);
            images.add(image);
        }

    }

    private void parseA(String info, List<Image> images, Document document) {
        Elements article = document.select("div[id=entry-content]");

        Elements elements = article.select("img[src]");

        Elements fenye = article.select("div[class=page-links] a");
        Log.d(TAG, "parseA: " + elements.size() + " " + fenye.size());
        int totalPage = 1;
        if (fenye.size() >= 1) {
            String pageStr = fenye.get(fenye.size() - 1).text();
            Log.d(TAG, "parseA: fenye" + pageStr);
            if (!TextUtils.isEmpty(pageStr) && TextUtils.isDigitsOnly(pageStr)) {
                totalPage = Integer.parseInt(pageStr);
            }
        }

        final int size = elements.size();
//                            String url = elements.last().attr("src");
//                            if (DataBase.getByUrl(url) != null) {
//                                Log.i(TAG, "getPicturesOfPost: find saved image!");
//                                return null;
//                            }
        for (int i = 0; i < size; i++) {
            String src = elements.get(i).attr("src").trim();
            src = dealWithImgUrl(src);
            String width = elements.get(i).attr("width");
            String height = elements.get(i).attr("height");
            if (!isPictureUrl(src) || DataBase.hasImage(src)) {
                continue;
            }
            Image image = new Image(src, type);
            if (!width.isEmpty() && TextUtils.isDigitsOnly(width)) {
                image.setWidth(Integer.valueOf(width));
            }
            if (!height.isEmpty() && TextUtils.isDigitsOnly(height)) {
                image.setHeight(Integer.valueOf(height));
            }
            image.setReferer(API.A_BASE + info);
            image.setTotalPage(totalPage);
            images.add(image);
        }
    }

    private String dealWithImgUrl(String src) {
//        if (BuildConfig.DEBUG) return src;
//        if (src.endsWith("!orgin")) {
//            src = src.replace("!orgin", "");
//        } else if (src.endsWith("!acgfiindex")) {
//            src = src.replace("!acgfiindex", "");
//
//        } else if (!src.endsWith(".jpg") && !src.endsWith("png")) {
//            if (src.contains(".jpg")) {
//                src = src.substring(0, src.lastIndexOf(".jpg") + 4);
//            } else if (src.contains(".png")) {
//                src = src.substring(0, src.lastIndexOf(".png") + 4);
//            }
//        }
        return src;
    }
    /*
        Elements links = doc.select("a[href]"); // 具有 href 属性的链接

        Elements pngs = doc.select("img[src$=.png]");//所有引用png图片的元素

        Element masthead = doc.select("div.masthead").first();  // 找出定义了 class=masthead 的元素

        Elements resultLinks = doc.select("h3.r > a"); // direct a after h3
     */

}
