package com.eris.androidddp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.SubscribeListener;

/*
 * Copyright (c) Rohan Pawar <daupawar@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ErisCollectionManager implements MeteorCallback {


    private  boolean printLog;
    private  boolean printMeteorLog;

    private HashMap<String, ErisCollectionHandler> collectionHandlerList;
    private HashMap<String,String> subscriptionList;


    Meteor mMeteor;
    Context mContext;

    protected ErisCollectionManager() {
        collectionHandlerList = new HashMap<>();
        subscriptionList = new HashMap<>();
        setPrintLog(true);
        setPrintMeteorLog(true);
    }

    private static ErisCollectionManager instance = null;

    public static synchronized ErisCollectionManager getInstance() {
        if (instance == null) {
            instance = new ErisCollectionManager();
        }
        return instance;
    }

    public void connect(Context context,String conUrl) {
        mContext = context;
        try {
            if(MeteorSingleton.hasInstance()){
                if(mMeteor!=null) {
                    mMeteor = MeteorSingleton.getInstance();
                }
            }else{
                MeteorSingleton.setLoggingEnabled(printMeteorLog);
                mMeteor = MeteorSingleton.createInstance(mContext, ErisCollectionManager.getConnectionUrl(conUrl));
                mMeteor.setCallback(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param printLog
     */
    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    /**
     *
     * @param printMeteorLog
     */
    public void setPrintMeteorLog(boolean printMeteorLog) {
        this.printMeteorLog = printMeteorLog;
    }

    public static String getConnectionUrl(String url) {
        return "ws://" + url + "/websocket";
    }

    @Override
    public void onConnect(boolean b) {
        Loggi("Meteor Connected");
    }

    @Override
    public void onDisconnect() {
        Loggi("Meteor Disconnect");
    }

    @Override
    public void onException(Exception e) {
        Loggi("Meteor Exception" + e.getMessage());
    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {
        Loggi("Meteor onDataAdded " + collectionName + " " + documentID + " " + newValuesJson);

        ErisCollectionHandler collection = null;
        if (collectionHandlerList.containsKey(collectionName)) {
            collection = collectionHandlerList.get(collectionName);
            collection.onDataAdded(collectionName, documentID, newValuesJson);
        } else {
            collection = new ErisCollectionHandler();
            collectionHandlerList.put(collectionName, collection);
            collection.onDataAdded(collectionName, documentID, newValuesJson);
        }
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Loggi("Meteor onDataChanged " + collectionName + " " + documentID + " " + updatedValuesJson + " " + removedValuesJson);
        ErisCollectionHandler collection = null;
        if (collectionHandlerList.containsKey(collectionName)) {
            collection = collectionHandlerList.get(collectionName);
            collection.onDataChanged(collectionName,documentID,updatedValuesJson,removedValuesJson);
        }
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        Loggi("Meteor onDataRemoved " + collectionName + " " + documentID);
        ErisCollectionHandler collection = null;
        if (collectionHandlerList.containsKey(collectionName)) {
            collection = collectionHandlerList.get(collectionName);
            collection.onDataRemoved(collectionName,documentID);
        }
    }

    // subscribe to data from the server
    public void subscribeCollection(String collectionName){
        subscribeCollection(collectionName, null, null);
    }

    public void subscribeCollection(String collectionName,Object[] parameters){
        subscribeCollection(collectionName,parameters,null);
    }
    public void subscribeCollection(String collectionName,Object[] parameters,SubscribeListener listener){

        if(!subscriptionList.containsKey(collectionName)) {
            String subscriptionId = mMeteor.subscribe(collectionName,parameters,listener);
            subscriptionList.put(subscriptionId, subscriptionId);
        }
    }

    // unsubscribe to data from the server
    public void unscubscribeCollection(String collectionName){
        if(subscriptionList.containsKey(collectionName)) {
            mMeteor.unsubscribe(collectionName);
            subscriptionList.remove(collectionName);
        }
    }

    protected void Loggi(String message) {
        if (printLog) {
            Log.i(getClass().getSimpleName(), message);
        }
    }

    public ErisCollectionHandler getCollection(String collectionName) {

        if (!collectionHandlerList.containsKey(collectionName)) {
            ErisCollectionHandler collection = new ErisCollectionHandler();
            collectionHandlerList.put(collectionName, collection);
        }
        return collectionHandlerList.get(collectionName);
    }

    public void insert(String collectionName,HashMap<String,Object> values,ResultListener listener){
        if(listener!=null) {
            mMeteor.insert(collectionName, values);
        }else{
            mMeteor.insert(collectionName, values, listener);
        }
    }

    public void remove(String collectionName, String documentId, ResultListener listener) {
        HashMap query = new HashMap();
        query.put("_id", documentId);
        if(listener!=null) {
            mMeteor.insert(collectionName, query);
        }else{
            mMeteor.insert(collectionName, query, listener);
        }
    }

    public void callMethod(String methodName,Object[] objectArray,ResultListener listener){
        new BackgroundRPC(methodName,objectArray,listener).execute();
    }

    private class BackgroundRPC extends AsyncTask<Void,Void,Void>{

        private String methodName;
        private Object[] objectArray;
        private ResultListener listener;

        public BackgroundRPC(String methodName,Object[] objectArray,ResultListener listener){
            this.methodName=methodName;
            this.objectArray=objectArray;
            this.listener=listener;

        }
        @Override
        protected Void doInBackground(Void... params) {
            MeteorSingleton.getInstance().call(methodName, objectArray, listener);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
