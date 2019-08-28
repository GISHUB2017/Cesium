package com.helloworld;

import android.graphics.drawable.Drawable;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.TileSystem;

import java.io.InputStream;

/**
 * Created by IceWang on 2017/3/27.
 */
public class InfoTileSource extends OnlineTileSourceBase{
    public InfoTileSource(
            String key,
            int aZoomMinLevel,
            int aZoomMaxLevel,
            int zeroSize,
            String fileType,
            String[] aBaseUrl)
    {
        super(key, aZoomMinLevel, aZoomMaxLevel, zeroSize/512, fileType, aBaseUrl);
        TileSystem.setTileSize(getTileSizePixels());
    }

    @Override
    protected String getBaseUrl() {
        return super.getBaseUrl();
    }

    //iTelluro.Server的切片请求地址：
    // http://dzmap.infoearth.com:8088/iTelluro.Server.TianDiTu/Service/DOM/dom.ashx?T=天地图&L=0&X=5&Y=2
    @Override
    public String getTileURLString(MapTile aTile) {
        System.out.println("x : " + aTile.getX());
        System.out.println("y : " + aTile.getY());
        System.out.println("l : " + aTile.getZoomLevel());
        return getBaseUrl() + "?T=" + super.name()+"&L=" + aTile.getZoomLevel() + "&X=" + aTile.getX() + "&Y=" + aTile.getY();
    }

    @Override
    public int getMaximumZoomLevel() {
        return super.getMaximumZoomLevel();
    }

    @Override
    public int getMinimumZoomLevel() {
        return super.getMinimumZoomLevel();
    }

    @Override
    public int getTileSizePixels() {
        return super.getTileSizePixels();
    }
    @Override
    public Drawable getDrawable(String aFilePath) {
        return super.getDrawable(aFilePath);
    }

    @Override
    public String getTileRelativeFilenameString(MapTile tile) {
        return super.getTileRelativeFilenameString(tile);
    }

    @Override
    public Drawable getDrawable(InputStream aFileInputStream) throws LowMemoryException {
        return super.getDrawable(aFileInputStream);
    }
}
