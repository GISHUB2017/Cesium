package com.info.osm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileLooper;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.IOverlayMenuProvider;
import org.osmdroid.views.overlay.Overlay;

/**
 * Created by IceWang on 2017/3/31.
 */
public class TestOverlay extends Overlay implements IOverlayMenuProvider {
    public static final int MENU_MAP_MODE = getSafeMenuId();
    public static final int MENU_TILE_SOURCE_STARTING_ID = getSafeMenuIdSequence(TileSourceFactory
            .getTileSources().size());
    public static final int MENU_OFFLINE = getSafeMenuId();

    /** Current tile source */
    protected final MapTileProviderBase mTileProvider;

    /* to avoid allocations during draw */
    protected final Paint mDebugPaint = new Paint();
    private final Rect mTileRect = new Rect();
    private final Point mTilePoint = new Point();
    private final Rect mViewPort = new Rect();
    private Point mTopLeftMercator = new Point();
    private Point mBottomRightMercator = new Point();
    private Point mTilePointMercator = new Point();

    private MyProjection mProjection;

    private boolean mOptionsMenuEnabled = true;

    /** A drawable loading tile **/
    private BitmapDrawable mLoadingTile = null;
    private int mLoadingBackgroundColor = Color.rgb(216, 208, 208);
    private int mLoadingLineColor = Color.rgb(200, 192, 192);

    /** For overshooting the tile cache **/
    private int mOvershootTileCache = 0;

    //Issue 133 night mode
    private boolean isInvert=false;
    final static float[] negate ={
            -1.0f,0,0,0,255,        //red
            0,-1.0f,0,0,255,//green
            0,0,-1.0f,0,255,//blue
            0,0,0,1.0f,0 //alpha
    };
    final static ColorFilter neg = new ColorMatrixColorFilter(negate);


    public TestOverlay(final MapTileProviderBase aTileProvider, final Context aContext) {
        this(aTileProvider, new DefaultResourceProxyImpl(aContext));
    }

    public TestOverlay(final MapTileProviderBase aTileProvider, final ResourceProxy pResourceProxy) {
        super(pResourceProxy);
        if (aTileProvider == null) {
            throw new IllegalArgumentException(
                    "You must pass a valid tile provider to the tiles overlay.");
        }
        this.mTileProvider = aTileProvider;
    }

    @Override
    public void onDetach(final MapView pMapView) {
        this.mTileProvider.detach();
    }

    public int getMinimumZoomLevel() {
        return mTileProvider.getMinimumZoomLevel();
    }

    public int getMaximumZoomLevel() {
        return mTileProvider.getMaximumZoomLevel();
    }

    /**
     * Whether to use the network connection if it's available.
     */
    public boolean useDataConnection() {
        return mTileProvider.useDataConnection();
    }

    /**
     * Set whether to use the network connection if it's available.
     *
     * @param aMode
     *            if true use the network connection if it's available. if false don't use the
     *            network connection even if it's available.
     */
    public void setUseDataConnection(final boolean aMode) {
        mTileProvider.setUseDataConnection(aMode);
    }

    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        // Calculate the half-world size 计算半世界大小
         final Point mTilePos = new Point();

        final Projection pj = osmv.getProjection();
        final int zoomLevel = osmv.getZoomLevel() - 1;//缩放级数
        final double tileSizePx = 36/Math.pow(2,zoomLevel);//0级的跨度

        // Calculate the tiles needed for each side around the center one.
        //计算中心周围所需瓦块
        //计算屏幕范围内在level下的行列号
        final double latSpan = osmv.getLatitudeSpan()/1E6;
        final double longSpan = osmv.getLongitudeSpan()/1E6;

        int lat_ = osmv.getHeight()/2;
        int lon_ = osmv.getWidth()/2;

        GeoPoint temp = (GeoPoint) new MyProjection(osmv).fromPixels(lon_,lat_);

        final double topLatitude = osmv.getMapCenter().getLatitude() + latSpan/2;
        final double leftLongitude = osmv.getMapCenter().getLongitude() - longSpan/2;
        final double bottomLatitude = osmv.getMapCenter().getLatitude() - latSpan/2;
        final double rightLongitude = osmv.getMapCenter().getLongitude() + longSpan/2;

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

