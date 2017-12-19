package com.pilicreateworld;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

public class Settings {
    private static Settings sInstance = null;

    public static Settings getInstance() {
        if (sInstance == null) {
            sInstance = new Settings();
        }

        return sInstance;
    }

    private boolean mEnableProxy = false;
    private String mProxyIp;
    private int mProxyPort;

    private boolean mEnableDebug = false;

    private Settings() {
        //nothing
    }

    public Proxy getProxy() {
        Proxy proxy;

        if (mEnableProxy) {
            SocketAddress address = new InetSocketAddress(mProxyIp, mProxyPort);
            proxy = new Proxy(Proxy.Type.HTTP, address);
        }
        else {
            proxy = Proxy.NO_PROXY;
        }

        return proxy;
    }

    public void setProxy(String ip, int port) {
        mEnableProxy = true;

        mProxyIp = ip;
        mProxyPort = port;
    }

    public void enableDebug() {
        mEnableDebug = true;
    }

    public boolean isDebuggable() {
        return mEnableDebug;
    }
}
