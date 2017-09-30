package org.lovebing.reactnative.baidumap;

import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONObject;

/**
 * Created by lovebing on Sept 28, 2016.
 */
public class MarkerUtil {

    private static BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);


    public static void updateMaker(Marker maker, ReadableMap option) {
        LatLng position = getLatLngFromOption(option);
        maker.setPosition(position);
        maker.setTitle(option.getString("title"));
    }

    private static GenericDraweeHierarchy createDraweeHierarchy() {
        return new GenericDraweeHierarchyBuilder(BaiduMapPackage.mContext.getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setFadeDuration(0)
                .build();
    }

    private static int getDrawableResourceByName(String name) {
        int source = BaiduMapPackage.mContext.getResources().getIdentifier(name, "drawable", BaiduMapPackage.mContext.getPackageName());
        return BaiduMapPackage.mContext.getResources().getIdentifier(name, "drawable", BaiduMapPackage.mContext.getPackageName());
    }

    private static BitmapDescriptor getBitmapDescriptorByName(String name) {
        return BitmapDescriptorFactory.fromResource(getDrawableResourceByName(name));
    }

    public static Marker addMarker(MapView mapView, ReadableMap option) throws Exception {
        Marker marker = null;
        if (option == null) {
            throw new Exception("marker annotation must not be null");
        }
        LatLng position = getLatLngFromOption(option);

        final MarkerOptions overlayOptions = new MarkerOptions()
                .icon(bitmap)
                .position(position);

        if (option.hasKey("draggable")) {
            boolean draggable = option.getBoolean("draggable");
            overlayOptions.draggable(draggable);
        }

        if (option.hasKey("title")) {
            overlayOptions.title(option.getString("title"));
        }
        if (option.hasKey("data")) {
            ReadableMap datamap = option.getMap("data");
            Bundle mybundle = new Bundle();
            JSONObject jsono = RNUtils.readableMapToJson(datamap);
            mybundle.putString("markerData", jsono.toString());
            overlayOptions.extraInfo(mybundle);
        }
        if (option.hasKey("animateType")) {
            String type = option.getString("animateType");
            if (type.equals("grow")) {
                overlayOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
            } else if (type.equals("drop")) {
                overlayOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
            }
        }

        if (option.hasKey("image")) {
            String imgUri = option.getMap("image").getString("uri");
            if (imgUri != null && imgUri.length() > 0) {
                if (imgUri.startsWith("http://") || imgUri.startsWith("https://")) {
                    ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUri)).build();
                    ImagePipeline imagePipeline = Fresco.getImagePipeline();
                    final DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, BaiduMapPackage.mContext);
                    final BitmapDescriptor iconBitmapDescriptor;
                    final DraweeHolder mLogoHolder;
                    mLogoHolder = DraweeHolder.create(createDraweeHierarchy(), null);
                    mLogoHolder.onAttach();
                    final Marker finalMarker = marker;
                    ControllerListener<ImageInfo> mLogoControllerListener =
                            new BaseControllerListener<ImageInfo>() {
                                @Override
                                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                                    CloseableReference<CloseableImage> imageReference = null;
                                    try {
                                        imageReference = dataSource.getResult();
                                        if (imageReference != null) {
                                            CloseableImage image = imageReference.get();
                                            if (image != null && image instanceof CloseableStaticBitmap) {
                                                CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
                                                Bitmap bitmap = closeableStaticBitmap.getUnderlyingBitmap();
                                                if (bitmap != null) {
                                                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                    if (finalMarker == null) {
                                                        overlayOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                                    } else {
                                                        finalMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                                                    }
                                                }
                                            }
                                        }
                                    } finally {
                                        dataSource.close();
                                        if (imageReference != null) {
                                            CloseableReference.closeSafely(imageReference);
                                        }
                                    }

                                }
                            };
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imageRequest)
                            .setControllerListener(mLogoControllerListener)
                            .setOldController(mLogoHolder.getController())
                            .build();
                    mLogoHolder.setController(controller);
                } else if (imgUri.startsWith("file://")) {
                    imgUri = imgUri.substring(imgUri.lastIndexOf("/") + 1);
                    imgUri = imgUri.substring(0, imgUri.lastIndexOf("."));
                    overlayOptions.icon(getBitmapDescriptorByName(imgUri));
                } else {
                    overlayOptions.icon(getBitmapDescriptorByName(imgUri));
                }
            }
        }

        marker = (Marker) mapView.getMap().addOverlay(overlayOptions);
        return marker;
    }


    private static LatLng getLatLngFromOption(ReadableMap option) {
        double latitude = option.getDouble("latitude");
        double longitude = option.getDouble("longitude");
        return new LatLng(latitude, longitude);

    }
}
