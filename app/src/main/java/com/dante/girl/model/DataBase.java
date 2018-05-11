package com.dante.girl.model;


import com.dante.girl.base.Constants;
import com.dante.girl.net.API;
import com.dante.girl.utils.SpUtil;

import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Deals with cache, data
 */
public class DataBase {
    private static final String TAG = "DataBase";

    private static Realm initRealm(Realm realm) {
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    public static <T extends RealmObject> void save(Realm realm, List<T> realmObjects) {
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObjects);
        realm.commitTransaction();
    }


    public static <T extends RealmObject> T getById(Realm realm, int id, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).equalTo(Constants.ID, id).findFirst();
    }

    private static <T extends RealmObject> RealmResults<T> findAll(Realm realm, Class<T> realmObjectClass) {
        realm = initRealm(realm);
        return realm.where(realmObjectClass).findAll();
    }

    public static void save(Realm realm, RealmObject realmObject) {
        if (realmObject == null) {
            return;
        }
        realm = initRealm(realm);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(realmObject);
        realm.commitTransaction();
    }

    public static boolean isVIP(String deviceId) {
        if (deviceId.isEmpty()) {
            return false;
        }
        return SpUtil.getString("vip").contains(deviceId);
    }

    public static Image getById(Realm realm, int id) {
        return getById(realm, id, Image.class);
    }

    public static boolean hasImage(String url) {
        return getByUrl(null, url) != null;
    }


    public static Image getByUrl(Realm realm, String url) {
        realm = initRealm(realm);
        return realm.where(Image.class).equalTo(Constants.URL, url).findFirst();
    }

    public static RealmResults<Image> findImages(Realm realm, String type) {
        realm = initRealm(realm);
        if (Constants.FAVORITE.equals(type)) {
            return findFavoriteImages(realm);
        }
        return realm.where(Image.class)
                .equalTo(Constants.TYPE, type)
                .findAllSorted("publishedAt", type.equals(API.TYPE_GANK) ? Sort.DESCENDING : Sort.ASCENDING);
    }

    public static RealmResults<Image> findFavoriteImages(Realm realm) {
        realm = initRealm(realm);
        return realm.where(Image.class)
                .equalTo("isLiked", true)
                .findAll();
    }

    public static void clearAllImages() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(Image.class);
        realm.commitTransaction();
    }

    public static String getRandomImage(String type) {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Image> images;
        if (type.equals("anime")) {
            images = realm.where(Image.class).equalTo("big", true).findAll();
        } else {
            images = realm.where(Image.class).equalTo(Constants.TYPE, type).findAll();
        }
        if (images.size() == 0) return null;
        return images.get(new Random().nextInt(images.size())).url;
    }

    public static Image findImageByUrl(String url) {
        Realm realm = Realm.getDefaultInstance();

        return realm.where(Image.class).equalTo(Constants.URL, url).findFirst();
    }
}
