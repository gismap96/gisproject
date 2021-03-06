package com.grappiapp.grappygis;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.mapping.Basemap;
import com.grappiapp.grappygis.Basemap.BasemapController;
import com.grappiapp.grappygis.ClientFeatureLayers.ClientFeatureCollectionLayer;
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPointFeatureCollection;
import com.grappiapp.grappygis.ClientFeatureLayers.ClientPolygonFeatureCollection;
import com.grappiapp.grappygis.ClientLayerPhotoController.ClientPhotoController;
import com.grappiapp.grappygis.ClientLayersHandler.ClientLayersController;
import com.grappiapp.grappygis.DownloadController.DownloadController;
import com.grappiapp.grappygis.EmailUpdate.EmailUpdateController;
import com.grappiapp.grappygis.GeoViewController.GeoViewController;
import com.grappiapp.grappygis.LayerCalloutControl.FeatureLayerController;
import com.grappiapp.grappygis.LayerCalloutDialog.DialogLayerAdapter;
import com.grappiapp.grappygis.LayerCalloutDialog.DialogLayerSelectionFragment;
import com.grappiapp.grappygis.LayerDetailsDialog.DialogLayerDetailsAdapter;
import com.grappiapp.grappygis.LayerDetailsDialog.DialogLayerDetailsFragment;
import com.grappiapp.grappygis.LegendSidebar.LegendGroup;
import com.grappiapp.grappygis.LegendSidebar.LegendLayerDisplayController;
import com.grappiapp.grappygis.LegendSidebar.LegendSidebarAdapter;
import com.grappiapp.grappygis.OfflineMode.OfflineModeController;
import com.grappiapp.grappygis.ProjectRelated.MapProperties;
import com.grappiapp.grappygis.ProjectRelated.ProjectId;
import com.grappiapp.grappygis.ProjectRelated.UserPoints;
import com.grappiapp.grappygis.ProjectRelated.UserPolygon;
import com.grappiapp.grappygis.ProjectRelated.UserPolyline;
import com.grappiapp.grappygis.SearchController.FeatureSearchController;
import com.grappiapp.grappygis.SearchController.SearchDialogFragment;
import com.grappiapp.grappygis.SearchController.SearchResultsAdapter;
import com.grappiapp.grappygis.SketchController.SketchEditorController;
import com.grappiapp.grappygis.SketchController.SketcherEditorTypes;
import com.grappiapp.grappygis.SketchController.SketcherSaveDialogFragment;
import com.grappiapp.grappygis.SketchController.SketcherSaveHydrantsDialogFragment;
import com.grappiapp.grappygis.SketchController.SketcherSelectionDialogAdapter;
import com.grappiapp.grappygis.SketchController.SketcherSelectionDialogFragment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.layers.RasterLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.mapping.view.SketchGeometryChangedEvent;
import com.esri.arcgisruntime.mapping.view.SketchGeometryChangedListener;
import com.esri.arcgisruntime.raster.Raster;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

public class MainActivity extends FragmentActivity implements LocationListener, DialogLayerAdapter.OnRowClickListener, FeatureLayerController.OnLayerClickListener, SketcherSelectionDialogAdapter.OnSketchSelectionClickListener
        , LegendLayerDisplayController.LayerGroupsListener, ClientLayersController.OnClientLayersJSONDownloaded, ClientFeatureCollectionLayer.OnPolylineUploadFinish,
        DialogLayerDetailsFragment.OnEditSelectedListener, MapLayerAdapter.OnLegendItemInteraction, SearchResultsAdapter.OnSearchResultClicked
        , ClientPointFeatureCollection.OnPointsUploaded, ClientPolygonFeatureCollection.OnClientPolygonUploadFinished, DownloadController.OnFinishedDownloadListener, SketchEditorController.OnShapeChangeTrackerListener{
    private MapView mMapView;
    private static final String FILE_EXTENSION = ".mmpk";
    private static File extStorDir;
    private Callout mCallout;
    private RecyclerView mLayerRecyclerView;
    private GraphicsOverlay mDistanceOverlay;
    private boolean isAllGranted;
    private ProgressBar mapProgress;
    LocationDisplay locationDisplay;
    private boolean isAutoPan;
    private static final int TAKE_PHOTO_FOR_LAYER = 2;
    private static final int EDIT_PHOTO_FOR_LAYER = 3;
    private static final int TAKE_PHOTO_FOR_GALLERY = 4;
    FirebaseStorage storage;
    StorageReference storageReference;
    SharedPreferences mPrefs;
    private ImageView toggleMenuBtn;
    private ImageView sketchEditorStartIV;
    private ImageView toggleAutoPanBtn;
    private ImageView undoSkecherIV;
    private ImageView zift2;
    private ImageView updateEmailIV;
    private String mProjectId;
    private ConstraintLayout legendDetailsConstraintsLayout;
    private DialogLayerSelectionFragment dialogLayerSelectionFragment;
    private SketcherSelectionDialogFragment sketcherSelectionDialogFragment;
    private android.graphics.Point screenPoint;
    private ConstraintLayout bottomSketchBarContainer;
    private ImageView closeSketcherIV;
    private ImageView offlineModeIV;
    private Viewpoint mViewPoint;
    private LegendSidebarAdapter legendAdapter;
    private TextView calculatePolygonAreaTV;
    private ConstraintLayout measurementConstraintLayout;
//    private boolean displayLegendFlag;
    private ImageView northBarIV;
    private SketchEditor mSketcher;
    private ImageView cleanSketcherIV;
    private ImageView searchFeatureIV;
    private ImageView startTrackingIV;
    private ImageView trackerModeIV;
    private TextView displaySectionForShapeTV;
    private TextView overallSizeHeadlineTV;
    private TextView lengthSectionHeadlineTV;
    private TextView makeLegendGreatAgainTV;
    private ProgressDialog progressDialog;
    private TextView saveShapeTV;
    private boolean isDownloadingRaster = false;
    private ProgressDialog initializingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
//        String selectedLocale = mPrefs.getString(Consts.CHOSEN_LANG_KEY, null);
//        if (selectedLocale != null) {
//            Utils.setLanguage(MainActivity.this, selectedLocale);
//        }
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FirebaseApp.initializeApp(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.uploading_layer));
        progressDialog.setCancelable(false);
        initializingProgressDialog = new ProgressDialog(this);
        initializingProgressDialog.setTitle(getString(R.string.init_dialog_headline));
        initializingProgressDialog.setMessage(getString(R.string.init_dialog_message));
        initializingProgressDialog.setCancelable(false);
        UserPolyline.INSTANCE.initFields();
        UserPolygon.INSTANCE.initFields();
        UserPoints.INSTANCE.initFields();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        measurementConstraintLayout = findViewById(R.id.measurementConstraintLayout);
        overallSizeHeadlineTV = findViewById(R.id.overallSizeHeadlineTV);
        lengthSectionHeadlineTV = findViewById(R.id.lengthSectionHeadlineTV);
        makeLegendGreatAgainTV = findViewById(R.id.makeAllLayersInvisibleTV);
        mPrefs = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        mProjectId = mPrefs.getString(Consts.PROJECT_ID_KEY, "default");
        ProjectId.INSTANCE.setProjectId(mProjectId);
