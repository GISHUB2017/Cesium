package com.info.osm;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

/**
 * Created by IceWang on 2017/3/27.
 */
public class InfoTileSource extends OnlineTileSourceBase{
    /**
     * Constructor
     *
     * @param aName                a human-friendly name for this tile source
     * @param aZoomMinLevel        the minimum zoom level this tile source can provide
     * @param aZoomMaxLevel        the maximum zoom level this tile source can provide
     * @param aTileSizePixels      the tile size in pixels this tile source provides
     * @param aImageFilenameEnding the file name extension used when constructing the filename
     * @param aBaseUrl             the base url(s) of the tile server used when constructing the url to download the tiles
     */
    public InfoTileSource(String aName, int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels, String aImageFilenameEnding, String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, aImageFilenameEnding, aBaseUrl);
    }

    @Override
    public String getTileURLString(MapTile aTile) {
        String result = "";
        switch (name().toUpperCase()) {
            case "VEC":
                result = "vec_w";
                break;
            case "IMG":
                result = "img_w";
                break;
            case "TER":
                result = "ter_w";
                break;
            case "CVA":
                result = "cva_w";
                break;
            case "CIA":
                result = "cia_w";
                break;
            case "CTA":
                result = "cta_w";
                break;
        }
        // http://dzmap.infoearth.com:8088/iTelluro.Server.TianDiTu/Service/DOM/dom.ashx?T=天地图影像S&L=2&X=23&Y=11
        System.out.println("ice aTile : " + getBaseUrl() + "?T=" + "天地图影像S" + "&L=" + aTile.getZoomLevel() + "&X=" + aTile.getX() + "&Y=" + aTile.getY());
//        System.out.println("ice aTile : " + getBaseUrl() + "?T=" + result + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&l=" + aTile.getZoomLevel());
//        return getBaseUrl() + "?T=" + result + "&x=" + aTile.getX() + "&y=" + aTile.getY() + "&l=" + aTile.getZoomLevel();
        return getBaseUrl() + "?T=" + "天地图影像S" + "&L=" + aTile.getZoomLevel() + "&X=" + aTile.getX() + "&Y=" + aTile.getY();
    }


}
