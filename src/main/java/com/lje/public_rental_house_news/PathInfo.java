package com.lje.public_rental_house_news;

public class PathInfo {
    public PathInfo() {

    }

    @SuppressWarnings("WeakerAccess")
    public String name;
    @SuppressWarnings("WeakerAccess")
    public String url;
    @SuppressWarnings("WeakerAccess")
    public String regex;

    @Override
    public String toString() {
        return "PathInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", regex='" + regex + '\'' +
                '}';
    }
}