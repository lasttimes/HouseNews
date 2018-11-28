package com.lje.public_rental_house_news;

import java.util.List;

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
    public String creator;
    @SuppressWarnings("WeakerAccess")
    public String charset;

    @Override
    public String toString() {
        return "PathInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", regex='" + regex + '\'' +
                ", creator='" + creator + '\'' +
                ", charset='" + charset + '\'' +
                '}';
    }

    public static PathInfo findInList(List<PathInfo> list, String pathName) {
        PathInfo pathInfo = null;
        for (PathInfo info : list) {
            if (info.name.equals(pathName)) {
                pathInfo = info;
                break;
            }
        }
        return pathInfo;
    }
}
