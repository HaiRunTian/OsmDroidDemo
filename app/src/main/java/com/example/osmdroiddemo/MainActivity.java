package com.example.osmdroiddemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MyLocationNewOverlay myLocationNewOverlay;
    //影像地图 _W是墨卡托投影  _c是国家2000的坐标系


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Context ctx = getApplicationContext();
//        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView);
        mMapView.setTileSource(GoogleTileSource.GoogleHybrid);
        mMapView.setMaxZoomLevel(22.0);
        mMapView.setMinZoomLevel(1.0);
        IMapController controller = mMapView.getController();
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setTilesScaledToDpi(true);
        controller.setZoom(12.0);
        controller.setCenter(new GeoPoint(23.156165, 113.413013));

        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMapView);
        myLocationNewOverlay.enableMyLocation();
        //定位的图标和方向旋转
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_menu_mylocation);
        myLocationNewOverlay.setDirectionArrow(bitmap, bitmap);
        mMapView.getOverlays().add(myLocationNewOverlay);
        //开启跟随模式
        myLocationNewOverlay.enableFollowLocation();

        //Mini map
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        MinimapOverlay mMinimapOverlay = new MinimapOverlay(this, mMapView.getTileRequestCompleteHandler());
        mMinimapOverlay.setWidth(defaultDisplay.getWidth() / 5 );
        mMinimapOverlay.setHeight(defaultDisplay.getHeight() / 5);
        mMapView.getOverlays().add(mMinimapOverlay);

        //比例尺显示
        //map scale
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        //设置比例尺显示的位置
        mScaleBarOverlay.setScaleBarOffset(defaultDisplay.getWidth() / 2, 10);
        mMapView.getOverlays().add(mScaleBarOverlay);

        //地图旋转
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(mMapView);
        //true 旋转  false 地图不旋转
        rotationGestureOverlay.setEnabled(true);
        mMapView.getOverlays().add(rotationGestureOverlay);


        //地图点击事件
        final  MapEventsReceiver receiver = new MapEventsReceiver(){
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Log.i("tag","点击地图" + p.toString());
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Log.i("tag","长按地图" + p.toString());
                return false;
            }
        };

        mMapView.getOverlays().add(new MapEventsOverlay(receiver));

        //指南针显示
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO){
            CompassOverlay compassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this),mMapView);
            compassOverlay.enableCompass();
            mMapView.getOverlays().add(compassOverlay);
        }

        //经纬度网格显示
        LatLonGridlineOverlay2 gridlineOverlay2 = new LatLonGridlineOverlay2();
        gridlineOverlay2.setBackgroundColor(Color.BLACK);
        gridlineOverlay2.setFontColor(Color.RED);
        gridlineOverlay2.setLineColor(Color.RED);
        gridlineOverlay2.setFontSizeDp((short)14);
        mMapView.getOverlayManager().add(gridlineOverlay2);

        requestPermissionsIfNecessary(new String[]{
                // if you need to show the current location, uncomment the line below
                // Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}
