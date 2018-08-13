/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.liferon.couchdbreplicator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author olanrewaju.ebenezer
 */
public class Replicator {

    private final String SOURCE_URL;
    private final String DEST_URL;
    private final String SOURCE_DATABASE;
    private final String DEST_DATABASE;
    private final String ALL_DOCS = "_all_docs";
    private final int FETCH_SIZE;
    private int START_FROM = 0;
    private final String username;
    private final String password;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private int totalSize = 1000;       // Just an initial value

    public Replicator(String sourceUrl, String destUrl, String database, String destDatabase, String fetchSize, String username, String password) {
        this.SOURCE_URL = sourceUrl;
        this.DEST_URL = destUrl;
        this.SOURCE_DATABASE = database;
        this.DEST_DATABASE = destDatabase;
        this.FETCH_SIZE = Integer.parseInt(fetchSize);
        this.username = username;
        this.password = password;
    }

    public static void main(String... args) {
        System.out.println("Parameters: " + Arrays.toString(args));
        Replicator repl = new Replicator(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        repl.getAllRecords();
        //repl.getSingleRecord("2018-01-09T12:26:47.392Z");
        //repl.getSingleRecord("_design/collector");
    }

    private void getAllRecords() {

        for (;START_FROM < totalSize; START_FROM+=FETCH_SIZE) {
            HttpUtility http = new HttpUtility();
            
            String couchUrl = SOURCE_URL + "/" + SOURCE_DATABASE + "/" + ALL_DOCS + "?limit=" + FETCH_SIZE + "&skip=" + START_FROM;

            try {
                String response = http.sendGet(couchUrl);                
                Gson gson = new Gson();

                JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
                totalSize = jsonObject.get("total_rows").getAsInt();
                System.out.println("Total: " + totalSize);

                JsonArray jsonArray = jsonObject.getAsJsonArray("rows");
                RecordCreator record = null;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject jsonElement = jsonArray.get(i).getAsJsonObject();
                    String id = jsonElement.get("id").getAsString();
                    //System.out.println(id);
                    //getSingleRecord(id);
                    String recordUrl = SOURCE_URL + "/" + SOURCE_DATABASE + "/" + id;
                    String postRecordUrl = DEST_URL + "/" + DEST_DATABASE + "/" + id;
                    record = new RecordCreator(recordUrl, postRecordUrl, username, password);

                    executor.execute(record);
                }

            } catch (Exception ex) {
                Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();

    }

    private void getSingleRecord(String id) {
        try {
            HttpUtility http = new HttpUtility();
            String couchUrl = SOURCE_URL + "/" + SOURCE_DATABASE + "/" + id;
            String response = http.sendGet(couchUrl);

            Gson gson = new Gson();

            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

            jsonObject.remove("_rev");
            jsonObject.remove("_id");

            System.out.println("Search URL: " + couchUrl);
            System.out.println("Response: " + jsonObject);
        } catch (Exception ex) {
            Logger.getLogger(Replicator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
