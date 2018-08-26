package com.example.youngju.meet_spot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    int num_of_place;
    int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder;
    FloatingActionButton fab;
    Button fab2;
    RecyclerView recyclerView ;
    ArrayList<LatLng> arr;
    ArrayList<Item> arrayList;
    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        num_of_place = 1;


        arr = new ArrayList<LatLng>();
        // Adapter 생성

        arrayList = new ArrayList<Item>();
        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), arrayList, R.layout.item_cardview);

        // 리스트뷰 참조 및 Adapter달기
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setAdapter(recyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



        // 플로팅 버튼 이벤트 등록
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //어플리케이션을 실행하였을 떄 현재 위치를 나타내고 싶다면 아래 내용을 주석 해제.

        /*
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 2 );
        }else{
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.i("Current Location", String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                    }
                    @SuppressLint("RestrictedApi") PlaceLikelihood placeLikelihood  = likelyPlaces.get(1);
                    arr.add(placeLikelihood.getPlace().getLatLng());

                    arrayList.add(new Item("https://maps.googleapis.com/maps/api/staticmap?center="+placeLikelihood.getPlace().getName().toString()+"&zoom=15&scale=2&size=500x200&maptype=roadmap\n" +
                            "&markers=color:red%7Clabel:C%7C+"+placeLikelihood.getPlace().getLatLng().latitude+","+placeLikelihood.getPlace().getLatLng().longitude+"\n" +
                            "&key=AIzaSyB30unzZlu2fRYVrYjfNnq3_TVH9s4g6zw",placeLikelihood.getPlace().getName().toString(),placeLikelihood.getPlace().getAddress().toString(),0));
                    recyclerAdapter.notifyDataSetChanged();
                    adapter.addItem(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_face_black_24dp),
                            "나", String.format("%s\n%s",placeLikelihood.getPlace().getAddress().toString(),placeLikelihood.getPlace().getName())) ;
                    adapter.notifyDataSetChanged();

                }
            });
        }*/

        getAppKeyHash();
        fab2= findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindLocationActivity.class);
                intent.putExtra("arr",arr);
                startActivity(intent);
            }
        });

    }
    // +모양 추가버튼을 눌러서 친구를 추가하는 PlacePicker 엑티비티의 장소정보를 받아오는 부분
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                arr.add(place.getLatLng());
                //친구의 장소를 담은 arrayList에 Item객체를 만들어서 추가.
                arrayList.add(new Item("https://maps.googleapis.com/maps/api/staticmap?center="+place.getName().toString()+"&zoom=15&scale=2&size=500x200&maptype=roadmap\n" +
                        "&markers=color:red%7Clabel:C%7C+"+place.getLatLng().latitude+","+place.getLatLng().longitude+"\n" +
                        "&key=AIzaSyB30unzZlu2fRYVrYjfNnq3_TVH9s4g6zw",place.getName().toString(),place.getAddress().toString(),num_of_place));
                num_of_place++;
                recyclerAdapter.notifyDataSetChanged();

            }
        }
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }



}
