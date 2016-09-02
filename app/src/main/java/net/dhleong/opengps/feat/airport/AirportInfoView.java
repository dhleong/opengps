package net.dhleong.opengps.feat.airport;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;
import rx.subjects.PublishSubject;

/**
 * @author dhleong
 */
public class AirportInfoView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<Airport, Void> {

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.pager_tabs) PagerTabStrip strip;

    public AirportInfoView(Context context) {
        super(context);
    }

    public AirportInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AirportInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
//        strip.get
    }

    @Override
    public Single<Void> result(Airport input) {
        // bind UI
        pager.setAdapter(new AirportInfoTabsAdapter(getContext(), input));

        // just something that never auto-completes
        return PublishSubject.<Void>create().toSingle();
    }

    static class AirportInfoTabsAdapter extends PagerAdapter {

        static final int PAGE_FREQUENCIES = 0;

        private final Context context;
        private final Airport airport;

        public AirportInfoTabsAdapter(Context context, Airport airport) {
            this.context = context;
            this.airport = airport;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            default:
            case PAGE_FREQUENCIES:
                return context.getString(R.string.airport_page_frequencies);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
            default:
            case PAGE_FREQUENCIES:
                // TODO
                TextView v = new TextView(context);
                v.setText(airport.frequencies(Airport.FrequencyType.GROUND).toString());
                container.addView(v);
                return v;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
