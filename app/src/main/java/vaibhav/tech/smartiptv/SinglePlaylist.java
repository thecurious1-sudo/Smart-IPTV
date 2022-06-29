package vaibhav.tech.smartiptv;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;


import com.appbrain.AdId;
import com.appbrain.InterstitialBuilder;

import java.util.ArrayList;

public class SinglePlaylist extends AppCompatActivity {
    ArrayList<String> listItems;
    private InterstitialBuilder interstitialBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_playlist);
        //AppBrain.addTestDevice("9993f2aa81232f67");
        interstitialBuilder = InterstitialBuilder.create()
                .setAdId(AdId.custom("int-229556"))
                .setOnDoneCallback(new Runnable() {
                    @Override
                    public void run() {
                        // Preload again, so we can use interstitialBuilder again.
                        interstitialBuilder.preload(getApplicationContext());
                    }}).preload(this);
        interstitialBuilder.show(getApplicationContext());
        GridView gridView=(GridView)findViewById(R.id.gridView);
        String titles[]=getIntent().getStringArrayExtra("names");
        String urls[]=getIntent().getStringArrayExtra("urls");
        gridView.setAdapter(new GridViewAdapter(SinglePlaylist.this,titles,urls));
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),PlaylistShow2.class));
    }
}