//        addPoint = findViewById(R.id.addPoint);
        displaySectionForShapeTV = findViewById(R.id.displaySectionForShapeTV);
//        deletePointIV = findViewById(R.id.deletePointIV);
        mapProgress = findViewById(R.id.map_progress);
        legendDetailsConstraintsLayout = findViewById(R.id.legendDetailsConstraintsLayout);
        sketchEditorStartIV = findViewById(R.id.sketchEditorIV);
        northBarIV = findViewById(R.id.northBarIV);
        northBarIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateMap(0);
            }
        });
        sketchEditorStartIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetMenuFunctions();
                if (MainUpperMenu.INSTANCE.featureEdit()) {
//                    sketchEditorStartIV.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
//                    mIsDistance = true;

                   // sketchEditorStartIV.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                    sketchEditorStartIV.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                    SketcherSelectionDialogAdapter sketcherSelectionDialogAdapter = new SketcherSelectionDialogAdapter(MainActivity.this, MainActivity.this);
                    sketcherSelectionDialogFragment = new SketcherSelectionDialogFragment(MainActivity.this, sketcherSelectionDialogAdapter, MainActivity.this);
                    sketcherSelectionDialogFragment.show();
                }
            }
        });
        toggleMenuBtn = findViewById(R.id.toggleLegendBtn);
        toggleMenuBtn.setVisibility(View.INVISIBLE);
        toggleMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleLayerList();
            }
        });
        updateEmailIV = findViewById(R.id.updateEmailIV);
//        updateEmailIV.setVisibility(View.GONE);
        updateEmailIV.setOnClickListener(v -> {
            EmailUpdateController.INSTANCE.sendUpdateMail(MainActivity.this);
        });
        zift2 = findViewById(R.id.zift2);
