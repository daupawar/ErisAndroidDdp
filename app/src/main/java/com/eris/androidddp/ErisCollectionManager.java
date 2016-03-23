package com.eris.androidddp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.SubscribeListener;
import im.delight.android.ddp.UnsubscribeListener;

/**
 * Created by Rohan on 09/02/16.
 */
public class ErisCollectionManager implements MeteorCallback {


    private boolean printLog;
    private boolean printMeteorLog;

    private HashMap<String, ErisCollectionHandler> collectionHandlerList;
    private HashMap<String, String> subscriptionList;


    Meteor mMeteor;
    Context mContext;
    private boolean hasNetworkConnection;
    private ErisConnectionListener connectionListener;

    protected ErisCollectionManager() {
        collectionHandlerList = new HashMap<>();
        subscriptionList = new HashMap<>();
        setPrintLog(true);
        setPrintMeteorLog(true);
    }

    private static ErisCollectionManager instance = null;

    /**
     * @return singleton object
     */
    public static synchronized ErisCollectionManager getInstance() {
        if (instance == null) {
            instance = new ErisCollectionManager();
        }
        return instance;
    }

    /**
     * @param context
     * @param conUrl
     */
    public void connect(Context context, String conUrl, ErisConnectionListener listener) {
        mContext = context;
        try {
            if (MeteorSingleton.hasInstance()) {
                if (mMeteor != null) {
                    mMeteor = MeteorSingleton.getInstance();
                }
            } else {
                MeteorSingleton.setLoggingEnabled(printMeteorLog);
                mMeteor = MeteorSingleton.createInstance(mContext, ErisCollectionManager.getConnectionUrl(conUrl));
                mMeteor.setCallback(this);
                startConnectivityMonitoring(mContext);
                seConnectionListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param printLog
     */
    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    /**
     * call this method before connect
     *
     * @param printMeteorLog
     */
    public void setPrintMeteorLog(boolean printMeteorLog) {
        this.printMeteorLog = printMeteorLog;
    }

    /**
     * @param url
     * @return
     */
    public static String getConnectionUrl(String url) {
        return "ws://" + url + "/websocket";
    }

    @Override
    public void onConnect(boolean b) {
        Loggi("Meteor Connected");
        if (connectionListener != null) {
            connectionListener.onConnect(b);
        }
        if (collectionHandlerList.size() > 0) {
            Set<String> keys = collectionHandlerList.keySet();
            HashSet<String> subSet = new HashSet<>();
            subSet.addAll(keys);
            for (String key : subSet) {
                subscribeCollection(key);
            }
        }

    }

    @Override
    public void onDisconnect() {
        Loggi("Meteor Disconnect");
        if (connectionListener != null) {
            connectionListener.onDisconnect();
        }
        try {
            if (subscriptionList != null) {
                if (subscriptionList.size() > 0) {
                    Set<String> keys = subscriptionList.keySet();
                    HashSet<String> subSet = new HashSet<>();
                    subSet.addAll(keys);
                    for (String key : subSet) {
                        unscubscribeCollection(key, null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(Exception e) {
        if (connectionListener != null) {
            connectionListener.onException(e);
        }
        Loggi("Meteor Exception" + e.getMessage());
    }

    public boolean isMeteorConnected() {
        return mMeteor.isConnected();
    }

    public void reConnectToMeteor(ErisConnectionListener listener) {
        mMeteor.reconnect();
        if (listener != null) {
            connectionListener = listener;
        }
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
            collection.onDataChanged(collectionName, documentID, updatedValuesJson, removedValuesJson);
        }
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        Loggi("Meteor onDataRemoved " + collectionName + " " + documentID);
        ErisCollectionHandler collection = null;
        if (collectionHandlerList.containsKey(collectionName)) {
            collection = collectionHandlerList.get(collectionName);
            collection.onDataRemoved(collectionName, documentID);
        }
    }

    /**
     * subscribe to data from the server
     *
     * @param collectionName String
     */
    public void subscribeCollection(String collectionName) {
        subscribeCollection(collectionName, null);
    }

    /**
     * @param collectionName
     * @param parameters
     */
    public void subscribeCollection(String collectionName, Object[] parameters) {
        subscribeCollection(collectionName, parameters, null);
    }

    /**
     * @param collectionName
     * @param parameters
     * @param listener
     */
    public void subscribeCollection(String collectionName, Object[] parameters, SubscribeListener listener) {

        if (!hasSubscription(collectionName)) {
            String subscriptionId = mMeteor.subscribe(collectionName, parameters, listener);
            subscriptionList.put(collectionName, subscriptionId);
        }
    }

    /**
     * unsubscribe to data from the server
     *
     * @param collectionName
     */
    public void unscubscribeCollection(String collectionName, UnsubscribeListener listener) {
        if (hasSubscription(collectionName)) {
            if (listener == null) {
                mMeteor.unsubscribe(collectionName);
            } else {
                mMeteor.unsubscribe(collectionName, listener);
            }
            subscriptionList.remove(collectionName);
        }
    }

    /**
     * @param collectionName
     * @return
     */
    public boolean hasSubscription(String collectionName) {
        return subscriptionList.containsKey(collectionName);
    }

    /**
     * @param message
     */
    protected void Loggi(String message) {
        if (printLog) {
            Logg.i(getClass().getSimpleName(), message);
        }
    }

    /**
     * @param collectionName
     * @return
     */
    public ErisCollectionHandler getCollectionHandler(String collectionName) {

        if (!collectionHandlerList.containsKey(collectionName)) {
            ErisCollectionHandler collection = new ErisCollectionHandler();
            collectionHandlerList.put(collectionName, collection);
        }
        return collectionHandlerList.get(collectionName);
    }

    /**
     * @param collectionName
     * @param values
     * @param listener
     */
    public void insert(String collectionName, HashMap<String, Object> values, ResultListener listener) {

        if (listener != null) {
            if (this.hasNetworkConnection) {
                if (mMeteor.isConnected()) {
                    mMeteor.insert(collectionName, values, listener);
                } else {
                    listener.onError("Error", "Please try again", "");
                    mMeteor.reconnect();
                }
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        } else {
            mMeteor.insert(collectionName, values);
        }

    }


    /**
     * @param collectionName
     * @param documentId
     * @param listener
     */
    public void remove(String collectionName, String documentId, ResultListener listener) {
        HashMap query = new HashMap();
        query.put("_id", documentId);
        if (listener == null) {
            mMeteor.insert(collectionName, query);
        } else {
            if (this.hasNetworkConnection) {
                if (mMeteor.isConnected()) {
                    mMeteor.insert(collectionName, query, listener);
                } else {
                    listener.onError("Error", "Please try again", "");
                    mMeteor.reconnect();
                }
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        }
    }

    /**
     * @param collectionName
     * @param query
     * @param data
     */
    public void update(String collectionName, Map<String, Object> query, Map<String, Object> data) {
        if (mMeteor.isConnected()) {
            MeteorSingleton.getInstance().update(collectionName, query, data);
        } else {
            mMeteor.reconnect();
        }
    }

    /**
     * @param collectionName
     * @param query
     * @param data
     * @param options
     */
    public void update(String collectionName, Map<String, Object> query, Map<String, Object> data, Map<String, Object> options) {
        if (mMeteor.isConnected()) {
            MeteorSingleton.getInstance().update(collectionName, query, data, options);
        } else {
            mMeteor.reconnect();
        }
    }

    /**
     * @param collectionName
     * @param query
     * @param data
     * @param options
     * @param listener
     */
    public void update(String collectionName, Map<String, Object> query, Map<String, Object> data, Map<String, Object> options, ResultListener listener) {
        if (listener != null) {
            if (this.hasNetworkConnection) {
                if (mMeteor.isConnected()) {
                    MeteorSingleton.getInstance().update(collectionName, query, data, options, listener);
                } else {
                    listener.onError("Error", "Please try again", "");
                    mMeteor.reconnect();
                }
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        }
    }

    /**
     * @param username
     * @param password
     * @param listener
     */
    public void loginWithUsername(String username, String password, ResultListener listener) {
        if (listener != null) {
            if (this.hasNetworkConnection) {
                MeteorSingleton.getInstance().loginWithUsername(username, password, listener);
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        }
    }

    /**
     * @param email
     * @param password
     * @param listener
     */
    public void loginWithEmail(String email, String password, ResultListener listener) {
        if (listener != null) {
            if (this.hasNetworkConnection) {
                MeteorSingleton.getInstance().loginWithEmail(email, password, listener);
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        }
    }

    /**
     * @param email
     * @param password
     * @param listener
     */
    public void registerUser(String username, String email, String password, HashMap<String, Object> params, ResultListener listener) {
        if (listener != null) {
            if (this.hasNetworkConnection) {
                if (params == null) {
                    MeteorSingleton.getInstance().registerAndLogin(username, email, password, listener);
                } else {
                    MeteorSingleton.getInstance().registerAndLogin(username, email, password, params, listener);
                }
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        }
    }

    /**
     * resume session
     *
     * @param token
     * @param listener
     */
    public void loginWithToken(String token, ResultListener listener) {
        if (token != null) {
            if (!token.isEmpty()) {
                final Map<String, Object> authData = new HashMap<>();
                authData.put("resume", token);

                callMethod("login", new Object[]{authData}, listener);
            }
        }
    }

    /**
     * call remote method aysnchronasaly
     *
     * @param methodName
     * @param objectArray
     * @param listener
     */
    public void callMethod(String methodName, Object[] objectArray, ResultListener listener) {
        if (listener != null) {
            if (this.hasNetworkConnection) {
                if (mMeteor.isConnected()) {
                    new BackgroundRPC(methodName, objectArray, listener).execute();
                } else {
                    listener.onError("Error", "Please try again", "");
                    mMeteor.reconnect();
                }
            } else {
                listener.onError("Error", "Please Check your network Connection", "");
            }
        }
    }


    /**
     * AsyncTask
     */
    private class BackgroundRPC extends AsyncTask<Void, Void, Void> {

        private String methodName;
        private Object[] objectArray;
        private ResultListener listener;

        public BackgroundRPC(String methodName, Object[] objectArray, ResultListener listener) {
            this.methodName = methodName;
            this.objectArray = objectArray;
            this.listener = listener;

        }

        @Override
        protected Void doInBackground(Void... params) {
            MeteorSingleton.getInstance().call(methodName, objectArray, listener);
            return null;
        }
    }

    /**
     * @param listener
     */
    public void seConnectionListener(ErisConnectionListener listener) {
        if (listener != null) {
            this.connectionListener = listener;
        }
    }

    public void unsetListener(){
        this.connectionListener = null;
    }

    /**
     * Starts network connectivity monitoring.
     *
     * @param context {@link Context} to access services and register handlers.
     */
    public void startConnectivityMonitoring(Context context) {
        // Start monitoring broadcast notifications for connectivity
        context.getApplicationContext().registerReceiver(new ConnectivityChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        handleConnectivityNotification(context);
    }

    /**
     *
     */
    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleConnectivityNotification(context);
        }
    }

    /**
     * @param context
     */
    private void handleConnectivityNotification(Context context) {
        // Update flag based on current connectivity state
        try {
            ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = mgr.getActiveNetworkInfo();
            if ((netInfo != null) && (netInfo.isConnected())) {
                callConnectionListener(true);
            } else {
                callConnectionListener(false);
            }
        } catch (Exception e) {
            callConnectionListener(false);
        }
    }

    /**
     * @param status
     */
    private void callConnectionListener(boolean status) {
        this.hasNetworkConnection = status;
        if (connectionListener != null) {
            connectionListener.onInternetStatusChanged(status);
        }
    }
}
