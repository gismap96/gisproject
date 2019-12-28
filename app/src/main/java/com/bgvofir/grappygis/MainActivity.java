package com.bgvofir.grappygis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bgvofir.grappygis.LayerCalloutControl.FeatureLayerController;
import com.bgvofir.grappygis.LayerCalloutDialog.DialogLayerAdapter;
import com.bgvofir.grappygis.LayerCalloutDialog.DialogLayerSelectionFragment;
import com.bgvofir.grappygis.LayerDetailsDialog.DialogLayerDetailsAdapter;
import com.bgvofir.grappygis.LayerDetailsDialog.DialogLayerDetailsFragment;
import com.bgvofir.grappygis.LegendSidebar.LegendLayerDisplayController;
import com.bgvofir.grappygis.SketchController.SketchEditorController;
import com.bgvofir.grappygis.SketchController.SketcherEditorTypes;
import com.bgvofir.grappygis.SketchController.SketcherSelectionDialogAdapter;
import com.bgvofir.grappygis.SketchController.SketcherSelectionDialogFragment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureCollection;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.layers.FeatureCollectionLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity implements LocationListener, DialogLayerAdapter.OnRowClickListener, FeatureLayerController.OnLayerClickListener, SketcherSelectionDialogAdapter.OnSketchSelectionClickListener {
    private MapView mMapView;
    private static final String FILE_EXTENSION = ".mmpk";
    private static File extStorDir;
    private static String extSDCardDirName;
    private Callout mCallout;
    private RecyclerView mLayerRecyclerView;
    private MapLayerAdapter mContentAdapter;
    private boolean mIsDistance;
    private Point mFirstDistanceClick;
    private GraphicsOverlay mDistanceOverlay;
    private String locationProvider;
    private LocationManager locationManager;
    private boolean isAllGranted;
    private ProgressBar mapProgress;
    LocationDisplay locationDisplay;
    private boolean isAutoPan;
    private boolean isAddPointMode;
    private boolean isDeletePointMode;
    private PointCollection mPolylinePoints;
    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    private GraphicsOverlay pointsOverlay;
    private SimpleMarkerSymbol mPointSymbol;
    private FeatureCollection mClientFeatureCollection;
    private FeatureCollectionLayer mClientFeatureCollectionLayer;
    private String mCurrentDescription;
    private ArrayList<ClientPoint> mClientPoints;
    SharedPreferences mPrefs;
    private float mCurrentX;
    private float mCurrentY;
    private ImageView toggleMenuBtn;
    private ImageView toggledistanceBtn;
    private ImageView addPoint;
    private ImageView toggleAutoPanBtn;
    private ImageView ivDeletePoint;
    private ImageView toggleFreehandBtn;
    private String mProjectId;
    private ListenableFuture<FeatureQueryResult> selectionResult;
    private String mCurrentCategory;
    private boolean mCurrentIsUpdateSys;
    private boolean activityAlive;
    private DialogLayerSelectionFragment dialogLayerSelectionFragment;
    private SketcherSelectionDialogFragment sketcherSelectionDialogFragment;
    private android.graphics.Point screenPoint;
    private ConstraintLayout bottomSketchBarContainer;
    private TextView closeSketcherTV;
    private TextView undoSkecherTV;
    private ImageView zift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FirebaseApp.initializeApp(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mPrefs = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mProjectId = mPrefs.getString(Consts.PROJECT_ID_KEY, "default");
        addPoint = findViewById(R.id.addPoint);
        ivDeletePoint = findViewById(R.id.deletePoint);
        mapProgress = findViewById(R.id.map_progress);
        toggledistanceBtn = findViewById(R.id.toggledistanceBtn);
        toggledistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mIsDistance = !mIsDistance;
//                if (mIsDistance){
//                    toggledistanceBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
//                }
//                else{
//                    toggledistanceBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
//                }

                resetMenuFunctions();
                if (MainUpperMenu.INSTANCE.measureLine()) {
                    toggledistanceBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                    mIsDistance = true;
                } else {
                    toggledistanceBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });
        toggleMenuBtn = findViewById(R.id.toggleLegendBtn);
        toggleMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleLayerList();
            }
        });


        addPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddPoint(true);
//                takePhoto();
            }
        });
        toggleFreehandBtn = findViewById(R.id.toggleFreehandBtn);
        toggleFreehandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SketchEditorController.INSTANCE.freehandMode(mMapView);
                SketcherSelectionDialogAdapter sketcherSelectionDialogAdapter = new SketcherSelectionDialogAdapter(MainActivity.this);
                sketcherSelectionDialogFragment = new SketcherSelectionDialogFragment(MainActivity.this, sketcherSelectionDialogAdapter);
                sketcherSelectionDialogFragment.show();
            }
        });
