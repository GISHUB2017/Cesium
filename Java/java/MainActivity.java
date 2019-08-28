package com.info.osm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {
    private String url1 = "http://dzmap.infoearth.com:8088/iTelluro.Server.TianDiTu/Service/DOM/dom.ashx";
    private static String[] TIANDITU_URL = new String[]
            {
                    "http://t0.tianditu.com/DataServer",
                    "http://t1.tianditu.com/DataServer",
                    "http://t2.tianditu.com/DataServer",
                    "http://t3.tianditu.com/DataServer",
                    "http://t4.tianditu.com/DataServer",
                    "http://t5.tianditu.com/DataServer",
                    "http://t6.tianditu.com/DataServer",
                    "http://t7.tianditu.com/DataServer"
            };


    private GeoPoint DronePoint = new GeoPoint(30.4819, 114.4010);
    MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView)findViewById(R.id.map);
        mapView.getController().setZoom(12);
        double la  = mapView.getMapCenter().getLatitude();
        double lo  = mapView.getMapCenter().getLongitude();
        double b = la + lo;
//        mapView.getController().animateTo(DronePoint);
//        InfoTileSource dataSource = new InfoTileSource("IMG", 6,10,512,"",new String[]{url1});
//        TileSystem.setTileSize(512);
//        MapTileProviderBasic tileProvider = new MapTileProviderBasic(this,dataSource);
//        TestOverlay testOverlay = new TestOverlay(tileProvider,this);
//        mapView.getOverlayManager().add(testOverlay);
//        tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(
//                mapView));

    }
}
