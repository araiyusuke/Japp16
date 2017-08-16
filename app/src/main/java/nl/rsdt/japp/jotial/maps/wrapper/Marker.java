package nl.rsdt.japp.jotial.maps.wrapper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.JsonReader;
import android.util.Pair;
import android.util.StringBuilderPrinter;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.*;
import org.osmdroid.views.overlay.Polyline;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import nl.rsdt.japp.application.Japp;

/**
 * Created by mattijn on 08/08/17.
 */

public class Marker {
    public static final int GOOGLEMARKER = 0;
    public static final int OSMMARKER = 1;
    private static AllOnClickListener allOnClickLister = null;
    private final int markerType;
    private final com.google.android.gms.maps.model.Marker googleMarker;
    private final org.osmdroid.views.overlay.Marker osmMarker;
    private final MapView osmMap;
    private OnClickListener onClickListener = null;

    public Marker(com.google.android.gms.maps.model.Marker marker) {
        this.googleMarker = marker;
        markerType = GOOGLEMARKER;
        osmMarker = null;
        osmMap = null;
    }

    public Marker(Pair<MarkerOptions,Bitmap> markerOptionsPair, MapView osmMap) {
        googleMarker = null;
        markerType = OSMMARKER;
        this.osmMap = osmMap;
        osmMarker = new org.osmdroid.views.overlay.Marker(osmMap);
        MarkerOptions markerOptions = markerOptionsPair.first;
        this.setIcon(markerOptionsPair.second);
        this.setPosition(markerOptions.getPosition());
        if (markerOptions.getTitle() == null){
            markerOptions.title("");
        }
        try {
            JSONObject mainObject = new JSONObject(markerOptions.getTitle());
            String type = mainObject.getString("type");
            JSONObject properties = mainObject.getJSONObject("properties");
            StringBuilder buff = new StringBuilder();
            if (type.equals("VOS")) {

                buff.append(properties.getString("extra")).append("\n");
                buff.append(properties.getString("time")).append("\n");
                buff.append(properties.getString("note")).append("\n");
                buff.append(properties.getString("team")).append("\n");
            }else if(type.equals("HUNTER")){
                buff.append(properties.getString("hunter")).append("\n");
                buff.append(properties.getString("time")).append("\n");
            }else {
                buff.append(markerOptions.getTitle());
            }
            osmMarker.setTitle(buff.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            osmMarker.setTitle(markerOptions.getTitle());
        }

        osmMarker.setOnMarkerClickListener(new org.osmdroid.views.overlay.Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(org.osmdroid.views.overlay.Marker marker, MapView mapView) {
                if (marker == osmMarker) {
                    onClick();
                }
                return false;
            }
        });
        osmMap.getOverlays().add(osmMarker);
        osmMap.invalidate();
    }
    public static void setAllOnClickLister(Marker.AllOnClickListener onClickListener){
        allOnClickLister = onClickListener;
    }

    private boolean onClick(){
        if (allOnClickLister != null){
            if (!allOnClickLister.OnClick(this)){
                if (this.onClickListener == null) {
                    showInfoWindow();
                    return false;
                }else{
                    return this.onClickListener.OnClick(this);
                }
            }
        }else {
            if (this.onClickListener == null) {
                showInfoWindow();
                return false;
            }else{
                return this.onClickListener.OnClick(this);
            }
        }
        return false;
    }

    public void showInfoWindow(){
        switch (markerType){
            case GOOGLEMARKER:
                googleMarker.showInfoWindow();
                break;
            case OSMMARKER:
                osmMarker.showInfoWindow();
                break;
            default:
                break;
        }
    }
    public void remove() {
        switch (markerType){
            case GOOGLEMARKER:
                this.googleMarker.remove();
                break;
            case OSMMARKER:
                osmMap.getOverlays().remove(osmMarker);
                osmMap.invalidate();
                break;
            default:
                break;
        }
    }

    public String getTitle() {
        switch (markerType){
            case GOOGLEMARKER:
                return this.googleMarker.getTitle();
            case OSMMARKER:
                return osmMarker.getTitle();
            default:
                return "";
        }
    }

    public LatLng getPosition() {
        switch (markerType) {
            case GOOGLEMARKER:
                return this.googleMarker.getPosition();
            case OSMMARKER:
                return new LatLng(osmMarker.getPosition().getLatitude(),osmMarker.getPosition().getLongitude());
            default:
                return null;
        }
    }

    public void setPosition(LatLng latLng) {
        switch (markerType){
            case GOOGLEMARKER:
                this.googleMarker.setPosition(latLng);
                break;
            case OSMMARKER:
                this.osmMarker.setPosition(new GeoPoint(latLng.latitude,latLng.longitude));
                osmMap.invalidate();
                break;
            default:
                break;
        }
    }

    public void setOnClickListener(Marker.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
    public void setIcon(int drawableHunt) {
        this.setIcon(BitmapFactory.decodeResource(Japp.getAppResources(),drawableHunt));

    }
    private void setIcon(Bitmap bitmap) {
        switch (markerType){
            case GOOGLEMARKER:
                this.googleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                break;
            case OSMMARKER:
                if (bitmap != null) {
                    Drawable d = new BitmapDrawable(Japp.getAppResources(), bitmap);
                    this.osmMarker.setIcon(d);
                    osmMap.invalidate();
                }
                break;
            default:
                break;
        }

    }

    public void setTitle(String title) {
        switch (markerType){
            case GOOGLEMARKER:
                googleMarker.setTitle(title);
                break;
            case OSMMARKER:
                osmMarker.setTitle(title);
            default:
                break;
        }
    }

    public boolean isVisible() {
        switch (markerType) {
            case GOOGLEMARKER:
                return this.googleMarker.isVisible();
            case OSMMARKER:
                return true; //// TODO: 09/08/17 is dit hetzelfde?
            default:
                return false;
        }
    }

    public void setVisible(boolean visible) {
        switch (markerType){
            case GOOGLEMARKER:
                googleMarker.setVisible(visible);
                break;
            case OSMMARKER:
                //osmMarker.setEnabled(visible); //// TODO: 09/08/17 is dit hetzelfde?
        }
    }

    public void setRotation(float rotation) {
        switch (markerType) {
            case GOOGLEMARKER:
                googleMarker.setRotation(rotation);
                break;
            case OSMMARKER:
                osmMarker.setRotation(rotation);
                osmMap.invalidate();
                break;
            default:
                break;
        }
    }

    public String getId() {
        switch (markerType) {
            case GOOGLEMARKER:
                return googleMarker.getId();
            case OSMMARKER:
                return "1";// // TODO: 09/08/17 implement this
            default:
                return null;
        }
    }

    public org.osmdroid.views.overlay.Marker getOSMMarker() {
        return osmMarker;
    }

    public interface AllOnClickListener {
        public boolean OnClick(Marker m);
    }
    public interface OnClickListener{
        public boolean OnClick(Marker m);
    }
}
