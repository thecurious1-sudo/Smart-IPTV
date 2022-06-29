package vaibhav.tech.smartiptv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.appbrain.AdId;
import com.appbrain.InterstitialBuilder;


public class SetPlayer extends AppCompatActivity {
    private InterstitialBuilder interstitialBuilder;
    RadioGroup radioGroup;
    int a[]={R.id.one,R.id.two,R.id.three,R.id.four,R.id.five};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_player);
        //AppBrain.addTestDevice("9993f2aa81232f67");
        interstitialBuilder = InterstitialBuilder.create()
                .setAdId(AdId.custom("int-2141be"))
                .setOnDoneCallback(new Runnable() {
                    @Override
                    public void run() {
                        // Preload again, so we can use interstitialBuilder again.
                        interstitialBuilder.preload(getApplicationContext());
                        startActivity(new Intent(getApplicationContext(),IptvActivity.class));
                    }}).preload(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Set Player");
        radioGroup=(RadioGroup)findViewById(R.id.radioGrp);
        SharedPreferences sharedPreferences=getSharedPreferences("smartiptv_mediaPlayer", Context.MODE_PRIVATE);
        int med_id=sharedPreferences.getInt("media_player_id",R.id.one);
        RadioButton radioButton=(RadioButton)findViewById(med_id);
        radioButton.setChecked(true);

    }
    public void select_radio(View v)
    {
        int radioId=radioGroup.getCheckedRadioButtonId();

        SharedPreferences sharedPreferences=getSharedPreferences("smartiptv_mediaPlayer", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("media_player_id",radioId);

        editor.apply();

        String app="MX Player";
            if(radioId==a[1])
                app="VLC";
            else if(radioId==a[2])
                app="BS Player";
            else if(radioId==a[3])
                app="KM Player";
            else if(radioId==a[4])
                app="Wondershare Player";

        Toast.makeText(this, app+" selected as default player for mobile videos.", Toast.LENGTH_SHORT).show();
        interstitialBuilder.show(getApplicationContext());

    }

}

