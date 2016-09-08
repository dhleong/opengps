package net.dhleong.opengps.ui;

import android.os.Bundle;

/**
 * @author dhleong
 */
public interface LifecycleDelegate {
    void onCreate(Bundle savedInstanceState);
    void onResume();
    void onPause();
    void onDestroy();
    void onLowMemory();
    void onSaveInstanceState(Bundle outState);
}
