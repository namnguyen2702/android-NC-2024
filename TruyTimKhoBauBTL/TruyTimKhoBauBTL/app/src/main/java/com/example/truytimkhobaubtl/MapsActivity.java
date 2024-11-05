package com.example.truytimkhobaubtl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation; // Vị trí hiện tại của người chơi
    private TreasureManager treasureManager; // Quản lý kho báu
    private QuizManager quizManager; // Quản lý câu đố
    private Marker selectedTreasureMarker = null; // Marker của kho báu được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Khởi tạo FusedLocationProviderClient để lấy vị trí
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Khởi tạo TreasureManager và QuizManager
        treasureManager = new TreasureManager();
        quizManager = new QuizManager();

        // Nút tìm kho báu
        Button btnFindTreasure = findViewById(R.id.btn_find_treasure);
        btnFindTreasure.setOnClickListener(v -> handleFindTreasureClick());
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000); // cập nhật mỗi 2 giây
        locationRequest.setFastestInterval(1000); // cập nhật nhanh nhất mỗi 1 giây
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    // cập nhật lại khoảng cách khi có vị trí mới
                    updateDistanceToTreasures(currentLocation);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateDistanceToTreasures(LatLng userLocation) {
        for (Marker treasure : treasureManager.getAllTreasures()) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.latitude, userLocation.longitude,
                    treasure.getPosition().latitude, treasure.getPosition().longitude, results);

            // Cập nhật lại khoảng cách và kiểm tra điều kiện nếu cần
            if (results[0] < 50) {

            }
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Kiểm tra và yêu cầu quyền truy cập vị trí nếu chưa được cấp
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);

            startLocationUpdates();

            // Tạo kho báu khi vị trí người chơi đã được xác định
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        treasureManager.createTreasureLocations(mMap, currentLocation, MapsActivity.this);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(LocationRequest.create(), locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Xử lý sự kiện nhấn vào Marker
        mMap.setOnMarkerClickListener(marker -> {
            selectedTreasureMarker = marker;

            // Tính khoảng cách từ vị trí hiện tại đến kho báu
            float[] results = new float[1];
            Location.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    marker.getPosition().latitude, marker.getPosition().longitude,
                    results
            );
            float distanceInMeters = results[0];

            Toast.makeText(this, "Kho báu được chọn: " + marker.getTitle() + "\nKhoảng cách của kho báu là " + Math.round(distanceInMeters) + "m", Toast.LENGTH_SHORT).show();

            return false;
        });

    }


    private void handleFindTreasureClick() {
        if (selectedTreasureMarker != null) {
            // Nếu có kho báu được chọn, kiểm tra khoảng cách với kho báu đó
            if (treasureManager.isWithinDistance(currentLocation, selectedTreasureMarker, 50)) {
                showQuizDialog(selectedTreasureMarker); // Hiển thị câu đố nếu trong phạm vi 50m
            } else {
                Toast.makeText(this, "Khoảng cách quá xa bạn, hãy tiến lại gần kho báu hơn.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không có kho báu nào được chọn, kiểm tra các kho báu gần nhất trong phạm vi 50m
            Marker nearbyTreasure = treasureManager.getNearbyTreasureMarker(currentLocation);
            if (nearbyTreasure != null) {
                showQuizDialog(nearbyTreasure);
            } else {
                Toast.makeText(this, "Không có kho báu nào trong khoảng cách gần bạn.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showQuizDialog(Marker treasureMarker) {
        QuizManager.Quiz quiz = quizManager.getRandomQuiz();

        // Tạo giao diện câu đố
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_quiz, null);
        EditText editTextAnswer = dialogView.findViewById(R.id.edit_text_answer);

        // Hiển thị hộp thoại câu đố
        new AlertDialog.Builder(this)
                .setTitle("Câu đố kho báu")
                .setMessage(quiz.getQuestion())
                .setView(dialogView)
                .setPositiveButton("Trả lời", (dialog, which) -> {
                    String userAnswer = editTextAnswer.getText().toString();

                    if (quizManager.checkAnswer(quiz, userAnswer)) {
                        Toast.makeText(MapsActivity.this, "Chúc mừng! Bạn đã tìm thấy kho báu!", Toast.LENGTH_LONG).show();
                        treasureManager.collectTreasure(treasureMarker);
                    } else {
                        Toast.makeText(MapsActivity.this, "Sai rồi! Thử lại lần sau!", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Bỏ qua", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
