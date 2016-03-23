package com.eris.androidddp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Rohan on 09/02/16.
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

    public void removeFromListMap(String documentID) {
        collectionMap.remove(documentID);
        for (ErisCollectionRecord collection : collectionList) {
            if (collection.getDocId().equalsIgnoreCase(documentID)) {
                int index = collectionList.indexOf(collection);
                collectionList.remove(index);
                break;
            }
        }
    }

    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {

        if (!collectionMap.containsKey(documentID)) {
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
    }

    public void onDataRemoved(String collectionName, String documentID) {
        if (collectionMap.containsKey(documentID)) {
            removeFromListMap(documentID);

            if (this.eventListener != null) {
                this.eventListener.onDataRemovedEris(collectionName, documentID);
            }
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
                    } else if (obj instanceof JSONObject) {
                        JSONObject urlObject = (JSONObject) obj;
                        objectOldData.put(key, urlObject);
                    } else {
                        objectOldData.put(key, obj);
                    }
                }
                collection.setJsonData(objectOldData.toString());
                if (this.eventListener != null) {
                    this.eventListener.onDataChangedEris(collection);
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
