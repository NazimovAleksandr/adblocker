package com.example.adblock;

import java.util.List;

public class AdvtBlocker {
    private final long advtBlockerPtr;

    private AdvtBlocker(long extObjPointer) {
        this.advtBlockerPtr = extObjPointer;
    }

    public static AdvtBlocker createInstance(List<String> rules) {
        long ptr = initObject(rules);
        return new AdvtBlocker(ptr);
    }

    public boolean checkUrls(String url, String sourceUrl, String requestType) {
        return this.checkNetworkUrls(this.advtBlockerPtr, url, sourceUrl, requestType);
    }

    public static native long initObject(List<String> var0);

    public native boolean checkNetworkUrls(long var1, String var3, String var4, String var5);

    static {
        System.loadLibrary("adblock_coffee");
    }
}
