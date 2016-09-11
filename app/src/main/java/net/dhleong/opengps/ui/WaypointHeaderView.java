package net.dhleong.opengps.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dhleong.opengps.R;
import net.dhleong.opengps.impl.BaseAeroObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class WaypointHeaderView extends RelativeLayout {

    @BindView(R.id.id) TextView id;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.id_name_spacer) View spacer;

    @BindView(R.id.bearing) TextView bearing;
    @BindView(R.id.distance) TextView distance;

    @BindView(R.id.lat) TextView lat;
    @BindView(R.id.lng) TextView lng;

    public WaypointHeaderView(Context context) {
        super(context);
    }

    public WaypointHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaypointHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void bind(BaseAeroObject object) {
        final String objId = object.id();
        final String objName = object.name();
        id.setText(objId);

        // handle spacing for navfixes, whose name is the same as their id
        if (objId.equals(objName)) {
            final LayoutParams params = (LayoutParams) spacer.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_END, R.id.id);
        } else {
            name.setText(objName);
        }

        lat.setText(TextUtil.formatLat(object.lat()));
        lng.setText(TextUtil.formatLng(object.lng()));
    }
}
