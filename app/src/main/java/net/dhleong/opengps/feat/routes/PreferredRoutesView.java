package net.dhleong.opengps.feat.routes;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.App;
import net.dhleong.opengps.PreferredRoute;
import net.dhleong.opengps.R;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * @author dhleong
 */
public class PreferredRoutesView extends CoordinatorLayout {

    @Inject Adapter adapter;
    @Inject PreferredRoutesPresenter presenter;

    @BindView(R.id.recycler) RecyclerView recycler;
    @BindView(R.id.loading) ContentLoadingProgressBar loading;
    @BindView(R.id.empty) View emptyView;

    public PreferredRoutesView(Context context) {
        super(context);
    }

    public PreferredRoutesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferredRoutesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .newPreferredRoutesComponent()
           .inject(this);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        if (!isInEditMode()) presenter.onViewCreated(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) presenter.onViewAttached(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) presenter.onViewDetached(this);
    }

    public Observable<Void> selectDestRequests() {
        return adapter.selectDestRequests;
    }

    public Observable<Void> selectOriginRequests() {
        return adapter.selectOriginRequests;
    }

    public Observable<PreferredRoute> selectedRoutes() {
        return adapter.selectedRoutes;
    }

    public void setDestination(Airport airport) {
        adapter.setDestination(airport);
    }

    public void setOrigin(Airport airport) {
        adapter.setOrigin(airport);
    }

    public void setRoutes(List<PreferredRoute> routes) {
        adapter.setRoutes(routes);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            loading.show();
        } else {
            loading.hide();
        }
    }

    public void setEmpty(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? VISIBLE : GONE);
    }

    static class Adapter extends RecyclerView.Adapter<BaseViewHolder> {
        PublishRelay<Void> selectOriginRequests = PublishRelay.create();
        PublishRelay<Void> selectDestRequests = PublishRelay.create();
        PublishRelay<PreferredRoute> selectedRoutes = PublishRelay.create();

        List<PreferredRoute> routes;
        Airport origin, dest;

        @Inject Adapter() {}

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                                            .inflate(viewType, parent, false);
            if (viewType == R.layout.feat_routes_header) {
                return new HeaderHolder(view, this);
            }

            RouteHolder holder = new RouteHolder(view);
            holder.itemView.setOnClickListener(v ->
                selectedRoutes.call(routes.get(holder.getAdapterPosition() - 1)));
            return holder;
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.bind(this, (position == 0 ? null : routes.get(position - 1)));
        }

        @Override
        public int getItemCount() {
            List<PreferredRoute> routes = this.routes;
            return 1 + (routes == null ? 0 : routes.size());
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return R.layout.feat_routes_header;
            return R.layout.feat_routes_item;
        }

        public void setDestination(Airport airport) {
            this.dest = airport;
            notifyItemChanged(0);
        }

        public void setOrigin(Airport airport) {
            this.origin = airport;
            notifyItemChanged(0);
        }

        public void setRoutes(List<PreferredRoute> routes) {
            final boolean hadRoutes = this.routes != null;

            this.routes = routes;

            if (hadRoutes) {
                // remove + add is probably not worth it...
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(1, routes.size());
            }
        }

    }

    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bind(Adapter adapter, PreferredRoute route);
    }

    static class HeaderHolder extends BaseViewHolder {

        @BindView(R.id.origin) TextView origin;
        @BindView(R.id.dest) TextView dest;

        public HeaderHolder(View view, Adapter adapter) {
            super(view);
            ButterKnife.bind(this, view);

            origin.setOnClickListener(v -> adapter.selectOriginRequests.call(null));
            dest.setOnClickListener(v -> adapter.selectDestRequests.call(null));
        }

        @Override
        void bind(Adapter adapter, PreferredRoute route) {
            Airport originAirport = adapter.origin;
            if (originAirport != null) {
                origin.setText(originAirport.id());
                origin.setSelected(true);
            }

            Airport destAirport = adapter.dest;
            if (destAirport != null) {
                dest.setText(destAirport.id());
                dest.setSelected(true);
            }
        }
    }

    static class RouteHolder
        extends BaseViewHolder {

        @BindView(R.id.altitude) TextView altitude;
        @BindView(R.id.route) TextView routeString;

        public RouteHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        void bind(Adapter adapter, PreferredRoute route) {
            altitude.setText(route.altitude);
            routeString.setText(route.routeString);
        }
    }
}
