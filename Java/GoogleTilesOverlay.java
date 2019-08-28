package com.helloworld;

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
import org.osmdroid.util.MyMath;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.util.Mercator;

/**
 * Created by IceWang on 2017/3/30.
 */
public class GoogleTilesOverlay extends Overlay {
    private static final boolean DEBUGMODE = false;//调试模式

    /** Current tile source */
    protected MapTileProviderBase mTileProvider;//地图瓦块数据源

    /* to avoid allocations during draw 瓦块绘制区域*/
    protected final Paint mPaint = new Paint();
    private final Rect mTileRect = new Rect();
    private final Point mTilePos = new Point();

    //构造函数
    public GoogleTilesOverlay(Context ctx, MapTileProviderBase mTileProvider) {
        super(ctx);
        this.mTileProvider = mTileProvider;
    }

    public GoogleTilesOverlay(ResourceProxy pResourceProxy, MapTileProviderBase mTileProvider) {
        super(pResourceProxy);
        this.mTileProvider = mTileProvider;
    }

    public GoogleTilesOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
        super(aContext);
        if (aTileProvider == null) {
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
        if (DEBUGMODE) {
        }

        // Calculate the half-world size 计算半世界大小
        final Projection pj = osmv.getProjection();
        final int zoomLevel = osmv.getZoomLevel() - 1;//缩放级数
        final int tileSizePx = this.mTileProvider.getTileSource().getTileSizePixels();//0级的跨度

        // Calculate the tiles needed for each side around the center one.
        //计算中心周围所需瓦块
        //计算屏幕范围内在level下的行列号
        final int latSpan = osmv.getLatitudeSpan();
        final int longSpan = osmv.getLongitudeSpan();
        final int topLatitude = osmv.getMapCenter().getLatitudeE6() + latSpan/2;
        final int leftLongitude = osmv.getMapCenter().getLongitudeE6() - longSpan/2;
        final int bottomLatitude = osmv.getMapCenter().getLatitudeE6() - latSpan/2;
        final int rightLongitude = osmv.getMapCenter().getLongitudeE6() + longSpan/2;
        final Point leftTopXY = Mercator.projectGeoPoint(topLatitude*1E-6, leftLongitude*1E-6, zoomLevel, new Point(0,0));
        final Point rightBottomXY = Mercator.projectGeoPoint(bottomLatitude*1E-6, rightLongitude*1E-6, zoomLevel, new Point(0,0));
        final int tileNeededAtLeft = leftTopXY.x;
        final int tileNeededAtRight = rightBottomXY.x;
        final int tileNeededAtTop = leftTopXY.y;
        final int tileNeededAtBottom = rightBottomXY.y;

        final int mapTileUpperBound = 1 << zoomLevel;//瓦块上限
        // make sure the cache is big enough for all the tiles确保缓存足够大，所有的瓦片
        final int numNeeded = (tileNeededAtBottom - tileNeededAtTop + 1)
                * (tileNeededAtRight - tileNeededAtLeft + 1);
        mTileProvider.ensureCapacity(numNeeded);

		/* Draw all the MapTiles (from the upper left to the lower right).
		* 绘制所有的瓦块地图，从左上到右下
		* */
        for (int y = tileNeededAtTop; y <= tileNeededAtBottom; y++) {
            for (int x = tileNeededAtLeft; x <= tileNeededAtRight; x++) {
                // Construct a MapTile to request from the tile provider.
                //构建一个地图瓦片从瓷砖供应商的要求。
                final int tileY = MyMath.mod(y, mapTileUpperBound);
                final int tileX = MyMath.mod(x, mapTileUpperBound);
                final MapTile tile = new MapTile(zoomLevel, tileX, tileY);
                final Drawable currentMapTile = mTileProvider.getMapTile(tile);
                if (currentMapTile != null) {
                    final GeoPoint gp = new GeoPoint(
                            (int) (Mercator.tile2lat(y, zoomLevel) * 1E6),
                            (int) (Mercator.tile2lon(x, zoomLevel) * 1E6));
                    pj.toPixels(gp, mTilePos);
                    mTileRect.set(mTilePos.x, mTilePos.y, mTilePos.x + tileSizePx, mTilePos.y + tileSizePx);
                    currentMapTile.setBounds(mTileRect);
                    currentMapTile.draw(c);
                }

                if (DEBUGMODE) {
                    c.drawText(tile.toString(), mTileRect.left + 1, mTileRect.top + mPaint.getTextSize(), mPaint);
                    c.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top, mPaint);
                    c.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom, mPaint);
                }
            }
        }

        // draw a cross at center in debug mode
        if (DEBUGMODE) {
            final GeoPoint center = (GeoPoint) osmv.getMapCenter();
            final Point centerPoint = pj.toPixels(center, null);
            c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mPaint);
            c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mPaint);
        }
    }
}
