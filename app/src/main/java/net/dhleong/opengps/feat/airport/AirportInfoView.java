package net.dhleong.opengps.feat.airport;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    @BindView(R.id.pager_tabs) TabLayout tabs;

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
    }

    @Override
    public Single<Void> result(Airport input) {
        // bind UI
        pager.setAdapter(new AirportInfoTabsAdapter(getContext(), input));
        tabs.setupWithViewPager(pager);

        // just something that never auto-completes
        return PublishSubject.<Void>create().toSingle();
    }

    static class AirportInfoTabsAdapter extends PagerAdapter {
        static class Page {
            public final @LayoutRes int layout;
            public final @StringRes int title;

            Page(final @LayoutRes int layout, final @StringRes int title) {
                this.layout = layout;
                this.title = title;
            }
        }

        static final Page[] PAGES = {
            new Page(R.layout.feat_airport_page_info, R.string.airport_page_info),
            new Page(R.layout.feat_airport_page_freqs, R.string.airport_page_frequencies),
        };

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
            return PAGES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return context.getString(
                PAGES[position].title
            );
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int layout = PAGES[position].layout;
            AirportPageView view = (AirportPageView) LayoutInflater.from(container.getContext())
                                                                   .inflate(layout, container, false);
            view.bind(airport);
            container.addView((View) view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
