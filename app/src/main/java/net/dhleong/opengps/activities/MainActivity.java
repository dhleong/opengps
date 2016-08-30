package net.dhleong.opengps.activities;

import android.os.Bundle;

import net.dhleong.opengps.R;
import net.dhleong.opengps.core.ActivityModuleActivity;

public class MainActivity
    extends ActivityModuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
