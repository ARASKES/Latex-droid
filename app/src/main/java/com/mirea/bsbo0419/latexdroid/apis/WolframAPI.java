package com.mirea.bsbo0419.latexdroid.apis;

import android.content.Context;

import com.mirea.bsbo0419.latexdroid.R;
import com.wolfram.alpha.*;
import java.util.ArrayList;

public class WolframAPI {
    private static String appId = null;

    public static String input;
    public static boolean typeOfTask;
    public static WAQueryResult queryResult;

    public static ArrayList<String> SendQuery(String equationText, Context context) {

        input = equationText;

        ArrayList<String> res = new ArrayList<String>();
        typeOfTask = input.contains("=");

        if (appId == null) {
            appId = context.getString(R.string.wolfram_app_id);
        }
        WAEngine engine = new WAEngine();
        engine.setAppID(appId);

        WAQuery query = engine.createQuery();
        query.setInput(input);

        Thread thread = new Thread(() -> {
            try  {
                queryResult = engine.performQuery(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        if (queryResult == null) {
            return null;
        }

        if (queryResult.isError()) {
            return null;
        } else if (!queryResult.isSuccess()) {
            return null;
        } else {
            if(typeOfTask) {
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                        if(pod.getTitle().toLowerCase().contains("solution")) {
                            for (WASubpod subPod : pod.getSubpods()) {
                                for (Object element : subPod.getContents()) {
                                    if (element instanceof WAPlainText) {
                                        res.add(((WAPlainText) element).getText());
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                res = FindResult();
            }
        }

        return res;
    }

    private static ArrayList<String> FindResult() {
        ArrayList<String> res = new ArrayList<String>();

        for (WAPod pod : queryResult.getPods()) {
            if (!pod.isError()) {
                if(pod.getTitle().toLowerCase().contains("approximation")) {
                    for (WASubpod subPod : pod.getSubpods()) {
                        for (Object element : subPod.getContents()) {
                            if (element instanceof WAPlainText) {
                                res.add(((WAPlainText) element).getText());
                            }
                        }
                    }
                }
            }
        }

        if(res.isEmpty()) {
            for (WAPod pod : queryResult.getPods()) {
                if (!pod.isError()) {
                    if(pod.getTitle().toLowerCase().contains("result")) {
                        for (WASubpod subPod : pod.getSubpods()) {
                            for (Object element : subPod.getContents()) {
                                if (element instanceof WAPlainText) {
                                    res.add(((WAPlainText) element).getText());
                                }
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    public static String GetAppID() {
        return appId;
    }

    public static void SetAppID(String appId) {
        WolframAPI.appId = appId;
    }
}
