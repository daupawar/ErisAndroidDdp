(Depreacted as [[delight-im/Android-DDP](https://github.com/delight-im/Android-DDP)] supports indb, no need to use this lib)
# ErisAndroidDdp

This is library project based on [[delight-im/Android-DDP](https://github.com/delight-im/Android-DDP)].<br />

## Purpose

To handle collection data in background

## Installation

 * Add this library to your project
   * Declare the Gradle repository in your root `build.gradle`

     ```gradle
     allprojects {
         repositories {
             jcenter()
             maven { url "https://jitpack.io" }
         }
     }
     ```

   * Declare the Gradle dependency in your app module's `build.gradle`

     ```gradle
     dependencies {
         compile 'com.eris.androidddp:erisandroidddp:1.0.7'
     }
     ```

 * Add the Internet permission to your app's `AndroidManifest.xml`:

    ```
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    ```
    
     * Singleton access
      * Accessing the instance (across `Activity` instances)
      ```
      ErisCollectionManager.getInstance();
      ```
      
## Usage
* Creating a new instance of the lib class and connect to server
  ```
  ErisCollectionManager.getInstance().connect(this,"example.meteor.com");
  ```
  
* Add listeners to collection
```
public class MainActivity extends Activity implements  ErisCollectionListener{

 @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ErisCollectionManager manager = ErisCollectionManager.getInstance();
        manager.getCollection("collectionName").setEventListener(RequestListing.this);
        manager.subscribeCollection("collectionName");
  }
    @Override
    public void onDataAddedEris(ErisCollectionRecord collection) {
        Log.i(this, collection.getCollectionName() + " " + collection.getJsonData());
    }
    
    @Override
    public void onDataChangedEris(ErisCollectionRecord collectionData, String removedValuesJson) {
        Log.i(MainActivity.this, collectionData.getCollectionName() + " " + collectionData.getJsonData());
    }
    
    @Override
    public void onDataRemovedEris(String collectionName, String documentID) {
        Log.i(MainActivity.this, collectionName + " " + documentID);
    }
}
```

* Get collection by id
```
ErisCollectionRecord collection = ErisCollectionManager.getInstance().getCollection("collectionName").getCollectionById("documentId");
```

* Remove from collection from collection list
```
 ErisCollectionManager.getInstance().getCollection("collectionName").removeFromListMap("documentId");
```

* Signing in with an existing username
```
ErisCollectionManager.getInstance().loginWithUsername("john", "password", new ResultListener() { });
```

* Signing in with an existing email address
```
ErisCollectionManager.getInstance().loginWithEmail("john.doe@example.com", "password", new ResultListener() { });
```

* Signing in with an existing token (resume loing)
```
ErisCollectionManager.getInstance().loginWithToken("yourtoken", new ResultListener() { });
```

* Call insert method 
```
HashMap<String,Object> values=new HashMap<>();
ErisCollectionManager.getInstance().insert("collectionName",values,new ResultListener() {});
```

* Call remove method
```
ErisCollectionManager.getInstance().remove("collectionName","documentId",new ResultListener() {});
```
* call  update method
```
 ErisCollectionManager.getInstance().update("my-collection", query, values, options, new ResultListener() { });
```

* Call remote method asynchronously 
parameters
1- method name
2- comma seprated values (new Object[]{value1,value2,value3})

```
ErisCollectionManager.getInstance().callMethod("remoteMethodName", new Object[]{}, new ResultListener() {});
```