//        toggleFreehandBtn.setVisibility(View.GONE);
        toggleAutoPanBtn = findViewById(R.id.toggleAutoPanBtn);
        toggleAutoPanBtn.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (isAllGranted){
                    isAutoPan = !isAutoPan;
                    if (isAutoPan){
                        toggleAutoPanBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                    }
                    else{
                        toggleAutoPanBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.OFF);

                    }

                }
            }
        });



        ivDeletePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                isDeletePointMode = !isDeletePointMode;
//                if (isDeletePointMode){
//                    ivDeletePoint.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
//                }
//                else{
//                    ivDeletePoint.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
//                }
                resetMenuFunctions();
                if (MainUpperMenu.INSTANCE.trashClicked()){
                    ivDeletePoint.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                    isDeletePointMode = true;
                }  else {
                    ivDeletePoint.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });

        mMapView = findViewById(R.id.mapView);
        mDistanceOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mDistanceOverlay);
        locationDisplay = mMapView.getLocationDisplay();

//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);

        mCallout = mMapView.getCallout();

        mLayerRecyclerView = (RecyclerView) findViewById(R.id.mapLayerRecyclerView);
        mLayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mContentAdapter = new MapLayerAdapter(this);
        mLayerRecyclerView.setAdapter(mContentAdapter);
        extStorDir = Environment.getExternalStorageDirectory();
        Dexter.withActivity(this).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new BaseMultiplePermissionsListener(){
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        super.onPermissionsChecked(report);
                        if (report.areAllPermissionsGranted()){
                            isAllGranted = true;
                            locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);
                            locationDisplay.startAsync();
//                            initMap();
                            downloadMMPK();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        super.onPermissionRationaleShouldBeShown(permissions, token);
                    }
                }).check();

        undoSkecherTV = findViewById(R.id.undoSkecherTV);
        undoSkecherTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SketchEditorController.INSTANCE.undo();
            }
        });
        closeSketcherTV = findViewById(R.id.closeSketcherTV);
        closeSketcherTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SketchEditorController.INSTANCE.stopSketcher(bottomSketchBarContainer);
            }
        });
        bottomSketchBarContainer = findViewById(R.id.bottomSketcherControllerBarContainer);
        SketchEditorController.INSTANCE.initSketchBarContainer(bottomSketchBarContainer);
        zift = findViewById(R.id.toggleZift);
        zift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LegendLayerDisplayController.INSTANCE.fetchMMap(mProjectId);
            }
        });

    }

    private String getJsonDataFromFile(File file){
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(file));
            String line;
            StringBuffer content = new StringBuffer();
            char[] buffer = new char[1024];
            int num;
            while ((num = input.read(buffer)) > 0) {
                content.append(buffer, 0, num);
            }
            return content.toString();

        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void resetMenuFunctions(){
        mDistanceOverlay.getGraphics().clear();
        mIsDistance = false;
        isAddPointMode = false;
        isDeletePointMode = false;
        addPoint.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toggledistanceBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        ivDeletePoint.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

    }

    private void setClientPoints(){
        mClientPoints = new ArrayList<>();
        StorageReference pointsRef = storageReference.child("settlements/" + mProjectId + "/layers/points.json");
        String pointsFilePath = getPointLocalFilePath();
        File pointsFile = new File(pointsFilePath);
        try {
            pointsFile.getParentFile().mkdirs();
            pointsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pointsRef.getFile(pointsFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d("MainActivity", "points file: " + pointsFile);
                try {
                    JSONArray pointsJson = new JSONArray(getJsonDataFromFile(pointsFile));
                    for (int i = 0; i < pointsJson.length(); i++){
                        Gson gson = new Gson();
                        ClientPoint clientPoint = gson.fromJson(pointsJson.getJSONObject(i).toString(), ClientPoint.class);
                        mClientPoints.add(clientPoint);
                        createFeatureCollection(clientPoint.getX(), clientPoint.getY(), clientPoint.getDescription(), clientPoint.getImageUrl(), clientPoint.getCategory(), clientPoint.isUpdateSystem());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("MainActivity", "points file failed: " + e.getMessage());
            }
        });



/*        mClientPoints = new ArrayList<>();
        if (mPrefs.contains(ClientPoint.POINTS_DATA_KEY)){
            Set<String> pointsDataStringSet = mPrefs.getStringSet(ClientPoint.POINTS_DATA_KEY, new HashSet<String>());
            for (String pointString : pointsDataStringSet){
                Gson gson = new Gson();
                ClientPoint clientPoint = gson.fromJson(pointString, ClientPoint.class);
                mClientPoints.add(clientPoint);
                createFeatureCollection(clientPoint.getX(), clientPoint.getY(), clientPoint.getDescription(), clientPoint.getImageUrl());
            }
        }*/
    }

    private void saveClientPoints(Boolean isLast){
        SharedPreferences.Editor editor = mPrefs.edit();
        Set<String> pointsDatStringSet = new HashSet<String>();

        JSONArray storageArray = new JSONArray();

        for(ClientPoint point : mClientPoints){
            Gson gson = new Gson();
            String currentPointString = gson.toJson(point);
            try {
                storageArray.put(new JSONObject(currentPointString));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pointsDatStringSet.add(currentPointString);
        }
        editor.putStringSet(ClientPoint.POINTS_DATA_KEY, pointsDatStringSet);
        editor.apply();



        StorageReference pointsRef = storageReference.child("settlements/" + mProjectId + "/layers/points.json");
        pointsRef.putBytes(storageArray.toString().getBytes())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("FireBase","success add points to firebase");
                        setClientPoints();
                        if (isLast) {
                            mapProgress.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void downloadMMPK(){
        String mmpkFilePath = createMobileMapPackageFilePath(mProjectId);
        File mmpkFile = new File(mmpkFilePath);
        try {
            mmpkFile.getParentFile().mkdirs();
            mmpkFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Downloading...");
        progressDialog.show();


        StorageReference mmpkRef = storageReference.child("settlements/" + mProjectId + "/mmpk/data.mmpk");

        mmpkRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long timeModified = storageMetadata.getUpdatedTimeMillis();
                long lastDownloadTime = mPrefs.getLong(Consts.DOWNLOAD_TIME_KEY, Long.MIN_VALUE);

                if (timeModified > lastDownloadTime){
//                    mmpkFile.delete();
//                    deleteMMPKFolderData();
                    if (lastDownloadTime != Long.MIN_VALUE){
                        deleteMMPKFolderData();
                    }
                    mmpkRef.getFile(mmpkFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d("MainActivity", "modified download success: " + mmpkFile.getAbsolutePath());
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putLong(Consts.DOWNLOAD_TIME_KEY, System.currentTimeMillis());
                            editor.apply();
                            initMap(mmpkFile.getAbsolutePath());
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("MainActivity", "modified download failed", e);
                            progressDialog.dismiss();
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            Log.d("MainActivity", "modified download progress " + taskSnapshot.getBytesTransferred());
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            if(progress >= 0)
                                progressDialog.setMessage("Downloaded "+(int)progress+"%");
                        }
                    });
                }
                else{
                    initMap(mmpkFile.getAbsolutePath());
                    progressDialog.dismiss();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("MainActivity", "modified download failed", e);
            }
        });


    }

    private void toggleAddPoint(boolean isOn){
//        if (isAddPointMode){
//            addPoint.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
//        }
//        else{
//            addPoint.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
//        }
        resetMenuFunctions();
        if (MainUpperMenu.INSTANCE.addPointClicked() && isOn){
            addPoint.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            isAddPointMode = isOn;
        } else {
            addPoint.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        }
    }

    private void initMap(String mmpkFileURL){
//        String mmpkFileURL = createMobileMapPackageFilePath("shfayim_full");
        String unpackedMmpkPath = getUnpackedPath("data");
        final ListenableFuture<Boolean> directReadSupportedFuture = MobileMapPackage.isDirectReadSupportedAsync(mmpkFileURL);
        directReadSupportedFuture.addDoneListener(new Runnable() {
            @Override public void run() {
                // Get the result of the future
                boolean directReadSupported = false;
                try {
                    directReadSupported = directReadSupportedFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e("MainActivity", "some error1");
                }

                // If the mobile map package supports direct read
                if (directReadSupported) {
                    // Create the mobile map package from the .mmpk file
                    MobileMapPackage mobileMapPackage = new MobileMapPackage(mmpkFileURL);
                    loadMmpk(mobileMapPackage);
                } else {

                    // If the mobile map package file does NOT support direct read
                    // Unpack the mobile map package file into a directory
                    //deleteUnpackedMMPKFolderData("data");
                    File folder = new File(unpackedMmpkPath);
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdirs();
                    }
                    if (success){
                        MobileMapPackage.unpackAsync(mmpkFileURL, unpackedMmpkPath).addDoneListener(new Runnable() {
                            @Override
                            public void run() {
                                // Create the mobile map package from the unpack directory
                                MobileMapPackage mobileMapPackage = new MobileMapPackage(unpackedMmpkPath);
                                loadMmpk(mobileMapPackage);
                            }
                        });
                    }
                }
            }
        });
    }

    private void loadMmpk(MobileMapPackage mobileMapPackage) {
        mobileMapPackage.addDoneLoadingListener(() -> {
            if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED) {
                System.out.println("Number of maps = " + mobileMapPackage.getMaps().size());
                // In this case the first map in the array is obtained
                ArcGISMap mobileMap = mobileMapPackage.getMaps().get(0);
                mMapView.setMap(mobileMap);
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setClientPoints();
                        setMapListener(mobileMap);
                        Envelope myExtents = mobileMap.getOperationalLayers().get(0).getFullExtent();
                        myExtents = (Envelope) GeometryEngine.project(myExtents, mMapView.getSpatialReference());
//                mMapView.setMaxExtent(myExtents);
                        mMapView.setViewpoint(new Viewpoint(myExtents));

                    }
                },500);
                final List<Layer> layerList = mobileMap.getOperationalLayers();
                mContentAdapter.setLayerList(layerList);


            } else {
                //todo If loading failed, deal with failure depending on the cause...
                Log.e("MainActivity", "some error");
            }
        });
        mobileMapPackage.loadAsync();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setMapListener(ArcGISMap mobileMap){
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // remove any existing callouts
                if (mCallout.isShowing()) {
                    mCallout.dismiss();
                }
                // get the point that was clicked and convert it to a point in map coordinates
                if (mIsDistance){
                    calculateDistance(e);
                }
                else if (isAddPointMode){
                    startDescriptionDialog(e);
                }
                else{
                    //showLayerData(e, mobileMap);
                    screenPoint = new android.graphics.Point(Math.round(e.getX()),
                            Math.round(e.getY()));
                    FeatureLayerController.INSTANCE.layerClicked(screenPoint, mMapView, MainActivity.this);
//                    Map<String, String> mMap = ArrayDump.INSTANCE.getItem();
//                    DialogLayerAdapter dialogLayerAdapter = new DialogLayerAdapter(mMap, MainActivity.this);
//                    dialogLayerSelectionFragment = new DialogLayerSelectionFragment(MainActivity.this, dialogLayerAdapter);
//                    dialogLayerSelectionFragment.show();


//                    mobileMap.getOperationalLayers().get(0).getFullExtent();
                }


                return super.onSingleTapConfirmed(e);
            }
        });
    }

    private void startDescriptionDialog(MotionEvent e){
        DescriptionDialog descriptionDialog = new DescriptionDialog(this, new DescriptionDialog.IDescriptionDialogListener() {
            @Override
            public void onConfirm(String description, String category, boolean isUpdateSys) {
                showTakePhotoAlertDialog(e, description, category, isUpdateSys);
            }

            @Override
            public void onCanceled() {
                toggleAddPoint(false);
            }
        });
        descriptionDialog.setCancelable(false);
        descriptionDialog.show();
    }

    private void showTakePhotoAlertDialog(MotionEvent e, String description, String category, boolean isUpdateSys){
        AlertDialog.Builder builder;
        Point locationPoint = mMapView
                .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.add_photo)
                .setMessage(R.string.take_photo_prompt)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        takePhoto((float) locationPoint.getX(), (float) locationPoint.getY(), description, category, isUpdateSys);
                        toggleAddPoint(false);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        createFeatureCollection((float) locationPoint.getX(), (float) locationPoint.getY(), description, null, category, isUpdateSys);
                        mClientPoints.add(new ClientPoint((float) locationPoint.getX(), (float) locationPoint.getY(), description, null, category, isUpdateSys));
                        saveClientPoints(false);
                        toggleAddPoint(false);
                    }
                })
                .setIcon(R.drawable.ic_add_photo)
                .show();


    }

    private void showDeletePointDialog(int pointHash){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.delete_point)
                .setMessage(R.string.delete_this_point)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //if (mClientPoints.size() > 0){mapProgress.setVisibility(View.VISIBLE);}
                        for (int i = 0; i < mClientPoints.size(); i++){
                            if(mClientPoints.get(i).getPointHash() == pointHash){
                                mClientPoints.remove(i);
                                mClientFeatureCollection = null;
                                mMapView.getMap().getOperationalLayers().remove(mClientFeatureCollectionLayer);

                                saveClientPoints(i == mClientPoints.size());

                                break;
                            }
                        }
                        resetMenuFunctions();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ivDeletePoint.performClick();
                        resetMenuFunctions();
                    }
                })
                .setIcon(R.drawable.ic_trash)
                .show();
    }

    private void calculateDistance(MotionEvent e){
        if (mFirstDistanceClick == null){
            mPolylinePoints = new PointCollection(mMapView.getSpatialReference());
            mFirstDistanceClick = mMapView
                    .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
            mPolylinePoints.add(mFirstDistanceClick);
            SimpleMarkerSymbol markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.CYAN, 10);
            mDistanceOverlay.getGraphics().add(new Graphic(mFirstDistanceClick, markerSymbol));
        }
        else{
            Point secondDistanceClick = mMapView
                    .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
            mPolylinePoints.add(secondDistanceClick);

            Polyline polyline = new Polyline(mPolylinePoints);
            SimpleLineSymbol polylineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3.0f);
            Graphic polylineGraphic = new Graphic(polyline, polylineSymbol);
            mDistanceOverlay.getGraphics().add(polylineGraphic);
            mDistanceOverlay.getGraphics().add(getTextSymbolForLine(mFirstDistanceClick, secondDistanceClick, "Distance: " + getDistanceBetweenTwoPoints(mFirstDistanceClick, secondDistanceClick)));
