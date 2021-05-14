package com.mirea.bsbo0419.latexdroid.apis;

import com.wolfram.alpha.*;
import java.util.ArrayList;

public class WolframAPI {
    public static String input;
    public static boolean typeOfTask;
    public static WAQueryResult queryResult;

    public static ArrayList<String> SendQuery(String equationText){

        input = equationText;

        ArrayList<String> res = new ArrayList<String>();
        typeOfTask = input.contains("=");

        WAEngine engine = new WAEngine();
        engine.setAppID("K6T6J7-EPP65Y7RHG");

        WAQuery query = engine.createQuery();
        query.setInput(input);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    queryResult = engine.performQuery(query);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    public static ArrayList<String> FindResult(){
        ArrayList<String> res = new ArrayList<String>();

        for (WAPod pod : queryResult.getPods()) {
            if (!pod.isError()) {
                if(pod.getTitle().toLowerCase().contains("approximation")){
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
}
