package com.mirea.bsbo0419.latexdroid;
import com.wolfram.alpha.*;
import java.util.ArrayList;
import java.util.Scanner;

import okhttp3.Response;

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
        }

        if (queryResult.isError()) {
            System.out.println("Query error");

        } else if (!queryResult.isSuccess()) {
            System.out.println("Query was not understood; no results available.");

        } else {

            if(typeOfTask){
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                        if(pod.getTitle().toLowerCase().contains("solution")){
                            for (WASubpod subpod : pod.getSubpods()) {
                                for (Object element : subpod.getContents()) {
                                    if (element instanceof WAPlainText) {
                                        res.add(((WAPlainText) element).getText());

                                    }
                                }
                            }
                        }
                    }
                }
            }
            else{
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
                    for (WASubpod subpod : pod.getSubpods()) {
                        for (Object element : subpod.getContents()) {
                            if (element instanceof WAPlainText) {
                                res.add(((WAPlainText) element).getText());

                            }
                        }
                    }
                }
            }
        }

        if(res.isEmpty()){
            for (WAPod pod : queryResult.getPods()) {
                if (!pod.isError()) {
                    if(pod.getTitle().toLowerCase().contains("result")){
                        for (WASubpod subpod : pod.getSubpods()) {
                            for (Object element : subpod.getContents()) {
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

