package com.info.osm;

import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IProjection;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;

/**
 * Created by IceWang on 2017/3/31.
 */
public class MyProjection implements IProjection {
    private MapView mapView;
    private int mZoomLevelProjection;

    private int mMapViewWidth;
    private int mMapViewHeight;

    protected int mOffsetX;
    protected int mOffsetY;

    private  Rect mScreenRectProjection;

    public MyProjection(MapView mapView) {
        this.mapView = mapView;
        mZoomLevelProjection = mapView.getZoomLevel(false);
        System.out.println("ice mZoomLevelProjection : " + mZoomLevelProjection);
        mMapViewWidth = mapView.getWidth();
        mMapViewHeight = mapView.getHeight();
        mOffsetX = -mapView.getScrollX();
        mOffsetY = -mapView.getScrollY();

        mScreenRectProjection = mapView.getScreenRect(null);
    }

    @Override
    public Point toPixels(IGeoPoint in, Point out) {
        int tileSizePx = TileSystem.getTileSize();
        int x = (int) Math.floor(((Math.abs((-180.0 - in.getLongitude())) % 360.0) / tileSizePx));
        int y = (int) Math.floor(((Math.abs((-90.0 - in.getLatitude())) % 180.0) / tileSizePx));
        out = new Point(x, y);
        return out;
    }

    @Override
    public IGeoPoint fromPixels(int x, int y) {
        int tileSizePx = TileSystem.getTileSize();
        double lon = (Math.abs(x * tileSizePx - 180)) % 360;
        double lat = (Math.abs(y * tileSizePx - 90)) % 180;
        return new GeoPoint(lat, lon);
    }

    @Override
    public float metersToEquatorPixels(float meters) {
        return meters / (float) TileSystem.GroundResolution(0, mZoomLevelProjection);
    }

    @Override
    public IGeoPoint getNorthEast() {
        return fromPixels(mMapViewWidth, 0);
    }

    @Override
    public IGeoPoint getSouthWest() {
        return fromPixels(0, mMapViewHeight);
    }

    public Rect getScreenRect() {
        return mScreenRectProjection;
    }

    public Point toMercatorPixels(int x, int y, Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        out.set(x, y);
        out.offset(-mOffsetX, -mOffsetY);
        return out;
    }

    public int getZoomLevel() {
        return mZoomLevelProjection;
    }

    public Point toPixelsFromMercator(int x, int y, Point reuse) {
        final Point out = reuse != null ? reuse : new Point();
        out.set(x, y);
        out.offset(mOffsetX, mOffsetY);
        return out;
    }
}
