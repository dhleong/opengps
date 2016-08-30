package net.dhleong.opengps.core;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import net.dhleong.opengps.App;

/**
 * @author dhleong
 */
public class ActivityModuleActivity extends AppCompatActivity {

    private ActivityComponent myActivityComponent;
    private ActivityModule module;

    private ActivityModule onCreateActivityModule() {
        return new ActivityModule(this);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        // enforce a per-activity singleton component instance
        //  (so @PerActivity scope actually works)
        if (App.ACTIVITY_COMPONENT.equals(name)) {
            final ActivityComponent existing = myActivityComponent;
            if (existing != null) return existing;

            if (module == null) {
                module = onCreateActivityModule();
            }

            //noinspection WrongConstant
            return myActivityComponent =
                App.component(this)
                   .newActivityComponent(module);
        }

        return super.getSystemService(name);
    }
}
