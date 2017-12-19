package com.pilicreateworld;

import com.pilicreateworld.typeset.Typesetter;

import java.io.File;
import java.io.IOException;

public class Argument {
    private String mOutputDir;
    private Typesetter.Parameter mTypesetParameter;

    public static Argument parse(String[] args) {
        Argument argument = new Argument();

        for (String arg : args) {
            if (arg.startsWith("--output=")) {
                argument.setOutputDir(arg.substring(9));
            }
            else if (arg.startsWith("--device=")) {
                argument.setTargetDevice(arg.substring(9));
            }
            else if (arg.startsWith("--proxy=")) {
                argument.setProxy(arg.substring(8));
            }
            else if (arg.startsWith("--enable_debug")) {
                argument.enableDebug();
            }
            else {
                //TODO: add more here
            }
        }

        return argument;
    }

    private Argument() {
        //nothing
    }

    private void setOutputDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            mOutputDir = dir.getCanonicalPath();
        }
        catch (IOException e) {
            //ignore
        }
    }

    private void setTargetDevice(String device) {
        mTypesetParameter = new Typesetter.Parameter();

        if (device.equals("nexus5")) {
            /**
             * 9:16
             */
            mTypesetParameter.setPageWidth(360);
            mTypesetParameter.setPageHeight(640);

            mTypesetParameter.setTopMargin(15);
            mTypesetParameter.setBottomMargin(15);
            mTypesetParameter.setLeftMargin(30);
            mTypesetParameter.setRightMargin(30);

            mTypesetParameter.setWordWidth(18);
            mTypesetParameter.setWordHeight(18);

            mTypesetParameter.setLineSpacing(5);
            mTypesetParameter.setWordSpacing(3);
        }
        else {
            throw new IllegalArgumentException("device is not supported");
        }
    }

    private void setProxy(String proxy) {
        int colonPos = proxy.indexOf(":");
        if (colonPos < 0) {
            throw new IllegalArgumentException("proxy must be ip:port");
        }

        String ip = proxy.substring(0, colonPos);
        String port = proxy.substring(colonPos + 1);

        Settings.getInstance().setProxy(ip, Integer.parseInt(port));
    }

    private void enableDebug() {
        Settings.getInstance().enableDebug();
    }

    public boolean isInvalid() {
        if (mOutputDir == null || mOutputDir.isEmpty()) {
            return true;
        }

        if (mTypesetParameter == null) {
            return true;
        }

        return false;
    }

    public String getOutputDir() {
        return mOutputDir;
    }

    public Typesetter.Parameter getTypesetParameter() {
        return mTypesetParameter;
    }
}
