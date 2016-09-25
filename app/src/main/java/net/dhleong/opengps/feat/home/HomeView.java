package net.dhleong.opengps.feat.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.waypoint.WaypointSearchView;
import net.dhleong.opengps.ui.NavigateUtil;
import net.dhleong.opengps.ui.UiUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author dhleong
 */
public class HomeView extends CoordinatorLayout {

    // NB just inject to ensure we eagerly start loading
    @Inject OpenGps gps;

    @BindView(R.id.recycler) RecyclerView recycler;

    private Adapter adapter;

    public HomeView(Context context) {
        super(context);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .newHomeComponent()
           .inject(this);

        GridLayoutManager gridLayoutMan =
            new GridLayoutManager(getContext(), 2);
        recycler.setLayoutManager(gridLayoutMan);
        recycler.setAdapter(adapter = new Adapter());
        recycler.addItemDecoration(new SpacingDecorator(
            getResources().getDimensionPixelOffset(R.dimen.padding_huge)
        ));
    }

    static class HomeMenuItem {
        public final @DrawableRes int icon;
        public final @StringRes int title;
        public final @LayoutRes int featureLayout;

        HomeMenuItem(@DrawableRes int icon, @StringRes int title, @LayoutRes int featureLayout) {
            this.icon = icon;
            this.title = title;
            this.featureLayout = featureLayout;
        }
    }

    static class Adapter extends RecyclerView.Adapter<HomeItemHolder> {

        static final HomeMenuItem[] ITEMS = {
            new HomeMenuItem(R.drawable.ic_flight_plan, R.string.home_title_flight_plan,
                R.layout.feat_fpl),
            new HomeMenuItem(R.drawable.ic_map, R.string.home_title_map,
                R.layout.feat_map),
            new HomeMenuItem(R.drawable.ic_waypont_info, R.string.home_title_waypoint_info,
                R.layout.feat_waypoint),
            new HomeMenuItem(R.drawable.ic_preferred_routes, R.string.home_title_preferred_routes,
                R.layout.feat_routes),
            new HomeMenuItem(R.drawable.ic_settings, R.string.home_title_settings,
                R.layout.feat_settings),
        };

        @Override
        public HomeItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HomeItemHolder holder = new HomeItemHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feat_home_item, parent, false)
            );

            holder.itemView.setOnClickListener(v -> {

                final View newView =
                    NavigateUtil.into(parent.getContext(),
                        ITEMS[holder.getAdapterPosition()].featureLayout);

                if (newView instanceof WaypointSearchView) {
                    final WaypointSearchView waypointView = (WaypointSearchView) newView;
                    UiUtil.onAttachSubscribe(waypointView, vv ->
                        vv.result(null)
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(waypoint -> {
                              if (waypoint == null) {
                                  // hax?
                                  ((Activity) vv.getContext()).onBackPressed();
                              } else {
                                  NavigateUtil.intoWaypoint(parent.getContext(), waypoint);
                              }
                          })
                    );
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(HomeItemHolder holder, int position) {
            holder.bind(ITEMS[position]);
        }

        @Override
        public int getItemCount() {
            return ITEMS.length;
        }
    }

    static class HomeItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.label) TextView view;

        public HomeItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(HomeMenuItem item) {
            view.setText(item.title);
            view.setCompoundDrawablesWithIntrinsicBounds(0, item.icon, 0, 0);
        }
    }

    static class SpacingDecorator extends RecyclerView.ItemDecoration {

        private final int spacing;

        SpacingDecorator(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int columns = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
            int adapterPosition = parent.getChildAdapterPosition(view);

            if (adapterPosition < columns) {
                outRect.top = spacing;
            }

            if (adapterPosition % columns == 0) {
                outRect.left = spacing;
            } else if (adapterPosition % columns == columns - 1) {
                outRect.right = spacing;
            }
        }
    }
}
