package net.dhleong.opengps.feat.airport.pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airport.AirportPageView;
import net.dhleong.opengps.feat.chartDisplay.ChartDisplayView;
import net.dhleong.opengps.feat.charts.ChartPickerView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.NavigateUtil;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static net.dhleong.opengps.util.RxUtil.notNull;

/**
 * @author dhleong
 */
public class InfoPageView
        extends LinearLayout
        implements AirportPageView {

    @BindView(R.id.name) TextView name;
    @BindView(R.id.id) TextView id;
    @BindView(R.id.elevation) TextView elevation;
    @BindView(R.id.charts) View chartsButton;

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
        elevation.setText(String.format(Locale.US, "%.0f ft", airport.elevation));

        RxView.clicks(chartsButton)
              .flatMap(any ->
                  DialogPrompter.prompt(getContext(), ChartPickerView.class,
                      R.layout.feat_charts, airport))
              .filter(notNull())
              .subscribe(chartInfo -> {
                  NavigateUtil.into(getContext(),
                      ChartDisplayView.class, R.layout.feat_chart_display,
                      chartInfo);
              });
    }
}
