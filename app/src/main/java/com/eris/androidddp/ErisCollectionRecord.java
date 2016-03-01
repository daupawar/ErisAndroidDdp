package com.eris.androidddp;

/**
 * Created by Rohan on 19/02/16.
 */
public class ErisCollectionRecord {


    public ErisCollectionRecord() {

    }

    private String jsonData;
    private String docId;
    private String collectionName;


    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }


}
