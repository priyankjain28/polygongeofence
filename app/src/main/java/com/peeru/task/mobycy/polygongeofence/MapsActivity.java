package com.peeru.task.mobycy.polygongeofence;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.peeru.task.mobycy.polygongeofence.R;

public class MapsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Log.d(TAG, "Access Token " + accessToken);
        //newTagFragment = NewTagFragment.newInstance();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MapFragment()).commit();
        }
    }
}
