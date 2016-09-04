package net.dhleong.opengps.feat.home;

import android.content.Context;
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

import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.NavigateUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class HomeView extends CoordinatorLayout {

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

        recycler.setLayoutManager(new GridLayoutManager(
            getContext(), 2
        ));
        recycler.setAdapter(adapter = new Adapter());
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
//            new HomeMenuItem(R.drawable.ic_settings, R.string.home_title_settings,
//                R.layout.feat_settings),
        };

        @Override
        public HomeItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HomeItemHolder holder = new HomeItemHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feat_home_item, parent, false)
            );

            holder.itemView.setOnClickListener(v ->
                NavigateUtil.into(parent.getContext(),
                    ITEMS[holder.getAdapterPosition()].featureLayout));

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

        TextView view;

        public HomeItemHolder(View itemView) {
            super(itemView);

            view = (TextView) itemView;
        }

        public void bind(HomeMenuItem item) {
            view.setText(item.title);
            view.setCompoundDrawablesWithIntrinsicBounds(0, item.icon, 0, 0);
        }
    }
}