    /**
     * This is meant to be a "pure" tile drawing function that doesn't take into account
     * osmdroid-specific characteristics (like osmdroid's canvas's having 0,0 as the center rather
     * than the upper-left corner). Once the tile is ready to be drawn, it is passed to
     * onTileReadyToDraw where custom manipulations can be made before drawing the tile.
     */
    public void drawTiles(final Canvas c, final MyProjection projection, final int zoomLevel,
                          final int tileSizePx, final Rect viewPort) {

        mProjection = projection;
        mTileLooper.loop(c, zoomLevel, tileSizePx, viewPort);

        // draw a cross at center in debug mode
        if (DEBUGMODE) {
            // final GeoPoint center = osmv.getMapCenter();
            final Point centerPoint = new Point(viewPort.centerX(), viewPort.centerY());
            c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x, centerPoint.y + 9, mDebugPaint);
            c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9, centerPoint.y, mDebugPaint);
        }

    }

    private final TileLooper mTileLooper = new TileLooper() {
        @Override
        public void initialiseLoop(final int pZoomLevel, final int pTileSizePx) {
            // make sure the cache is big enough for all the tiles
            final int numNeeded = (mLowerRight.y - mUpperLeft.y + 1) * (mLowerRight.x - mUpperLeft.x + 1);
            mTileProvider.ensureCapacity(numNeeded + mOvershootTileCache);
        }
        @Override
        public void handleTile(final Canvas pCanvas, final int pTileSizePx, final MapTile pTile, final int pX, final int pY) {
            Drawable currentMapTile = mTileProvider.getMapTile(pTile);
            boolean isReusable = currentMapTile instanceof ReusableBitmapDrawable;
            final ReusableBitmapDrawable reusableBitmapDrawable =
                    isReusable ? (ReusableBitmapDrawable) currentMapTile : null;
            if (currentMapTile == null) {
                currentMapTile = getLoadingTile();
            }

            if (currentMapTile != null) {
                mTilePoint.set(pX * pTileSizePx, pY * pTileSizePx);
                mTileRect.set(mTilePoint.x, mTilePoint.y, mTilePoint.x + pTileSizePx, mTilePoint.y
                        + pTileSizePx);
                if (isReusable) {
                    reusableBitmapDrawable.beginUsingDrawable();
                }
                try {
                    if (isReusable && !((ReusableBitmapDrawable) currentMapTile).isBitmapValid()) {
                        currentMapTile = getLoadingTile();
                        isReusable = false;
                    }
                    onTileReadyToDraw(pCanvas, currentMapTile, mTileRect);
                } finally {
                    if (isReusable)
                        reusableBitmapDrawable.finishUsingDrawable();
                }
            }

            if (DEBUGMODE) {
                mTileRect.set(pX * pTileSizePx, pY * pTileSizePx, pX * pTileSizePx + pTileSizePx, pY
                        * pTileSizePx + pTileSizePx);
                pCanvas.drawText(pTile.toString(), mTileRect.left + 1,
                        mTileRect.top + mDebugPaint.getTextSize(), mDebugPaint);
                pCanvas.drawLine(mTileRect.left, mTileRect.top, mTileRect.right, mTileRect.top,
                        mDebugPaint);
                pCanvas.drawLine(mTileRect.left, mTileRect.top, mTileRect.left, mTileRect.bottom,
                        mDebugPaint);
            }
        }
        @Override
        public void finaliseLoop() {
        }
    };

    protected void onTileReadyToDraw(final Canvas c, final Drawable currentMapTile,
                                     final Rect tileRect) {
        if (isInvert)
            currentMapTile.setColorFilter(neg);
        mProjection.toPixelsFromMercator(tileRect.left, tileRect.top, mTilePointMercator);
        tileRect.offsetTo(mTilePointMercator.x, mTilePointMercator.y);
        currentMapTile.setBounds(tileRect);
        currentMapTile.draw(c);
    }

    @Override
    public void setOptionsMenuEnabled(final boolean pOptionsMenuEnabled) {
        this.mOptionsMenuEnabled = pOptionsMenuEnabled;
    }

    @Override
    public boolean isOptionsMenuEnabled() {
        return this.mOptionsMenuEnabled;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                       final MapView pMapView) {
        final SubMenu mapMenu = pMenu.addSubMenu(0, MENU_MAP_MODE + pMenuIdOffset, Menu.NONE,
                mResourceProxy.getString(ResourceProxy.string.map_mode)).setIcon(
                mResourceProxy.getDrawable(ResourceProxy.bitmap.ic_menu_mapmode));

        for (int a = 0; a < TileSourceFactory.getTileSources().size(); a++) {
            final ITileSource tileSource = TileSourceFactory.getTileSources().get(a);
            mapMenu.add(MENU_MAP_MODE + pMenuIdOffset, MENU_TILE_SOURCE_STARTING_ID + a
                    + pMenuIdOffset, Menu.NONE, tileSource.name());
        }
        mapMenu.setGroupCheckable(MENU_MAP_MODE + pMenuIdOffset, true, true);

        final String title = pMapView.getResourceProxy().getString(
                pMapView.useDataConnection() ? ResourceProxy.string.offline_mode
                        : ResourceProxy.string.online_mode);
        final Drawable icon = pMapView.getResourceProxy().getDrawable(
                ResourceProxy.bitmap.ic_menu_offline);
        pMenu.add(0, MENU_OFFLINE + pMenuIdOffset, Menu.NONE, title).setIcon(icon);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu pMenu, final int pMenuIdOffset,
                                        final MapView pMapView) {
        final int index = TileSourceFactory.getTileSources().indexOf(
                pMapView.getTileProvider().getTileSource());
        if (index >= 0) {
            pMenu.findItem(MENU_TILE_SOURCE_STARTING_ID + index + pMenuIdOffset).setChecked(true);
        }

        pMenu.findItem(MENU_OFFLINE + pMenuIdOffset).setTitle(
                pMapView.getResourceProxy().getString(
                        pMapView.useDataConnection() ? ResourceProxy.string.offline_mode
                                : ResourceProxy.string.online_mode));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem pItem, final int pMenuIdOffset,
                                         final MapView pMapView) {

        final int menuId = pItem.getItemId() - pMenuIdOffset;
        if ((menuId >= MENU_TILE_SOURCE_STARTING_ID)
                && (menuId < MENU_TILE_SOURCE_STARTING_ID
                + TileSourceFactory.getTileSources().size())) {
            pMapView.setTileSource(TileSourceFactory.getTileSources().get(
                    menuId - MENU_TILE_SOURCE_STARTING_ID));
            return true;
        } else if (menuId == MENU_OFFLINE) {
            final boolean useDataConnection = !pMapView.useDataConnection();
            pMapView.setUseDataConnection(useDataConnection);
            return true;
        } else {
            return false;
        }
    }

    public int getLoadingBackgroundColor() {
        return mLoadingBackgroundColor;
    }

    /**
     * Set the color to use to draw the background while we're waiting for the tile to load.
     *
     * @param pLoadingBackgroundColor
     *            the color to use. If the value is {@link Color#TRANSPARENT} then there will be no
     *            loading tile.
     */
    public void setLoadingBackgroundColor(final int pLoadingBackgroundColor) {
        if (mLoadingBackgroundColor != pLoadingBackgroundColor) {
            mLoadingBackgroundColor = pLoadingBackgroundColor;
            clearLoadingTile();
        }
    }

    public int getLoadingLineColor() {
        return mLoadingLineColor;
    }

    public void setLoadingLineColor(final int pLoadingLineColor) {
        if (mLoadingLineColor != pLoadingLineColor) {
            mLoadingLineColor = pLoadingLineColor;
            clearLoadingTile();
        }
    }

    private Drawable getLoadingTile() {
        if (mLoadingTile == null && mLoadingBackgroundColor != Color.TRANSPARENT) {
            try {
                final int tileSize = mTileProvider.getTileSource() != null ? mTileProvider
                        .getTileSource().getTileSizePixels() : 512;
                final Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize,
                        Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                final Paint paint = new Paint();
                canvas.drawColor(mLoadingBackgroundColor);
                paint.setColor(mLoadingLineColor);
                paint.setStrokeWidth(0);
                final int lineSize = tileSize / 16;
                for (int a = 0; a < tileSize; a += lineSize) {
                    canvas.drawLine(0, a, tileSize, a, paint);
                    canvas.drawLine(a, 0, a, tileSize, paint);
                }
                mLoadingTile = new BitmapDrawable(bitmap);
            } catch (final OutOfMemoryError e) {
                Log.e(IMapView.LOGTAG, "OutOfMemoryError getting loading tile");
                System.gc();
            } catch (final NullPointerException e) {
                Log.e(IMapView.LOGTAG, "NullPointerException getting loading tile");
                System.gc();
            }
        }
        return mLoadingTile;
    }

    private void clearLoadingTile() {
        final BitmapDrawable bitmapDrawable = mLoadingTile;
        mLoadingTile = null;
        // Only recycle if we are running on a project less than 2.3.3 Gingerbread.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            if (bitmapDrawable != null) {
                bitmapDrawable.getBitmap().recycle();
            }
        }
    }

    /**
     * Set this to overshoot the tile cache. By default the TilesOverlay only creates a cache large
     * enough to hold the minimum number of tiles necessary to draw to the screen. Setting this
     * value will allow you to overshoot the tile cache and allow more tiles to be cached. This
     * increases the memory usage, but increases drawing performance.
     *
     * @param overshootTileCache
     *            the number of tiles to overshoot the tile cache by
     */
    public void setOvershootTileCache(int overshootTileCache) {
        mOvershootTileCache = overshootTileCache;
    }

    /**
     * Get the tile cache overshoot value.
     *
     * @return the number of tiles to overshoot tile cache
     */
    public int getOvershootTileCache() {
        return mOvershootTileCache;
    }
}
