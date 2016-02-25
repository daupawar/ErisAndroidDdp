package com.eris.androidddp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
public class ErisCollectionHandler {


    private ErisCollectionListener eventListener;
    private HashMap<String, ErisCollectionRecord> collectionMap;
    private List<ErisCollectionRecord> collectionList;

    public ErisCollectionHandler() {
        collectionMap = new HashMap<>();
        collectionList = new ArrayList<>();
    }

    public void setEventListener(ErisCollectionListener eventListener) {
        this.eventListener = eventListener;
    }

    public void removeFromListMap(String  documentID){
        collectionMap.remove(documentID);
        for(ErisCollectionRecord collection:collectionList){
            if (collection.getDocId().equalsIgnoreCase(documentID)) {
                int index=collectionList.indexOf(collection);
                collectionList.remove(index);
                break;
            }
        }
    }

    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {

        ErisCollectionRecord colObj = new ErisCollectionRecord();
        colObj.setJsonData(newValuesJson);
        colObj.setDocId(documentID);
        colObj.setCollectionName(collectionName);

        collectionList.add(colObj);
        collectionMap.put(documentID, colObj);

        if (this.eventListener != null) {
            this.eventListener.onDataAddedEris(colObj);
        }
    }

    public void onDataRemoved(String collectionName, String documentID) {
        removeFromListMap(documentID);

        if (this.eventListener != null) {
            this.eventListener.onDataRemovedEris(collectionName,documentID);
        }
    }

    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {

        try {
            if (collectionMap.containsKey(documentID)) {
                ErisCollectionRecord collection = collectionMap.get(documentID);
                JSONObject objectOldData = new JSONObject(collection.getJsonData());
                JSONObject newData = new JSONObject(updatedValuesJson);
                Iterator<String> keyset = newData.keys();
                while (keyset.hasNext()) {
                    String key = keyset.next();
                    Object obj = newData.get(key);
                    if (obj instanceof JSONArray) {
                        JSONArray urlArray = (JSONArray) obj;
                        objectOldData.put(key, urlArray);
                    } else if(obj instanceof JSONObject){
                        JSONObject urlObject = (JSONObject) obj;
                        objectOldData.put(key, urlObject);
                    }else {
                        objectOldData.put(key,obj);
                    }
                }
                collection.setJsonData(objectOldData.toString());
                if (this.eventListener != null) {
                    this.eventListener.onDataChangedEris(collection, removedValuesJson);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ErisCollectionRecord getCollectionById(String id) {
        return collectionMap.get(id);
    }

    public HashMap<String, ErisCollectionRecord> getCollectionMap() {
        return collectionMap;
    }

    public void setCollectionMap(HashMap<String, ErisCollectionRecord> collectionMap) {
        this.collectionMap = collectionMap;
    }

    public List<ErisCollectionRecord> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<ErisCollectionRecord> collectionList) {
        this.collectionList = collectionList;
    }
}
