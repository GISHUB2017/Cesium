package com.info.osm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

/**
 * Created by Administrator on 2017/3/30.
 */

public class iTelluroTileOverlay extends Overlay {
    /** Current tile source */
    protected MapTileProviderBase mTileProvider;//地图瓦块数据源

    /* to avoid allocations during draw 瓦块绘制区域*/
    protected final Paint mPaint = new Paint();
    private final Rect mTileRect = new Rect();
    private final Point mTilePos = new Point();

    //构造函数
    public iTelluroTileOverlay(Context ctx, MapTileProviderBase mTileProvider) {
        super(ctx);
        this.mTileProvider = mTileProvider;
    }

    public iTelluroTileOverlay(ResourceProxy pResourceProxy, MapTileProviderBase mTileProvider) {
        super(pResourceProxy);
        this.mTileProvider = mTileProvider;
    }

    public iTelluroTileOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
        super(aContext);
        if (aTileProvider == null) {
            //必须将有效的瓦片提供程序传递给平铺覆盖
            throw new IllegalArgumentException("You must pass a valid tile provider to the tiles overlay.");
        }
        this.mTileProvider = aTileProvider;
    }


    //设置透明色
    public void setAlpha(final int a) {
        this.mPaint.setAlpha(a);
    }

    //获取图层显示最低级数
    public int getMinimumZoomLevel() {
        return mTileProvider.getMinimumZoomLevel();
    }

    //获取图层显示最高级数
    public int getMaximumZoomLevel() {
        return mTileProvider.getMaximumZoomLevel();
    }

    /**
     * Whether to use the network connection if it's available.
     * 是否使用网络连接
     */
    public boolean useDataConnection() {
        return mTileProvider.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *设置是否使用网络连接，如果它是可用的。
     * @param aMode
     *            if true use the network connection if it's available. if false
     *            don't use the network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mTileProvider.setUseDataConnection(aMode);
    }

    @Override
    public void draw(final Canvas c, final MapView osmv, final boolean shadow) {
        // Calculate the half-world size 计算半世界大小
        final Projection pj = osmv.getProjection();
        final int zoomLevel = osmv.getZoomLevel() - 1;//缩放级数
        final double tileSizePx = 36/Math.pow(2,zoomLevel);//0级的跨度

        // Calculate the tiles needed for each side around the center one.
        //计算中心周围所需瓦块
        //计算屏幕范围内在level下的行列号
        final int latSpan = osmv.getLatitudeSpan();
        final int longSpan = osmv.getLongitudeSpan();

        final int topLatitude = osmv.getMapCenter().getLatitudeE6() + latSpan/2;
        final int leftLongitude = osmv.getMapCenter().getLongitudeE6() - longSpan/2;
        final int bottomLatitude = osmv.getMapCenter().getLatitudeE6() - latSpan/2;
        final int rightLongitude = osmv.getMapCenter().getLongitudeE6() + longSpan/2;

        System.out.print("ice : " + topLatitude);
        System.out.print("ice : " + leftLongitude);
        System.out.print("ice : " + bottomLatitude);
        System.out.print("ice : " + topLatitude);

        int minRow = GetRowFormLatitude(bottomLatitude*1E-6,tileSizePx);
        int maxRow = GetRowFormLatitude(topLatitude*1E-6,tileSizePx);
        int minCol = GetColFormLongitude(leftLongitude*1E-6,tileSizePx);
        int maxCol = GetColFormLongitude(rightLongitude*1E-6,tileSizePx);

        final int mapTileUpperBound = 1 << zoomLevel;//瓦块上限
        // make sure the cache is big enough for all the tiles 确保缓存足够大，所有的瓦片
        final int numNeeded = (maxRow - minRow + 1) * (maxCol - minCol + 1);//需要切片总张数
        mTileProvider.ensureCapacity(numNeeded);

        /* Draw all the MapTiles (from the upper left to the lower right).
		* 绘制所有的瓦块地图，从左上到右下
		* */
        for (int row = maxRow;row>minRow;row--){
            for (int col = maxCol;col >minCol;col--){
                MapTile tile = new MapTile(zoomLevel,col+1,row+1);
                Drawable currentMapTile = mTileProvider.getMapTile(tile);
                if (currentMapTile != null) {
                    // 行列号转换为经纬度
                    final GeoPoint gp = new GeoPoint(
                            (int) (GetLatFormRow(row,tileSizePx) * 1E6),
                            (int) (GetLonFormCol(col, tileSizePx) * 1E6));//Mercator.tile2lon();

                    //经纬度转换为像素点坐标
                    pj.toPixels(gp, mTilePos);
                    //左、顶、右、底
                    mTileRect.set(mTilePos.x, mTilePos.y, mTilePos.x + 512, mTilePos.y + 512);
                    currentMapTile.setBounds(mTileRect);
                    currentMapTile.draw(c);
                }
            }
        }
    }

    public int GetColFormLongitude(double lon,double tileSizePx){
        return (int) Math.floor((double) ((Math.abs((double) (-180.0 - lon)) % 360.0) / tileSizePx));
    }

    public int GetRowFormLatitude(double lat,double tileSizePx){
        return (int) Math.floor((double) ((Math.abs((double) (-90.0 - lat)) % 180.0) / tileSizePx));
    }
    public double GetLatFormRow(int y,double tileSizePx) {
        return (Math.abs(y * tileSizePx - 90)) % 180;
    }
    public double GetLonFormCol(int x,double tileSizePx) {
        return (Math.abs(x * tileSizePx - 180)) % 360;
    }
}
