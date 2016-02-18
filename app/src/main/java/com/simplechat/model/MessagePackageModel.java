package com.simplechat.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by xack1 on 17.02.2016.
 */
public class MessagePackageModel {

    @SerializedName("message")
    public String message;

    @SerializedName("name")
    public String name;

    @SerializedName("from")
    public String from;

    @SerializedName("to")
    public String to;

    @SerializedName("online")
    public boolean online = true;

    public MessagePackageModel() {}

    public MessagePackageModel(String name, String message, String from, String to) {
        this.name = name;
        this.message = message;
        this.from = from;
        this.to = to;
    }

    public MessagePackageModel(String name, String message, String from) {
        this.name = name;
        this.message = message;
        this.from = from;
    }

    public MessagePackageModel(String from, boolean online) {
        this.from = from;
        this.online = online;
    }

    public MessagePackageModel(String from, String name, boolean online) {
        this.from = from;
        this.name = name;
        this.online = online;
    }

    public MessagePackageModel(String from, String name) {
        this.from = from;
        this.name = name;
    }

}
