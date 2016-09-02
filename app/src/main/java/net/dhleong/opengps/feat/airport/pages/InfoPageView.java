package net.dhleong.opengps.feat.airport.pages;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airport.AirportPageView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class InfoPageView
        extends LinearLayout
        implements AirportPageView {

    @BindView(R.id.name) TextView name;
    @BindView(R.id.id) TextView id;
    @BindView(R.id.elevation) TextView elevation;

    public InfoPageView(Context context) {
        super(context);
    }

    public InfoPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InfoPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public void bind(Airport airport) {
        name.setText(airport.name()); // TODO title case?
        id.setText(airport.id());
        elevation.setText(String.format(Locale.US, "%.3f ft", airport.elevation));
    }
}
