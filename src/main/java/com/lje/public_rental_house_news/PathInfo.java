package com.lje.public_rental_house_news;

import com.sun.istack.internal.Nullable;

public class PathInfo {
    public PathInfo() {

    }

    @SuppressWarnings("WeakerAccess")
    public String name;
    @SuppressWarnings("WeakerAccess")
    public String url;
    @SuppressWarnings("WeakerAccess")
    public String regex;
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public String creator;

    @Override
    public String toString() {
        return "PathInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", regex='" + regex + '\'' +
                ", creator='" + creator + '\'' +
                '}';
    }
}
