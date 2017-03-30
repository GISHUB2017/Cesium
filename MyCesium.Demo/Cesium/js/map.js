var layers = null;
var viewer = null;

function onload(Cesium) {
    viewer = new Cesium.Viewer('mapControl', {
        animation: false, //动画控制不显示
        baseLayerPicker: false,//图层控制显示
        geocoder: false,//地名查找不显示
        timeline: false,//时间线不显示
        scene3DOnly: false,//只显示三维模式
        selectionIndicator: false,
        infoBox: false,
        fullscreenButton: true,//全屏按钮
        VrButton: true,//VR按钮
        navigationHelpButton: true,//导航帮助按钮
        sceneModePicker: true,//投影方式显示
    });

    //初始视图中心位置
    viewer.camera.flyTo({
        destination: new Cesium.Cartesian3.fromDegrees(108, 32, 6000000)
    })

    //Home键
    Cesium.Camera.DEFAULT_VIEW_RECTANGLE = Cesium.Rectangle.fromDegrees(100, 25, 110, 35);

    layers = viewer.imageryLayers;
    layers.removeAll();

    //天地图影像图层
    var tdtImg = new Cesium.WebMapTileServiceImageryProvider({
        url: "http://t0.tianditu.com/img_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=img&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles",
        layer: "tdtImgLayer",
        style: "default",
        format: "image/jpeg",
        tileMatrixSetID: "GoogleMapsCompatible",
        credit: new Cesium.Credit('天地图影像'),
        show: false
    });

    layers.addImageryProvider(tdtImg);

    //天地图影像标注图层
    var tdtAnnoLyr = new Cesium.WebMapTileServiceImageryProvider({
        url: "http://t0.tianditu.com/cia_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=cia&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles",
        layer: "tdtImgAnnoLayer",
        style: "default",
        format: "image/jpeg",
        tileMatrixSetID: "GoogleMapsCompatible",
        credit: new Cesium.Credit('天地图影像注记'),
        show: false
    })

    layers.addImageryProvider(tdtAnnoLyr);

    //var imageryLayers = viewer.imageryLayers;

    //Sandcastle.addDefaultToolbarMenu([{
    //    text: '全球影像地图服务(经纬度)',
    //    onselect: function () {
    //        var baseLayer = imageryLayers.get(0);
    //        imageryLayers.addImageryProvider(new Cesium.WebMapTileServiceImageryProvider({
    //            url: 'http://t0.tianditu.com/img_c/wmts?service=WMTS&version=1.0.0&request=GetTile&tilematrix={TileMatrix}&layer=img&style={style}&tilerow={TileRow}&tilecol={TileCol}&tilematrixset={TileMatrixSet}&format=tiles',
    //            layer: 'img',
    //            style: 'default',
    //            format: 'tiles',
    //            tileMatrixSetID: 'c',
    //            credit: new Cesium.Credit('天地图全球影像服务'),
    //            subdomains: ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7'],
    //            maximumLevel: 15,
    //            tilingScheme: new Cesium.GeographicTilingScheme(),
    //            tileMatrixLabels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19']
    //        }), 1);
    //        imageryLayers.remove(baseLayer);
    //    }
    //}, {
    //    text: '全球影像地图服务(墨卡托)',
    //    onselect: function () {
    //        var baseLayer = imageryLayers.get(0);

    //        var url = 'http://{s}.tianditu.com/img_w/wmts?service=WMTS&version=1.0.0&request=GetTile&tilematrix={TileMatrix}&layer=img&style={style}&tilerow={TileRow}&tilecol={TileCol}&tilematrixset={TileMatrixSet}&format=tiles';

    //        imageryLayers.addImageryProvider(new Cesium.WebMapTileServiceImageryProvider({
    //            url: url,
    //            layer: 'img',
    //            style: 'default',
    //            format: 'tiles',
    //            tileMatrixSetID: 'w',
    //            credit: new Cesium.Credit('天地图全球影像服务'),
    //            subdomains: ['t0', 't1', 't2', 't3', 't4', 't5', 't6', 't7'],
    //            maximumLevel: 18
    //        }), 1);
    //        imageryLayers.remove(baseLayer);
    //    }
    //}]);

}

function TestClick() {
    //layers.removeAll();
    alert("测试");
}