package com.sky.context;

/**
 * @author siming323
 * @date 2024/2/7 16:49
 */
public class ImageContext {
    public static ThreadLocal<String> threadLocalPath = new ThreadLocal<>();

    public static void setCurrentWillDeleteImage(String path) {
        threadLocalPath.set(path);
    }

    public static String getCurrentWillDeleteImage() {
        return threadLocalPath.get();
    }

    public static void removeCurrentWillDeleteImage() {
        threadLocalPath.remove();
    }
}
