package com.example.youngju.meet_spot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
// 카카오 API를 이용하여 카카오 지도에 친구들의 위치를 파란색 마커로 중간지점을 빨간색 마커로 표시하는 엑티비티.
public class FindLocationActivity extends AppCompatActivity implements MapView.POIItemEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private GeoDataClient mGeoDataClient;
    private MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_location);
        //LatLng 객체 꺼내기
        Intent intent = getIntent();
        ArrayList<LatLng> arr = (ArrayList<LatLng>)intent.getSerializableExtra("arr");

        mGeoDataClient = Places.getGeoDataClient(this, null);

        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        // 친구의 수 만큼 반복하면서 지도에 마커를 표시.
        mapView.setPOIItemEventListener(this);
        if(arr != null){
            int i = 0;
            for(LatLng point : arr){
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName("친구"+ i);
                marker.setTag(i+1);
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(point.latitude,point.longitude));
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.BluePin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                marker.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
                mapView.addPOIItem(marker);
                i++;
            }
            //중간점 계산 다각형의 무게중심.
            double sum_lat = 0;
            double sum_lng = 0;
            //중간지점 계산
            for(LatLng point : arr){
                sum_lat+= point.latitude;
                sum_lng+= point.longitude;
            }
            sum_lat = sum_lat/arr.size();
            sum_lng = sum_lng/arr.size();

            //중간지점으로 이동
            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(sum_lat,sum_lng), 6, true);

            //중간지점 마커추가
            MapPOIItem marker_center = new MapPOIItem();
            marker_center.setItemName("중간지점 주변장소 선택");
            marker_center.setTag(0);
            marker_center.setMapPoint(MapPoint.mapPointWithGeoCoord(sum_lat,sum_lng));
            marker_center.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
            marker_center.setShowAnimationType(MapPOIItem.ShowAnimationType.SpringFromGround);
            mapView.addPOIItem(marker_center);
            //중간지점에 원 그리기
            MapCircle circle1 = new MapCircle(
                    MapPoint.mapPointWithGeoCoord(sum_lat, sum_lng), // center
                    500, // radius
                    Color.argb(128, 255, 0, 0), // strokeColor
                    Color.argb(128, 0, 255, 0) // fillColor
            );
            circle1.setTag(1234);
            mapView.addCircle(circle1);

            //폴리라인 그리기
            for(LatLng point : arr) {
                MapPolyline polyline = new MapPolyline();
                polyline.setTag(1000);
                polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.
                // Polyline 좌표 지정.
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(point.latitude, point.longitude));
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(sum_lat, sum_lng));

                // Polyline 지도에 올리기.
                mapView.addPolyline(polyline);
            }



        }


    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder("33af90ec799df607ca58bf244f1e521d", mapPOIItem.getMapPoint(), this, this);
        reverseGeoCoder.startFindingAddress();
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }
    // 중간지점 마커를 선택하면 구글 PlacePicker를 실행
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        PlacePicker.IntentBuilder builder;
        builder = new PlacePicker.IntentBuilder();
        LatLng latLng = new LatLng(mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude,mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude);
        LatLng latLng1 = new LatLng(latLng.latitude-0.01,latLng.longitude-0.01);
        LatLng latLng2 = new LatLng(latLng.latitude+0.01,latLng.longitude+0.01);

        try {
            builder.setLatLngBounds(new LatLngBounds(latLng1,latLng2));
            startActivityForResult(builder.build(FindLocationActivity.this), 2);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        Toast.makeText(this, s,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        Toast.makeText(this, "주소를 찾을 수 없습니다.",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String address = place.getAddress().toString();
                String name = place.getName().toString();
                String id = place.getId();
                /******************** 추가 *****************************************************/
                 LocationTemplate params = LocationTemplate.newBuilder(address,
                        ContentObject.newBuilder(name+"에서 만나요",
                                "https://maps.googleapis.com/maps/api/staticmap?center="+place.getName().toString()+"&zoom=15&scale=1&size=150x150&maptype=roadmap\n" +
                                        "&markers=color:red%7Clabel:C%7C+"+place.getLatLng().latitude+","+place.getLatLng().longitude+"\n" +
                                        "&key=AIzaSyB30unzZlu2fRYVrYjfNnq3_TVH9s4g6zw",
                                LinkObject.newBuilder()
                                        .setWebUrl("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query="+address+"+"+name)
                                        .setMobileWebUrl("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query="+address+"+"+name)
                                        .build())
                                .setDescrption("Meet-Spot에서 보낸 중간 지점입니다.")
                                .build())
                        .setAddressTitle("중간지점")
                        .build();


                Map<String, String> serverCallbackArgs = new HashMap<String, String>();
                serverCallbackArgs.put("user_id", "${current_user_id}");
                serverCallbackArgs.put("product_id", "${shared_product_id}");

                KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Logger.e(errorResult.toString());
                    }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {
                        // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                    }
                });
                /******************** 추가 *****************************************************/
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query="+address+"+"+name)));
            }
        }
    }
    /*private void getPhotos(String s) {
        String placeId = s;
        Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                    }
                });
            }
        });
    }*/
}