//            getTextSymbolForLine(mFirstDistanceClick, secondDistanceClick, "Distance: " + getDistanceBetweenTwoPoints(mFirstDistanceClick, secondDistanceClick));
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.mapContainer), getDistanceBetweenTwoPoints(mFirstDistanceClick, secondDistanceClick), Snackbar.LENGTH_LONG);
            Handler clearOverlayHandler = new Handler();
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            clearOverlayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDistanceOverlay.getGraphics().clear();
                    toggledistanceBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                    MainUpperMenu.INSTANCE.resetMenu();
//                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }, 5000);
            snackbar.show();
            mIsDistance = false;
            mFirstDistanceClick = null;
        }
    }

    private Graphic getTextSymbolForLine(Point a, Point b, String text){

        double x = (a.getX() + b.getX()) / 2;
        double y = (a.getY() + b.getY()) / 2;

        Point textPoint = new Point(x, y, mMapView.getSpatialReference());
        TextSymbol textSymbol =
                new TextSymbol(
                        20, text, Color.argb(255, 0, 0, 0),
                        TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.BOTTOM);

        return new Graphic(textPoint, textSymbol);


    }

    private String getDistanceBetweenTwoPoints(Point a, Point b){
        double distance = Math.sqrt(Math.pow(a.getX()-b.getX(), 2) + (Math.pow(a.getY() - b.getY(), 2)));
        return String.format(Locale.ENGLISH, "%.2f Meters", distance);
    }



    private void createFeatureCollection(float x, float y, String description, String imageUrl, String category, boolean isUpdateSys) {
        if (!activityAlive) return;

        if (mMapView != null) {
            if (mClientFeatureCollection == null || mClientFeatureCollectionLayer == null){
                mClientFeatureCollection = new FeatureCollection();
                mClientFeatureCollectionLayer = new FeatureCollectionLayer(mClientFeatureCollection );
                mMapView.getMap().getOperationalLayers().add(mClientFeatureCollectionLayer);
            }


//            createPointTable(mClientFeatureCollection, x, y,description, imageUrl);

            List<Feature> features = new ArrayList<>();
            List<Field> pointFields = new ArrayList<>();
            pointFields.add(Field.createString("Description", getString(R.string.description_alias), 50));
            pointFields.add(Field.createString("URL", getString(R.string.pic_url_alias), 255));
            pointFields.add(Field.createString("Category", getString(R.string.category), 255));
            pointFields.add(Field.createString("Update system", getString(R.string.update_system), 50));
            pointFields.add(Field.createInteger("CustomPointHash", "CustomPointHash"));
            FeatureCollectionTable pointsTable = new FeatureCollectionTable(pointFields, GeometryType.POINT, mMapView.getSpatialReference());
//            SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.GREEN, 12);

            BitmapDrawable pinStarDrawable = new BitmapDrawable(getResources(), getBitmapFromVectorDrawable(R.drawable.ic_star_blue));
            final PictureMarkerSymbol pinStarBlueSymbol = new PictureMarkerSymbol(pinStarDrawable);
            //Optionally set the size, if not set the image will be auto sized based on its size in pixels,
            //its appearance would then differ across devices with different resolutions.
            pinStarBlueSymbol.setHeight(14);
            pinStarBlueSymbol.setWidth(14);



            SimpleRenderer renderer = new SimpleRenderer(pinStarBlueSymbol);
            pointsTable.setRenderer(renderer);
            mClientFeatureCollection.getTables().add(pointsTable);

            Map<String, Object> attributes1 = new HashMap<>();
            attributes1.put(pointFields.get(0).getName(), description);
            attributes1.put(pointFields.get(1).getName(), imageUrl);
            attributes1.put(pointFields.get(2).getName(), category);
            attributes1.put(pointFields.get(3).getName(), isUpdateSys ? "Yes" : "No");
            attributes1.put(pointFields.get(4).getName(), ClientPoint.createPointHash(x, y, imageUrl, description, category, isUpdateSys));
            Point point1 = new Point(x, y, mMapView.getSpatialReference());
            features.add(pointsTable.createFeature(attributes1, point1));
            pointsTable.addFeaturesAsync(features);

//            saveClientPoints();
        }
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }



    private void createPointTable(FeatureCollection clientFeatureCollection, float x, float y, String description, String imageUrl) {
        List<Feature> features = new ArrayList<>();
        List<Field> pointFields = new ArrayList<>();
        pointFields.add(Field.createString("Description", "Description", 50));
        pointFields.add(Field.createString("URL", "URL", 255));
        FeatureCollectionTable pointsTable = new FeatureCollectionTable(pointFields, GeometryType.POINT, mMapView.getSpatialReference());
        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFF0000FF, 18);
        SimpleRenderer renderer = new SimpleRenderer(simpleMarkerSymbol);
        pointsTable.setRenderer(renderer);
        clientFeatureCollection.getTables().add(pointsTable);

        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put(pointFields.get(0).getName(), description);
        Point point1 = new Point(x, y, mMapView.getSpatialReference());
        features.add(pointsTable.createFeature(attributes1, point1));

        pointsTable.addFeaturesAsync(features);
    }

    private void addPointToLayer(float x, float y){
        if (pointsOverlay == null){
            pointsOverlay = new GraphicsOverlay();
            mMapView.getGraphicsOverlays().add(pointsOverlay);
        }
        if (mPointSymbol == null){
            mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.GREEN, 15);
        }
        Point point = mMapView.screenToLocation(new android.graphics.Point(Math.round(x), Math.round(y)));

        pointsOverlay.getGraphics().add(new Graphic(point, mPointSymbol));
    }



    private void showLayerData(MotionEvent e, ArcGISMap mobileMap){
        final Point clickPoint = mMapView
                .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
        // create a selection tolerance
        int tolerance = isDeletePointMode ? 20 : 20;
        double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
        // use tolerance to create an envelope to query
        Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, mobileMap.getSpatialReference());
        QueryParameters query = new QueryParameters();
        query.setGeometry(envelope);
        // request all available attribute fields

        ArrayList<FeatureLayer> layers = new ArrayList<>();

        for (int i = 0; i < mobileMap.getOperationalLayers().size(); i++){
            if (mobileMap.getOperationalLayers().get(i) instanceof FeatureLayer){
                if (mobileMap.getOperationalLayers().get(i).isVisible())
                    layers.add((FeatureLayer) mobileMap.getOperationalLayers().get(i));
            }
        }
        try{
            if (mClientFeatureCollectionLayer != null && mClientFeatureCollectionLayer.getLayers() != null)
                for (FeatureLayer layer :mClientFeatureCollectionLayer.getLayers()){
                    layers.add(layer);
                }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

//        final List<ListenableFuture<FeatureQueryResult>> futures = new ArrayList<>();
        final boolean[] breakLoop = {false};
        for (int i = 0; i < layers.size() && !breakLoop[0]; i++){
//            futures.add(layers.get(i).getFeatureTable().queryFeaturesAsync(query));
            if (!layers.get(i).isVisible())
                continue;
            int finalI = i;
            layers.get(i).getFeatureTable().queryFeaturesAsync(query).addDoneListener(new Runnable() {
                @Override
                public void run() {



                    try {
                        //call get on the future to get the result
                        FeatureQueryResult result = layers.get(finalI).getFeatureTable().queryFeaturesAsync(query).get();
                        // create an Iterator
                        Iterator<Feature> iterator = result.iterator();
                        // create a TextView to display field values
                        TextView calloutContent = new TextView(getApplicationContext());
                        calloutContent.setTextColor(Color.BLACK);
                        calloutContent.setSingleLine(false);
                        calloutContent.setVerticalScrollBarEnabled(true);
                        calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                        calloutContent.setMovementMethod(new ScrollingMovementMethod());
//                        calloutContent.setLines(5);
                        // cycle through selections
                        int counter = 0;
                        Feature feature;
                        while (iterator.hasNext() && !breakLoop[0]) {
                            feature = iterator.next();


                            // create a Map of all available attributes as name value pairs
                            Map<String, Object> attr = feature.getAttributes();
                            Set<String> keys = attr.keySet();
                            calloutContent.append(getString(R.string.layer) + ": " + layers.get(finalI).getName());
                            for (String key : keys) {
                                if (isDeletePointMode && key.toLowerCase().contains("custompointhash")){
                                    deletePoint(Integer.parseInt(attr.get(key).toString()));
                                    breakLoop[0] = true;
                                    resetMenuFunctions();
                                    return;
                                }
                                if(!key.toLowerCase().contains("fid") && !key.toLowerCase().contains("source") && !key.toLowerCase().contains("custompointhash") && !key.toLowerCase().contains("objectid")) {
                                    Object value = attr.get(key);
                                    // format observed field value as date
                                    if (value instanceof GregorianCalendar) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                        value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                    }
                                    /*if (key.equalsIgnoreCase("url")){
                                        value = Html.fromHtml("<a href=\"" + value + "\">Website</a>");
                                    }*/

                                    // append name value pairs to TextView
                                    calloutContent.setLinksClickable(true);
                                    calloutContent.setClickable(true);
                                    calloutContent.setAutoLinkMask(Linkify.WEB_URLS);

                                    calloutContent.append(key + ": " + value + "\n");


                                }
                            }
                            counter++;
                            // center the mapview on selected feature
//                            Envelope envelope = feature.getGeometry().getExtent();
//                            mMapView.setViewpointGeometryAsync(envelope, 200);
                            // show CallOut
                            mCallout.setLocation(clickPoint);
                            mCallout.setContent(calloutContent);
                            mCallout.show();
                            breakLoop[0] = true;
                        }
                    } catch (Exception e) {
                        Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void deletePoint(int pointHash){
        showDeletePointDialog(pointHash);

    }

    private String createMobileMapPackageFilePath(String fileName) {
        return getMMPKFolderPath() + File.separator +  fileName
                + FILE_EXTENSION;
    }

    private String getPointLocalFilePath(){
        return extStorDir.getAbsolutePath() + File.separator + Consts.GRAPPY_FOLDER_NAME + File.separator + mProjectId + File.separator + "layers" + File.separator +  "points.json";
    }

    private String getUnpackedPath(String fileName) {
        return getMMPKFolderPath() + File.separator + "Unpacked" + File.separator + fileName;
    }

    private String getMMPKFolderPath(){
        return extStorDir.getAbsolutePath() + File.separator +  Consts.GRAPPY_FOLDER_NAME + File.separator + mProjectId + File.separator +"mmpk";
    }

    private void deleteUnpackedMMPKFolderData(){
        File mmpkFolder = new File(getUnpackedPath("data"));
        if (!mmpkFolder.exists())
            return;
        deleteRecursive(mmpkFolder);
    }

    private boolean deleteMMPKFolderData(){
        File mmpkFolder = new File(getMMPKFolderPath());
        if (!mmpkFolder.exists())
            return false;
        String mmpkFilePath = createMobileMapPackageFilePath(mProjectId);
        File mmpkFile = new File(mmpkFilePath);
        deleteUnpackedMMPKFolderData();
        mmpkFile.delete();
        return mmpkFolder.delete();

    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
            {
                child.delete();
                deleteRecursive(child);
            }

        fileOrDirectory.delete();
    }

    private void toggleLayerList() {

        TransitionManager.beginDelayedTransition(findViewById(R.id.mapContainer));
        if (mLayerRecyclerView.getVisibility() == android.view.View.GONE){
            //final ViewGroup.LayoutParams params = mLayerRecyclerView.getLayoutParams();
/*            mLayerRecyclerView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));*/

            toggleMenuBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);



            mLayerRecyclerView.requestLayout();
            mLayerRecyclerView.setVisibility(android.view.View.VISIBLE);
        }else{
            mLayerRecyclerView.setVisibility(android.view.View.GONE);
/*            mLayerRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 4));*/
            toggleMenuBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            mLayerRecyclerView.requestLayout();
        }
    }



    public void takePhoto(float x, float y, String description, String category, boolean isUpdateSys) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        mCurrentX = x;
        mCurrentY = y;
        mCurrentDescription = description;
        mCurrentCategory = category;
        mCurrentIsUpdateSys = isUpdateSys;
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK/* && data != null*/) {
                    uploadImage(imageUri);
                    /*Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ImageView imageView = (ImageView) findViewById(R.id.ImageView);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media
                                .getBitmap(cr, selectedImage);

                        imageView.setImageBitmap(bitmap);
                        Toast.makeText(this, selectedImage.toString(),
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
                                .show();
                        Log.e("Camera", e.toString());
                    }*/
                } else {
                    if (mCurrentX != 0 && mCurrentY != 0 && !mCurrentDescription.isEmpty()){
                        createFeatureCollection(mCurrentX, mCurrentY, mCurrentDescription, null, mCurrentCategory, mCurrentIsUpdateSys);
                        mClientPoints.add(new ClientPoint((float) mCurrentX, mCurrentY, mCurrentDescription, null, mCurrentCategory, mCurrentIsUpdateSys));
                        saveClientPoints(false);
                        toggleAddPoint(false);
                    }
                }
        }
    }

    public Uri reduceImageSize(Uri uri){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 8;
            // factor of downsizing the image

            File file = new File(uri.getPath());

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            final int REQUIRED_SIZE = 80;

            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);
            return Uri.fromFile(file);
        } catch (Exception e) {
            return null;
        }
    }


    private void uploadImage(Uri uri) {

        if(uri != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            StorageReference ref = storageReference.child("settlements/" + mProjectId + "/images/"+ UUID.randomUUID().toString());
            ref.putFile(reduceImageSize(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    if (mCurrentX != 0 && mCurrentY != 0 && !mCurrentDescription.isEmpty()){
                                        mClientPoints.add(new ClientPoint(mCurrentX,  mCurrentY, mCurrentDescription, uri.toString(), mCurrentCategory, mCurrentIsUpdateSys));
                                        saveClientPoints(false);
                                        createFeatureCollection(mCurrentX, mCurrentY, mCurrentDescription, uri.toString(), mCurrentCategory, mCurrentIsUpdateSys);
                                        mCurrentX = 0;
                                        mCurrentY = 0;
                                        mCurrentDescription = null;
                                    }
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }


    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityAlive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        activityAlive = false;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRowClickListener(@NotNull String layerIndex, @NonNull IdentifyLayerResult layerResult) {
        dialogLayerSelectionFragment.dismiss();
        showDetailsDialog(layerResult);


    }

    private void showDetailsDialog(@NonNull IdentifyLayerResult layerResult) {
        ArrayList<Map<String, String>> displayMap = FeatureLayerController.INSTANCE.layerDetails(layerResult);
        DialogLayerDetailsAdapter dialogLayerDetailsAdapter = new DialogLayerDetailsAdapter(this, displayMap);
        String layerTitle = layerResult.getLayerContent().getName();
        DialogLayerDetailsFragment dialogLayerDetailsFragment = new DialogLayerDetailsFragment(this, dialogLayerDetailsAdapter, layerTitle, layerResult);
        dialogLayerDetailsFragment.show();
    }

    @Override
    public void onLayerClickListener(@NotNull ArrayList<String> layerNames, @NotNull List<IdentifyLayerResult> identifiedLayers) {
        if (isDeletePointMode){
            Integer pointHash = FeatureLayerController.INSTANCE.identifyAttForPointDeletion(identifiedLayers);
            deletePoint(pointHash);
            return;
        }
        if (layerNames.size()>1) {
            DialogLayerAdapter dialogLayerAdapter = new DialogLayerAdapter(this, layerNames, this, identifiedLayers);
            dialogLayerSelectionFragment = new DialogLayerSelectionFragment(MainActivity.this, dialogLayerAdapter);
            dialogLayerSelectionFragment.show();
        } else if ((layerNames.size() == 1)){
            showDetailsDialog(identifiedLayers.get(0));
        }
    }

    @Override
    public void onSketchSelectionListener(@NotNull SketcherEditorTypes sketcher) {
        sketcherSelectionDialogFragment.dismiss();
        SketchEditorController.INSTANCE.startSketching(sketcher, mMapView);
        SketchEditorController.INSTANCE.openSketcherBarContainer(bottomSketchBarContainer);
    }
}
