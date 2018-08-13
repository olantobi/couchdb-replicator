/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.liferon.couchdbreplicator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author olanrewaju.ebenezer
 */
public class RecordCreator implements Runnable {

    private final String recordUrl;
    private final String postRecordUrl;
    private final String username;
    private final String password;
    //private String payload;
    private final Gson gson;

    public RecordCreator(String recordUrl, String postRecordUrl, String username, String password) {
        this.recordUrl = recordUrl;
        this.postRecordUrl = postRecordUrl;
        this.username = username;
        this.password = password;
        gson = new Gson();
        //this.payload = payload;
    }

    @Override
    public void run() {
        String payload = getSingleRecord(recordUrl);

        JsonObject jsonObject = gson.fromJson(payload, JsonObject.class);

        jsonObject.remove("_rev");
        jsonObject.remove("_id");
        postSingleRecord(postRecordUrl, jsonObject.toString());
    }

    private String getSingleRecord(String recordUrl) {
        String response = "";
        try {
            HttpUtility http = new HttpUtility();
            response = http.sendGet(recordUrl);

            //System.out.println("Search URL: " + recordUrl);
            //System.out.println("Response: " + response);
        } catch (Exception ex) {
            Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return response;
    }

    private void postSingleRecord(String recordUrl, String payload) {
        try {
            HttpUtility http = new HttpUtility();
            System.out.println("Search URL: " + recordUrl);
            System.out.println("Payload: " + payload);
            String response = http.sendPut(recordUrl, payload, username, password);
            System.out.println("Response: " + response);

        } catch (Exception ex) {
            Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
