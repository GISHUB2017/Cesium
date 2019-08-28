package com.helloworld;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileCache;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {
    private String url1 = "http://t0.tianditu.com/DataServer";

    MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.map);
        InfoTileSource dataSource = new InfoTileSource("天地图影像S", 0,13,512,".jpg",new String[]{url1});
        //iTelluroTileProvider itelluro = new iTelluroTileProvider(dataSource);

        MapTileProviderBase itelluro = new MapTileProviderBase(dataSource){

            private ITileSource tileSource;
            @Override
            public void setTileSource(ITileSource pTileSource) {
                super.setTileSource(pTileSource);
                tileSource = pTileSource;
            }

            @Override
            public int getMaximumZoomLevel() {
                return tileSource.getMaximumZoomLevel();
            }

            @Override
            public int getMinimumZoomLevel() {
                return tileSource.getMinimumZoomLevel();
            }

            //拆分
            @Override
            public void detach() {

            }

            @Override
            public Drawable getMapTile(MapTile pTile) {
                return null;
            }

            @Override
            public ITileSource getTileSource() {
                return tileSource;
            }

            @Override
            public MapTileCache createTileCache() {
                return super.createTileCache();
            }
        };
        itelluro.setTileSource(dataSource);

        iTelluroTileOverlay  layer = new iTelluroTileOverlay(itelluro,this);
        mapView.getOverlays().add(layer);
    }
}