//        zift2.setVisibility(View.GONE);
//
        zift2.setOnClickListener(v -> {
//                DialogAddFlexibleLayerNameTypeFragment fragment = new DialogAddFlexibleLayerNameTypeFragment(MainActivity.this);
//                fragment.show();
//                EmailUpdateController.INSTANCE.sendUpdateMail(MainActivity.this);
//                BasemapController.INSTANCE.inserBasemap(mMapView);
            SketchEditorController.INSTANCE.trackerSketcherMode(bottomSketchBarContainer, MainActivity.this);
//            SketchEditorController.INSTANCE.startSketchingFreehand(mMapView);
        });
        saveShapeTV = findViewById(R.id.saveShapeTV);
        startTrackingIV = findViewById(R.id.startTrackingIV);
        startTrackingIV.setOnClickListener(v -> {
            SketchEditorController.INSTANCE.playTracker(mMapView, bottomSketchBarContainer,this);
        });
        saveShapeTV.setOnClickListener(v->{
            mViewPoint = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE);
            SketcherEditorTypes type = SketchEditorController.INSTANCE.getSketcherEditorTypes();
            GeoViewController.INSTANCE.setNewSavedViewPoint(mMapView);
            switch (type){
                case HYDRANTS:
                    if (SketchEditorController.INSTANCE.getGeometry() != null && !SketchEditorController.INSTANCE.getGeometry().isEmpty()){
                        SketcherSaveHydrantsDialogFragment layerSave = new SketcherSaveHydrantsDialogFragment(MainActivity.this, mMapView);
                        layerSave.show();
                    }else {
                        Toast toast = Toast.makeText(MainActivity.this, R.string.empty_Point, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        return;
                    }
                    break;
                case MULTIPOINTS:
                case POINT:
                    if (SketchEditorController.INSTANCE.getGeometry() != null && !SketchEditorController.INSTANCE.getGeometry().isEmpty()){
                        if (SketchEditorController.INSTANCE.isEditMode()){
                            String layerId = FeatureLayerController.INSTANCE.getLayerId();
                            Geometry geometry = SketchEditorController.INSTANCE.getGeometry();
                            if (!geometry.isEmpty()){
                                SketchEditorController.INSTANCE.stopSketcher(bottomSketchBarContainer);
                                UserPoints.INSTANCE.getUserPoints().editFeatureGeometry(this, layerId, geometry);
                                resetMenuFunctions();
                                return;
                            } else {
                                Toast.makeText(this, R.string.empty_Point, Toast.LENGTH_LONG).show();
                            }
                        }
                        SketcherSaveDialogFragment layerAttributes = new SketcherSaveDialogFragment(MainActivity.this, mMapView, MainActivity.this, MainActivity.this, progressDialog, false);
                        layerAttributes.show();
                    }else {
                        Toast toast = Toast.makeText(MainActivity.this, R.string.empty_Point, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        return;
                    }
                    break;
                case POLYLINE:
                    if (SketchEditorController.INSTANCE.isPolylineNotEmpty()){
                        if (SketchEditorController.INSTANCE.isEditMode()){
                            String layerId = FeatureLayerController.INSTANCE.getLayerId();
                            Geometry geometry = SketchEditorController.INSTANCE.getGeometry();
                            if (!geometry.isEmpty()){
                                SketchEditorController.INSTANCE.stopSketcher(bottomSketchBarContainer);
                                UserPolyline.INSTANCE.getUserPolyline().editFeatureGeometry(layerId,geometry, MainActivity.this);
                                resetMenuFunctions();
                            } else {
                                Toast.makeText(this, R.string.empty_polyline, Toast.LENGTH_LONG).show();
                            }

                        } else {
                            SketcherSaveDialogFragment layerAttributes = new SketcherSaveDialogFragment(MainActivity.this, mMapView, MainActivity.this, MainActivity.this, progressDialog, false);
                            layerAttributes.show();
                        }
                        //end of edit mode
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, R.string.empty_polyline, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        return;
                    }
                    break;
                case POLYGON:
                    Geometry geometry = SketchEditorController.INSTANCE.getGeometry();
                    if (geometry != null && !geometry.isEmpty()) {
                        if (SketchEditorController.INSTANCE.isEditMode()) {
                            String layerId = FeatureLayerController.INSTANCE.getLayerId();
                            SketchEditorController.INSTANCE.stopSketcher(bottomSketchBarContainer);
                            UserPolygon.INSTANCE.getUserPolygon().editFeatureGeometry(layerId, geometry, MainActivity.this);
                            resetMenuFunctions();
                        } else {
                            SketcherSaveDialogFragment layerAttributes = new SketcherSaveDialogFragment(MainActivity.this, mMapView, MainActivity.this, MainActivity.this, progressDialog, false);
                            layerAttributes.show();
                        }
                        //end edit mode
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, R.string.empty_polygon, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                        return;
                    }
                    break;

            }

        });
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

        mMapView = findViewById(R.id.mapView);
        mDistanceOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mDistanceOverlay);
        locationDisplay = mMapView.getLocationDisplay();

//        mMapView.addNavigationChangedListener(navigationChangedEvent -> GeoViewController.INSTANCE.calculateAndSetCurrentLocation(mMapView));

//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);

        mCallout = mMapView.getCallout();

        mLayerRecyclerView = (RecyclerView) findViewById(R.id.mapLayerRecyclerView);
        mLayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mContentAdapter = new MapLayerAdapter(this);
