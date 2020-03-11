package com.xurent.livedance.model;

public class Music {

    private int id;
    private String name;
    private String url;
    private String imageUrl;
    private String nativeUrl;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNativeUrl() {
        return nativeUrl;
    }

    public void setNativeUrl(String nativeUrl) {
        this.nativeUrl = nativeUrl;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", nativeUrl='" + nativeUrl + '\'' +
                '}';
    }
}
