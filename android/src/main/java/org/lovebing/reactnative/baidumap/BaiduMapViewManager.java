package org.lovebing.reactnative.baidumap;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lovebing on 12/20/2015.
 */
public class BaiduMapViewManager extends ViewGroupManager<MapView> {

    private static final String REACT_CLASS = "RCTBaiduMapView";

    private ThemedReactContext mReactContext;

    private ReadableArray childrenPoints;
    private HashMap<String, Marker> mMarkerMap = new HashMap<>();
    private HashMap<String, List<Marker>> mMarkersMap = new HashMap<>();
    private ReactMapView mMapView;
    private TextView mMarkerText;
    private boolean isMapLoaded;

    public String getName() {
        return REACT_CLASS;
    }


    public void initSDK(Context context) {
        SDKInitializer.initialize(context);
    }

    public MapView createViewInstance(ThemedReactContext context) {
        mMarkerMap = new HashMap<>();
        mMarkersMap = new HashMap<>();
        mReactContext = context;
        MapView mapView = new MapView(context);
        mMapView = new ReactMapView(mapView);
        setListeners(mapView);

        return mapView;
    }

    @Override
    public void addView(MapView parent, View child, int index) {
        if (childrenPoints != null) {
            Point point = new Point();
            ReadableArray item = childrenPoints.getArray(index);
            if (item != null) {
                point.set(item.getInt(0), item.getInt(1));
                MapViewLayoutParams mapViewLayoutParams = new MapViewLayoutParams
                        .Builder()
                        .layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode)
                        .point(point)
                        .build();
                parent.addView(child, mapViewLayoutParams);
            }
        }

    }

    @ReactProp(name = "zoomControlsVisible", defaultBoolean = false)
    public void setZoomControlsVisible(MapView mapView, boolean zoomControlsVisible) {
        mapView.showZoomControls(zoomControlsVisible);
    }

    @ReactProp(name = "trafficEnabled")
    public void setTrafficEnabled(MapView mapView, boolean trafficEnabled) {
        mapView.getMap().setTrafficEnabled(trafficEnabled);
    }

    @ReactProp(name = "showsCompass", defaultBoolean = false)
    public void setShowsCompass(MapView mapView, Boolean show) {
        mapView.getMap().getUiSettings().setCompassEnabled(show);
    }

    @ReactProp(name = "showsUserLocation", defaultBoolean = false)
    public void setShowsUserLocation(MapView mapView, Boolean show) {
        mMapView.setShowsUserLocation(show);
    }

    @ReactProp(name = "userLocationViewParams")
    public void setUserLocationViewParams(MapView mapView, @Nullable ReadableMap params) {
        ReactMapMyLocationConfiguration configuration = new ReactMapMyLocationConfiguration(mReactContext);
        configuration.buildConfiguration(params);
        this.mMapView.setConfiguration(configuration);
    }

    @ReactProp(name = "zoomEnabled", defaultBoolean = true)
    public void setZoomEnabled(MapView mapView, Boolean enable) {
        mapView.getMap().getUiSettings().setZoomGesturesEnabled(enable);
    }

    @ReactProp(name = "baiduHeatMapEnabled")
    public void setBaiduHeatMapEnabled(MapView mapView, boolean baiduHeatMapEnabled) {
        mapView.getMap().setBaiduHeatMapEnabled(baiduHeatMapEnabled);
    }

    @ReactProp(name = "mapType")
    public void setMapType(MapView mapView, int mapType) {
        mapView.getMap().setMapType(mapType);
    }

    @ReactProp(name = "zoom")
    public void setZoom(MapView mapView, float zoom) {
        MapStatus mapStatus = new MapStatus.Builder().zoom(zoom).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mapView.getMap().setMapStatus(mapStatusUpdate);
    }

    @ReactProp(name = "center")
    public void setCenter(MapView mapView, ReadableMap position) {
        if (position != null) {
            double latitude = position.getDouble("latitude");
            double longitude = position.getDouble("longitude");
            LatLng point = new LatLng(latitude, longitude);
            MapStatus mapStatus = new MapStatus.Builder()
                    .target(point)
                    .build();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
            mapView.getMap().setMapStatus(mapStatusUpdate);
        }
    }

    @ReactProp(name = "marker")
    public void setMarker(MapView mapView, ReadableMap annotation) throws Exception {
        if (annotation == null) {
            Log.e(REACT_CLASS, "Error: no annotation");
            return;
        }

        List<ReactMapMarker> markers = new ArrayList<ReactMapMarker>();

        ReactMapMarker marker = new ReactMapMarker(this.mReactContext);
        marker.buildMarker(annotation);
        markers.add(marker);

        mMapView.setMarker(markers);

        if (this.isMapLoaded && this.mMapView.isAutoZoomToSpan()) {
            this.mMapView.zoomToSpan();
        }
    }

    @ReactProp(name = "markers")
    public void setMarkers(MapView mapView, ReadableArray options) throws Exception {
        if (options == null || options.size() == 0) {
            Log.e(REACT_CLASS, "Error: no annotation");
            return;
        }

        List<ReactMapMarker> markers = new ArrayList<ReactMapMarker>();
        int size = options.size();
        for (int i = 0; i < size; i++) {
            ReadableMap annotation = options.getMap(i);
            ReactMapMarker marker = new ReactMapMarker(this.mReactContext);
            marker.buildMarker(annotation);
            markers.add(marker);
        }

        mMapView.setMarker(markers);

        if (this.isMapLoaded && this.mMapView.isAutoZoomToSpan()) {
            this.mMapView.zoomToSpan();
        }
    }

    @ReactProp(name = "childrenPoints")
    public void setChildrenPoints(MapView mapView, ReadableArray childrenPoints) {
        this.childrenPoints = childrenPoints;
    }

    public void sendStatusChange(MapView view, ThemedReactContext themedReactContext, WritableMap data) {
        WritableMap event;
        if (data == null) {
            event = Arguments.createMap();
        } else {
            event = data;
        }
        event.putString("message", "MyMessage");
        event.putMap("coords", getScreenLocation(view));

        sendEvent(view, "onMapStatusChangeFinish", event);
    }

    public WritableMap getScreenLocation(MapView view) {
        MapStatus status = mMapView.getMap().getMapStatus();
        WritableMap map = Arguments.createMap();


        WritableMap northeastmap = Arguments.createMap();
        northeastmap.putDouble("longitude", status.bound.northeast.longitude);
        northeastmap.putDouble("latitude", status.bound.northeast.latitude);

        WritableMap northwestmap = Arguments.createMap();
        northwestmap.putDouble("longitude", status.bound.southwest.longitude);
        northwestmap.putDouble("latitude", status.bound.northeast.latitude);

        WritableMap southwestmap = Arguments.createMap();
        southwestmap.putDouble("longitude", status.bound.southwest.longitude);
        southwestmap.putDouble("latitude", status.bound.southwest.latitude);


        WritableMap southeastmap = Arguments.createMap();
        southeastmap.putDouble("longitude", status.bound.northeast.longitude);
        southeastmap.putDouble("latitude", status.bound.southwest.latitude);

        //获取中心位置
        LatLng mCenterLatLng = status.target;
        WritableMap centermap = Arguments.createMap();
        centermap.putDouble("longitude", mCenterLatLng.longitude);
        centermap.putDouble("latitude", mCenterLatLng.latitude);

        map.putMap("rt", northeastmap);
        map.putMap("lt", northwestmap);
        map.putMap("lb", southwestmap);
        map.putMap("rb", southeastmap);
        map.putMap("center", centermap);
        return map;
    }

    /**
     * @param mapView
     */
    private void setListeners(final MapView mapView) {
        BaiduMap map = mapView.getMap();

        if (mMarkerText == null) {
            mMarkerText = new TextView(mapView.getContext());
            mMarkerText.setBackgroundResource(R.drawable.popup);
            mMarkerText.setPadding(32, 32, 32, 32);
        }
        map.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            private WritableMap getEventParams(MapStatus mapStatus) {
                WritableMap writableMap = Arguments.createMap();
                WritableMap target = Arguments.createMap();
                target.putDouble("latitude", mapStatus.target.latitude);
                target.putDouble("longitude", mapStatus.target.longitude);
                writableMap.putMap("target", target);
                writableMap.putDouble("zoom", mapStatus.zoom);
                writableMap.putDouble("overlook", mapStatus.overlook);
                return writableMap;
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                sendEvent(mapView, "onMapStatusChangeStart", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                sendEvent(mapView, "onMapStatusChange", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                if (mMarkerText.getVisibility() != View.GONE) {
                    mMarkerText.setVisibility(View.GONE);
                }
                sendEvent(mapView, "onMapStatusChangeFinish", getEventParams(mapStatus));
            }
        });

        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                sendEvent(mapView, "onMapLoaded", null);
                BaiduMapViewManager.this.isMapLoaded = true;
                mMapView.onMapLoaded();
                sendStatusChange(mapView, mReactContext, null);
            }
        });

        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapClick", writableMap);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("name", mapPoi.getName());
                writableMap.putString("uid", mapPoi.getUid());
                writableMap.putDouble("latitude", mapPoi.getPosition().latitude);
                writableMap.putDouble("longitude", mapPoi.getPosition().longitude);
                sendEvent(mapView, "onMapPoiClick", writableMap);
                return true;
            }
        });
        map.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng latLng) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapDoubleClick", writableMap);
            }
        });

        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().length() > 0) {
                    mMarkerText.setText(marker.getTitle());
                    InfoWindow infoWindow = new InfoWindow(mMarkerText, marker.getPosition(), -80);
                    mMarkerText.setVisibility(View.GONE);
                    mapView.getMap().showInfoWindow(infoWindow);
                } else {
                    mapView.getMap().hideInfoWindow();
                }
                WritableMap writableMap = Arguments.createMap();
                WritableMap position = Arguments.createMap();
                position.putDouble("latitude", marker.getPosition().latitude);
                position.putDouble("longitude", marker.getPosition().longitude);
                writableMap.putMap("position", position);
                writableMap.putString("title", marker.getTitle());
                sendEvent(mapView, "onMarkerClick", writableMap);
                return true;
            }
        });

    }

    /**
     * @param eventName
     * @param params
     */
    private void sendEvent(MapView mapView, String eventName, @Nullable WritableMap params) {
        WritableMap event = Arguments.createMap();
        event.putMap("params", params);
        event.putString("type", eventName);
        mReactContext
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(mapView.getId(),
                        "topChange",
                        event);
    }
}