//        mLayerRecyclerView.setAdapter(mContentAdapter);

        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new BaseMultiplePermissionsListener(){
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        super.onPermissionsChecked(report);
                        extStorDir = Environment.getExternalStorageDirectory();
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

        undoSkecherIV = findViewById(R.id.undoSkecherIV);
        undoSkecherIV.setOnClickListener(v -> SketchEditorController.INSTANCE.undo());
        cleanSketcherIV = findViewById(R.id.cleanSketcherIV);
        cleanSketcherIV.setOnClickListener(v-> {
            SketchEditorController.INSTANCE.clean();
        });
//        closeSketcherTV = findViewById(R.id.closeSketcherTV);
//        closeSketcherTV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                SketchEditorController.INSTANCE.stopSketcher(bottomSketchBarContainer);
//                resetMenuFunctions();
//            }
//        });
        closeSketcherIV = findViewById(R.id.closeSketcherIV);
        closeSketcherIV.setOnClickListener(v -> {
            resetMenuFunctions();
//            deletePointIV.setEnabled(true);
            sketchEditorStartIV.setEnabled(true);
        });
        bottomSketchBarContainer = findViewById(R.id.bottomSketcherControllerBarContainer);
        SketchEditorController.INSTANCE.initSketchBarContainer(bottomSketchBarContainer);
        offlineModeIV = findViewById(R.id.offlineModeIV);
//        offlineModeIV.setVisibility(View.GONE);
        offlineModeIV.setOnClickListener(v -> {
            OfflineModeController controller = OfflineModeController.INSTANCE;
            if (controller.isOfflineMode()){
                offlineModeIV.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
                controller.exitOfflineMode(this);
            }else {
                offlineModeIV.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                controller.startOfflineMode(this);
            }

        });
        makeLegendGreatAgainTV.setOnClickListener(v -> {
            FeatureLayerController.INSTANCE.makeAllLayersInvisible(mMapView, legendAdapter);
        });
        searchFeatureIV = findViewById(R.id.searchFeatureIV);
        searchFeatureIV.setOnClickListener(v->{
            searchFeatureIV.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            SearchDialogFragment searchDialogFragment = new SearchDialogFragment(this, mMapView, this);
            searchDialogFragment.show();
            FeatureSearchController.INSTANCE.unselectFeature();
        });
//        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        calculatePolygonAreaTV = findViewById(R.id.displayOverallForShapeTV);
        calculatePolygonAreaTV.setOnClickListener(v -> FeatureLayerController.INSTANCE.setColor());
        trackerModeIV = findViewById(R.id.trackerModeIV);
        trackerModeIV.setOnClickListener(v -> {
            SketchEditorController.INSTANCE.toggleTrackerMode(bottomSketchBarContainer, this, mMapView, this);
            if (SketchEditorController.INSTANCE.isTrackerPolygon()){
                overallSizeHeadlineTV.setText(R.string.area);
            } else {
                overallSizeHeadlineTV.setText(R.string.length);
            }
        });
    }



    private void resetMenuFunctions(){
        trackerModeIV.setVisibility(View.GONE);
        SketchEditorController.INSTANCE.setTrackerFalse();
        mDistanceOverlay.getGraphics().clear();
        SketchEditorController.INSTANCE.stopSketcher(bottomSketchBarContainer);
        sketchEditorStartIV.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        sketchEditorStartIV.setEnabled(true);
        searchFeatureIV.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        startTrackingIV.setVisibility(View.GONE);
        undoSkecherIV.setVisibility(View.VISIBLE);
        cleanSketcherIV.setVisibility(View.VISIBLE);
        overallSizeHeadlineTV.setText(R.string.length);
        MainUpperMenu.INSTANCE.resetMenu();
    }


    private void gotoXYBylayer(@NonNull Layer layer){
        Envelope myExtents = layer.getFullExtent();
        if (myExtents != null){
            myExtents = (Envelope) GeometryEngine.project(myExtents, mMapView.getSpatialReference());
            mMapView.setViewpoint(new Viewpoint(myExtents));

        }
    }

    /**
     * Get the raster file by extension (for example: .jpg)
     * @param folderPath path to folder
     * @return abs path to file
     */
    private void setRaster(String folderPath){
        File dir = new File(folderPath);
        final boolean[] didZoom = {false};
        File[] ap = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String fileName) {
                if (fileName.endsWith(".jpg") || fileName.endsWith(".tif") || fileName.endsWith(".ecw")){
//                    String rasterPath = file.getAbsolutePath();
                    Raster raster = new Raster(file.getAbsolutePath() + File.separator + fileName);
                    RasterLayer rasterLayer = new RasterLayer(raster);
                    rasterLayer.setName(fileName);



                    mMapView.getMap().getOperationalLayers().add(0, rasterLayer);
                    MapProperties.INSTANCE.setSpatialReference(mMapView.getSpatialReference());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!didZoom[0]){
                                Envelope myExtents = rasterLayer.getFullExtent();
                                if (myExtents != null){
                                    myExtents = (Envelope) GeometryEngine.project(myExtents, mMapView.getSpatialReference());
//                mMapView.setMaxExtent(myExtents);
                                    mMapView.setViewpoint(new Viewpoint(myExtents));
                                    didZoom[0] = true;
                                }
                                initializingProgressDialog.show();
                                rotateMap(0);
//                                initLegendSidebar();
                                ClientLayersController.INSTANCE.fetchClientPolyline(MainActivity.this,MainActivity.this);


                            }
                        }
                    }, 200);

