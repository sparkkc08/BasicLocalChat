package com.simplechat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simplechat.model.MessagePackageModel;

public class GsonHelper {

    private static final Gson gson = getGson();

    public static Gson getGson() {
        final GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();


        Gson ret = builder.create();

        if (ret == null) {
            ret = new GsonBuilder().create();

            if (ret == null)
                ret = new Gson();
        }

        return ret;
    }

    public static <T> T fromJson(String receive, Class<T> responseType) {
        try {
            return gson.fromJson(receive, responseType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJson(MessagePackageModel model) {
        try {
            return gson.toJson(model);
        } catch (Exception e) {
            return null;
        }
    }
}
