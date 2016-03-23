package com.eris.androidddp;

/**
 * Created by Rohan on 04/03/16.
 */
public interface ErisConnectionListener extends ErisInternetConnectionListner{
    public void onConnect(boolean value);
    public void onDisconnect();
    public void onException(Exception e);
}