//                    setRaster();
                    return true;
                }
                return false;
            }
        });
    }

    private void initLegendSidebar(){
        List<LegendGroup> legendGroups = LegendLayerDisplayController.INSTANCE.generateLegendSidebar(mMapView);
        legendAdapter = new LegendSidebarAdapter(this, this, legendGroups, mLayerRecyclerView);
        mLayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLayerRecyclerView.setAdapter(legendAdapter);
        toggleMenuBtn.setVisibility(View.VISIBLE);
        RecyclerView.ItemAnimator animator = mLayerRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator){
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }
    private void checkForRaster(){
        File rasterFolderFile = new File(getRasterFolderPath());
        if (!rasterFolderFile.exists()) {
            downloadRaster();
            return;
        }
        final long folderModifiedTime = rasterFolderFile.lastModified();
        StorageReference rasterRef = storageReference.child("settlements/" + mProjectId + "/raster/raster_data.zip");
        rasterRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                if (storageMetadata.getUpdatedTimeMillis() > folderModifiedTime){
                    downloadRaster();
                }
                else{
                    File rasterFolderFile = new File(getRasterFolderPath() + File.separator + "raster_data.zip");
                    if (!rasterFolderFile.exists()) {
                        downloadRaster();
                        return;
                    }
                    setRaster(getRasterFolderPath());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Main", "failed to find raster data");
            }
        });
    }

    private void deleteRasterFolder(){
        File rasterFolder = new File(getRasterFolderPath());
        deleteRecursive(rasterFolder);
    }

    private void downloadRaster(){
        if (isDownloadingRaster){
            return;
        }
        deleteRasterFolder();
        isDownloadingRaster = true;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.initial_download_message));
        progressDialog.show();
        StorageReference rasterRef = storageReference.child("settlements/" + mProjectId + "/raster/raster_data.zip");
        File rasterFolderFile = new File(getRasterFolderPath() + File.separator + "raster_data.zip");
        rasterFolderFile.getParentFile().mkdirs();
        rasterRef.getFile(rasterFolderFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                try {
                    isDownloadingRaster = false;
                    File zipFile = new File(getRasterFolderPath() + File.separator + "raster_data.zip");
                    Utils.unzip(zipFile, new File(getRasterFolderPath()));
                    DownloadController.INSTANCE.downloadMultiple(MainActivity.this, storageReference, getRasterFolderPath(), mProjectId, progressDialog,
                            2,MainActivity.this);
                    //setRaster(getRasterFolderPath());
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("MainActivity", "DownloadRaster failed: " + e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                if(progress >= 0 && progress <= 97) {
                    String msg1 = getString(R.string.please_wait) + "\n";
                    progressDialog.setMessage(msg1+ (int) progress + "%");
                } else if (progress >= 98){
                    String msg = getString(R.string.finalising_download);
                    progressDialog.setMessage(msg);
                }
            }
        });
    }

    private void downloadMMPK(){
        String path = createMobileMapPackageFilePath(mProjectId);
        String mmpkFilePath = path;
        File mmpkFile = new File(mmpkFilePath);
        try {
            mmpkFile.getParentFile().mkdirs();
            mmpkFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.commencing_download));
        progressDialog.show();

        StorageReference mmpkRef = storageReference.child("settlements/" + mProjectId + "/mmpk/data.mmpk");

        mmpkRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long timeModified = storageMetadata.getUpdatedTimeMillis();
                long lastDownloadTime = mPrefs.getLong(Consts.DOWNLOAD_TIME_KEY, Long.MIN_VALUE);
