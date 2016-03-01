package com.eris.androidddp;

/**
 * Created by Rohan on 11/02/16.
 */
public interface ErisCollectionListener {
    void onDataAddedEris(ErisCollectionRecord collectionData);

    void onDataChangedEris(ErisCollectionRecord collectionData);

    void onDataRemovedEris(String collectionName, String documentID);
}
