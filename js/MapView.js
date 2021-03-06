import {
    requireNativeComponent,
    View,
    NativeModules,
    Platform,
    DeviceEventEmitter
} from 'react-native';
import React, {
    Component,
} from 'react';
import PropTypes from "prop-types"

import MapTypes from './MapTypes';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

export default class MapView extends Component {
    static propTypes = {
        ...View.propTypes,
        zoomControlsVisible: PropTypes.bool,
        trafficEnabled: PropTypes.bool,
        showsCompass:PropTypes.bool,
        showsUserLocation:PropTypes.bool,
        userLocationViewParams:PropTypes.object,
        zoomEnabled:PropTypes.bool,
        baiduHeatMapEnabled: PropTypes.bool,
        mapType: PropTypes.number,
        zoom: PropTypes.number,
        center: PropTypes.object,
        marker: PropTypes.object,
        markers: PropTypes.array,
        childrenPoints: PropTypes.array,
        onMapStatusChangeStart: PropTypes.func,
        onMapStatusChange: PropTypes.func,
        onMapStatusChangeFinish: PropTypes.func,
        onMapLoaded: PropTypes.func,
        onMapClick: PropTypes.func,
        onMapDoubleClick: PropTypes.func,
        onMarkerClick: PropTypes.func,
        onMapPoiClick: PropTypes.func
    };

    static defaultProps = {
        zoomControlsVisible: true,
        trafficEnabled: false,
        baiduHeatMapEnabled: false,
        mapType: MapTypes.NORMAL,
        childrenPoints: [],
        marker: null,
        markers: [],
        center: null,
        zoom: 10
    };

    constructor() {
        super();
    }

    _onChange(event) {
        if (typeof this.props[event.nativeEvent.type] === 'function') {
            this.props[event.nativeEvent.type](event.nativeEvent.params);
        }
    }

    render() {
        let {markers, marker} =this.props;
        let markers_ = JSON.deepcopy(markers);
        let marker_ = JSON.deepcopy(marker);
        if(marker_ && marker_.image){
            marker_.image = resolveAssetSource(marker_.image);
        }
        markers_.map(item=>{
            if(item.image){
                item.image = resolveAssetSource(item.image);
            }
        });
        return <BaiduMapView {...this.props} marker={marker_} markers={markers_} onChange={this._onChange.bind(this)}/>;
    }
}

const BaiduMapView = requireNativeComponent('RCTBaiduMapView', MapView, {
    nativeOnly: {onChange: true}
});