//                lastDownloadTime = Long.MIN_VALUE;
                if (timeModified > lastDownloadTime){
//                    mmpkFile.delete();
                    if (lastDownloadTime != Long.MIN_VALUE){
//                        deleteMMPKFolderData();
                        deleteMMPKFolderData();
                        String path = createMobileMapPackageFilePath(mProjectId);
                        String mmpkFilePath = path;
                        File mmpkFile = new File(mmpkFilePath);
                        try {
                            mmpkFile.getParentFile().mkdirs();
                            mmpkFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
//                    FileMemoryController.INSTANCE.deleteMMPKFile(mmpkFile);
//                    SystemClock.sleep(2000);
                    mmpkRef.getFile(mmpkFile).addOnSuccessListener(taskSnapshot -> {
                        Log.d("MainActivity", "modified download success: " + mmpkFile.getAbsolutePath());
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putLong(Consts.DOWNLOAD_TIME_KEY, System.currentTimeMillis());
                        editor.apply();
                        initMap(mmpkFile.getAbsolutePath());
                        progressDialog.dismiss();
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
        }).addOnFailureListener(e -> Log.e("MainActivity", "modified download failed", e));
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

        mobileMapPackage.loadAsync();
        mobileMapPackage.addDoneLoadingListener(() -> {
            if (mobileMapPackage.getLoadStatus() == LoadStatus.LOADED && !mobileMapPackage.getMaps().isEmpty()) {
                System.out.println("Number of maps = " + mobileMapPackage.getMaps().size());
                // In this case the first map in the array is obtained
                ArcGISMap mobileMap = mobileMapPackage.getMaps().get(0);
                mMapView.setMap(mobileMap);
                BasemapController.INSTANCE.inserBasemap(mMapView);
                LegendLayerDisplayController.INSTANCE.makeLayersInvisible(mMapView);
                LegendLayerDisplayController.INSTANCE.makeAllGroupLayersVisible(mMapView);
                GeoViewController.INSTANCE.setCurrentViewPointForMap(mMapView);
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForRaster();
                        setMapListener(mobileMap);
                        Envelope myExtents = mobileMap.getOperationalLayers().get(0).getFullExtent();
                        if ((myExtents == null)) {
                            if (!isDownloadingRaster){
                                checkForRaster();
                                loadMmpk(mobileMapPackage);
                            }
                        } else {
                            myExtents = (Envelope) GeometryEngine.project(myExtents, mMapView.getSpatialReference());
                            mMapView.setViewpoint(new Viewpoint(myExtents));
                        }
//                mMapView.setMaxExtent(myExtents);


                    }
                },500);

            } else {
                Log.e("MainActivity", "some error");
            }
        });


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
                else{
                    //showLayerData(e, mobileMap);
                    screenPoint = new android.graphics.Point(Math.round(e.getX()),
                            Math.round(e.getY()));
                    FeatureLayerController.INSTANCE.layerClicked(screenPoint, mMapView, MainActivity.this);
                }


                return super.onSingleTapConfirmed(e);
            }
        });
    }





    private String createMobileMapPackageFilePath(String fileName) {
        return getMMPKFolderPath() + File.separator +  fileName
                + FILE_EXTENSION;
    }

    private String getPointLocalFilePath(){
        return extStorDir.getAbsolutePath() + File.separator + Consts.GRAPPY_FOLDER_NAME + File.separator + mProjectId + File.separator + "layers" + File.separator +  "points.json";
    }

    private String getRasterFolderPath(){
        return extStorDir.getAbsolutePath() + File.separator + Consts.GRAPPY_FOLDER_NAME + File.separator + mProjectId + File.separator + "raster_data";
    }

    private String getUnpackedPath(String fileName) {
        return getMMPKFolderPath() + File.separator + "Unpacked" + File.separator + fileName;
    }

    private String getMMPKFolderPath(){
        return extStorDir.getAbsolutePath() + File.separator +  Consts.GRAPPY_FOLDER_NAME + File.separator + mProjectId + File.separator +"mmpk";
    }

    private void deleteUnpackedMMPKFolderData(){
        File mmpkFolder = new File(getMMPKFolderPath() + File.separator + "Unpacked");
        if (!mmpkFolder.exists())
            return;
        deleteRecursive(mmpkFolder);
    }

    private void deleteMMPKFolderData(){
        deleteRecursive(new File(getMMPKFolderPath()));

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

        //TransitionManager.beginDelayedTransition(findViewById(R.id.mapContainer));
        if (legendDetailsConstraintsLayout.getVisibility() == android.view.View.GONE){
            //final ViewGroup.LayoutParams params = mLayerRecyclerView.getLayoutParams();
/*            mLayerRecyclerView.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));*/

            toggleMenuBtn.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

            legendDetailsConstraintsLayout.setVisibility(android.view.View.VISIBLE);
            LegendLayerDisplayController.INSTANCE.animateOpen(legendDetailsConstraintsLayout);
            //mLayerRecyclerView.requestLayout();


            
        }else{
            LegendLayerDisplayController.INSTANCE.animateClose(legendDetailsConstraintsLayout);
/*            mLayerRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 4));*/
            toggleMenuBtn.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            mLayerRecyclerView.requestLayout();
        }
    }



    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_PHOTO_FOR_LAYER:
                GeoViewController.INSTANCE.setCurrentViewPointForMap(mMapView);
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = ClientPhotoController.INSTANCE.getImageURI();
                    String layerID = FeatureLayerController.INSTANCE.getLayerId();
                    switch (FeatureLayerController.INSTANCE.getShapeType()){

                        case POINT:
                            UserPoints.INSTANCE.getUserPoints().editFeatureImage(this, layerID, uri);
                            break;
                        case POLYLINE:
                            UserPolyline.INSTANCE.getUserPolyline().editFeatureImage(this, layerID, uri);
                            break;
                        case POLYGON:
                            UserPolygon.INSTANCE.getUserPolygon().editFeatureImage(this, layerID, uri);
                            break;
                    }

                }
                break;
            case TAKE_PHOTO_FOR_LAYER:
                if (resultCode == Activity.RESULT_OK){
                    this.progressDialog.show();
                    Uri uri = ClientPhotoController.INSTANCE.getImageURI();
                    ClientPhotoController.INSTANCE.uploadImage(uri,MainActivity.this,MainActivity.this, MainActivity.this, MainActivity.this, mMapView);
                } else {
                    UploadFeatureWithoutImage();

                }
                break;
            case TAKE_PHOTO_FOR_GALLERY:
                if (resultCode == Activity.RESULT_OK){
                    this.progressDialog.show();
                    Uri uri = data.getData();
                    ClientPhotoController.INSTANCE.uploadImage(uri,MainActivity.this,MainActivity.this, MainActivity.this, MainActivity.this, mMapView);

                } else {
                    UploadFeatureWithoutImage();
                }

                break;
        }
    }

    private void UploadFeatureWithoutImage() {
        GeoViewController.INSTANCE.setCurrentViewPointForMap(mMapView);
        Geometry geometry = ClientPhotoController.INSTANCE.getGeometry();
        HashMap attributes = ClientPhotoController.INSTANCE.getAttributes();
        switch (ClientPhotoController.INSTANCE.getType()){
            case POINT:
                this.progressDialog.show();
                UserPoints.INSTANCE.getUserPoints().createFeature(attributes, geometry, null);
                UserPoints.INSTANCE.getUserPoints().uploadJSON(this);
                break;
            case ENVELOPE:
                break;
            case POLYLINE:
                UserPolyline.INSTANCE.getUserPolyline().createFeature(attributes, geometry);
                UserPolyline.INSTANCE.getUserPolyline().uploadJSON(this);
                break;
            case POLYGON:
                UserPolygon.INSTANCE.getUserPolygon().createFeature(attributes,geometry);
                UserPolygon.INSTANCE.getUserPolygon().uploadJSON(this);
                break;
            case MULTIPOINT:
                break;
            case UNKNOWN:
                break;
        }
    }


    @Override
    protected void onPause(){
//        GeoViewController.INSTANCE.setSavedViewPoint(mMapView);
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
//        GeoViewController.INSTANCE.setCurrentViewPointForMap(mMapView);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
//        GeoViewController.INSTANCE.setCurrentViewPointForMap(mMapView);
        dialogLayerSelectionFragment.dismiss();
        showDetailsDialog(layerResult);


    }

    /**
     *
     * @param angle - 0 = north
     */
    private void rotateMap(int angle){
        if (mMapView != null) {
            mMapView.setViewpointRotationAsync(angle);
        }
    }

    private void showDetailsDialog(@NonNull IdentifyLayerResult layerResult) {
        ArrayList<Map<String, String>> displayMap = FeatureLayerController.INSTANCE.layerDetails(layerResult);
        DialogLayerDetailsAdapter dialogLayerDetailsAdapter = new DialogLayerDetailsAdapter(this, displayMap);
        String layerTitle = layerResult.getLayerContent().getName();
        DialogLayerDetailsFragment dialogLayerDetailsFragment = new DialogLayerDetailsFragment(mMapView, this, dialogLayerDetailsAdapter, layerTitle, layerResult, this, this);
        dialogLayerDetailsFragment.show();
    }

    @Override
    public void onLayerClickListener(@NotNull ArrayList<String> layerNames, @NotNull List<IdentifyLayerResult> identifiedLayers) {
//        if (isDeletePointMode){
//            Integer pointHash = FeatureLayerController.INSTANCE.identifyAttForPointDeletion(identifiedLayers);
//            deletePoint(pointHash);
//            return;
//        }
        if (layerNames.size()>1) {
            DialogLayerAdapter dialogLayerAdapter = new DialogLayerAdapter(this, layerNames, this, identifiedLayers);
            dialogLayerSelectionFragment = new DialogLayerSelectionFragment(MainActivity.this, dialogLayerAdapter);
            dialogLayerSelectionFragment.show();
        } else if ((layerNames.size() == 1)){
            showDetailsDialog(identifiedLayers.get(0));
        }
    }

    @Override
    public void onSketchSelectionListener(SketcherEditorTypes sketcher) {
        sketcherSelectionDialogFragment.dismiss();
        if (sketcher == null){
            resetMenuFunctions();
            return;
        }
        if (sketcher == SketcherEditorTypes.TRACKER){
            SketchEditorController.INSTANCE.trackerSketcherMode(bottomSketchBarContainer, MainActivity.this);
        } else{
            sketcherStart(sketcher);
        }

    }

    private void sketcherStart(SketcherEditorTypes sketcher) {
        sketchEditorStartIV.setEnabled(false);
//        deletePointIV.setEnabled(false);
        switch (sketcher){
            case POINT:
            case HYDRANTS:
            case MULTIPOINTS:
                measurementConstraintLayout.setVisibility(View.INVISIBLE);
                break;
            case POLYGON:
                overallSizeHeadlineTV.setText(R.string.area);
                lengthSectionHeadlineTV.setText(R.string.section);
                break;
            case POLYLINE:
            case TRACKER:
                overallSizeHeadlineTV.setText(R.string.length);
                lengthSectionHeadlineTV.setText(R.string.section);
                break;
        }
        SketchEditorController.INSTANCE.startSketching(sketcher, mMapView);
        SketchEditorController.INSTANCE.openSketcherBarContainer(bottomSketchBarContainer);
        mSketcher = SketchEditorController.INSTANCE.getSketchEditor();
        setMeasurementsDisplay(sketcher);
        if (sketcher != SketcherEditorTypes.POINT && sketcher != SketcherEditorTypes.MULTIPOINTS && sketcher != SketcherEditorTypes.HYDRANTS){
            measurementConstraintLayout.setVisibility(View.VISIBLE);
            mSketcher.addGeometryChangedListener(new SketchGeometryChangedListener() {
                @Override
                public void geometryChanged(SketchGeometryChangedEvent sketchGeometryChangedEvent) {
                    setMeasurementsDisplay(sketcher);
                }
            });
        }
    }

    @Override
    public void successListener() {
        initLegendSidebar();
//        List<LegendGroup> legendGroups = LegendLayerDisplayController.INSTANCE.generateLegendSidebar(mMapView);
//        legendAdapter = new LegendSidebarAdapter(this, this, legendGroups, mLayerRecyclerView);
//        mLayerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mLayerRecyclerView.setAdapter(legendAdapter);
//        toggleMenuBtn.setVisibility(View.VISIBLE);
//        RecyclerView.ItemAnimator animator = mLayerRecyclerView.getItemAnimator();
//        if (animator instanceof SimpleItemAnimator){
//            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
//        }
//        initializingProgressDialog.dismiss();
    }

    @Override
    public void onClientPolylineJSONDownloaded(@NotNull JSONObject json) {
        UserPolyline.INSTANCE.setUserPolyline(new ClientFeatureCollectionLayer(json, mMapView, this));
        mMapView.getMap().getOperationalLayers().add(UserPolyline.INSTANCE.getUserPolyline().getLayer());
        ClientLayersController.INSTANCE.fetchClientPoints(this);
//        LegendLayerDisplayController.INSTANCE.fetchMMap(mProjectId, MainActivity.this);
    }

    @Override
    public void onEmptyClientPolylineJSON() {
        ClientLayersController.INSTANCE.fetchClientPoints(this);
//        LegendLayerDisplayController.INSTANCE.fetchMMap(mProjectId, MainActivity.this);
    }

    @Override
    public void onPolylineUploadFinish() {
        progressDialog.dismiss();
        Toast toast = Toast.makeText(this, R.string.polyline_saved, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
        mMapView.setViewpoint(mViewPoint);
        if (SketchEditorController.INSTANCE.isEditMode()){
            SketchEditorController.INSTANCE.setEditMode(false);
            resetMenuFunctions();
        }
    }

    @Override
    public void onEditSelectedListener(@NotNull SketcherEditorTypes type, @NotNull String layerId) {
        Geometry editGeometry = null;
        switch (type){

            case POINT:
                editGeometry = UserPoints.INSTANCE.getUserPoints().getFeatureGeometry(layerId);
                break;
            case POLYLINE:
                editGeometry = UserPolyline.INSTANCE.getUserPolyline().getFeatureGeometry(layerId);
                break;
            case POLYGON:
                editGeometry = UserPolygon.INSTANCE.getUserPolygon().getFeatureGeometry(layerId);
                break;
        }
        if (editGeometry == null){
            return;
        }
        sketcherStart(type);
        sketchEditorStartIV.setEnabled(false);
//        deletePointIV.setEnabled(false);
        switch (type){
            case POINT:
                measurementConstraintLayout.setVisibility(View.INVISIBLE);
                break;
            case POLYGON:
                overallSizeHeadlineTV.setText(R.string.dunam);
                lengthSectionHeadlineTV.setText(R.string.section);
                measurementConstraintLayout.setVisibility(View.VISIBLE);
                break;
            case POLYLINE:
                overallSizeHeadlineTV.setText(R.string.length);
                lengthSectionHeadlineTV.setText(R.string.section);
                measurementConstraintLayout.setVisibility(View.VISIBLE);
                break;
        }
        SketchEditorController.INSTANCE.startSketching(type, mMapView, editGeometry);
        SketchEditorController.INSTANCE.openSketcherBarContainer(bottomSketchBarContainer);
        mSketcher = SketchEditorController.INSTANCE.getSketchEditor();
        if (type != SketcherEditorTypes.POINT){
            setMeasurementsDisplay(type);
            mSketcher.addGeometryChangedListener(sketchGeometryChangedEvent -> setMeasurementsDisplay(type));
        }
    }

    private void setMeasurementsDisplay(@NotNull SketcherEditorTypes type) {
        if (type == SketcherEditorTypes.POINT || type == SketcherEditorTypes.TRACKER) {

            return;
        }
        String unit = mMapView.getSpatialReference().getUnit().getAbbreviation();
        String section = SketchEditorController.INSTANCE.wertexOriginal(unit);
        displaySectionForShapeTV.setText(section);
        switch (type) {
            case POLYLINE:
                String distance = SketchEditorController.INSTANCE.polylineDistance(mMapView);
                calculatePolygonAreaTV.setText(distance);
                break;
            case POLYGON:
                String area = SketchEditorController.INSTANCE.polygonArea(mMapView);
                calculatePolygonAreaTV.setText(area);
                break;
        }
    }

    @Override
    public void onLongPressed(Layer layer) {
        gotoXYBylayer(layer);
    }

    @Override
    public void onClientPointsJSONDownloaded(@NotNull JSONObject json) {
        UserPoints.INSTANCE.setUserPoints(new ClientPointFeatureCollection(this, json));
        mMapView.getMap().getOperationalLayers().add(UserPoints.INSTANCE.getUserPoints().getLayer());
        ClientLayersController.INSTANCE.fetchClientPolygon(this);
    }

    @Override
    public void onEmptyClientPointsJSON() {
        ClientLayersController.INSTANCE.fetchClientPolygon(this);
    }

    @Override
    public void onPointsUploadFinished() {
        progressDialog.dismiss();
    }

    @Override
    public void jumpToSearchResultFeature(Envelope envelope) {
        if (envelope != null){
            GeoViewController.INSTANCE.moveToLocationByGeometry(envelope, mMapView);
        }
        searchFeatureIV.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onClientPolygonJSONDownloaded(@NotNull JSONObject json) {
        UserPolygon.INSTANCE.setUserPolygon(new ClientPolygonFeatureCollection(this, json));
        mMapView.getMap().getOperationalLayers().add(UserPolygon.INSTANCE.getUserPolygon().getLayer());
        initLegendSidebar();
//        LegendLayerDisplayController.INSTANCE.fetchMMap(mProjectId, MainActivity.this);
        initializingProgressDialog.dismiss();
    }

    @Override
    public void onEmptyClientPolygon() {
//        LegendLayerDisplayController.INSTANCE.fetchMMap(mProjectId, MainActivity.this);
        initLegendSidebar();
        initializingProgressDialog.dismiss();
    }

    @Override
    public void onClientPolygonUploaded() {
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.closing_app)).setMessage(R.string.closing_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onFinishedDownloadListener(@NotNull ProgressDialog progressDialog) {
        progressDialog.dismiss();
        setRaster(getRasterFolderPath());
    }


    @Override
    public void OnShapeChangeTrackerListener(@NotNull String totalArea, @NotNull String vertex) {
        runOnUiThread(() ->
                calculatePolygonAreaTV.setText(totalArea));
        runOnUiThread(()->
                displaySectionForShapeTV.setText(vertex));
    }
}
