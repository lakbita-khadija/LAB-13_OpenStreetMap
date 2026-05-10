package com.example.mapapplication;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class GoogleMapActivity extends AppCompatActivity {

    MapView map;
    RequestQueue requestQueue;

    String showUrl = "http://10.0.2.2/map_project/getPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osm_prefs", MODE_PRIVATE)
        );

        setContentView(R.layout.activity_google_map);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(33.5731, -7.5898));

        requestQueue = Volley.newRequestQueue(this);

        loadPositions();
    }

    private void loadPositions() {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");

                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject obj = positions.getJSONObject(i);

                            double lat = obj.getDouble("latitude");
                            double lng = obj.getDouble("longitude");

                            Marker marker = new Marker(map);
                            marker.setPosition(new GeoPoint(lat, lng));
                            marker.setTitle("Position " + (i + 1));

                            Drawable drawable = getResources().getDrawable(R.drawable.marker);
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 80, 80, false);

                            marker.setIcon(new BitmapDrawable(getResources(), smallMarker));
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            map.getOverlays().add(marker);
                        }

                        map.invalidate();

                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erreur chargement positions", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}