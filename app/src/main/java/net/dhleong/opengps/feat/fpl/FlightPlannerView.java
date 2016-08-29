package net.dhleong.opengps.feat.fpl;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.App;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * @author dhleong
 */
public class FlightPlannerView extends CoordinatorLayout {

    @Inject Adapter adapter;
    @Inject FlightPlannerPresenter presenter;

    @BindView(R.id.recycler) RecyclerView recycler;

    public FlightPlannerView(Context context) {
        super(context);
    }

    public FlightPlannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlightPlannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.component(getContext())
           .newFlightPlannerComponent()
           .inject(this);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        presenter.onViewCreated(this);
    }

    public void setRoute(GpsRoute route) {
        adapter.setRoute(route);
    }

    public Observable<Void> addWaypointEvents() {
        return adapter.addWaypointEvents;
    }

    public Observable<AeroObject> loadAirwayEvents() {
        return Observable.empty(); // TODO
    }

    static class Adapter extends RecyclerView.Adapter<FPLItemHolder> {

        private GpsRoute route;
        public PublishRelay<Void> addWaypointEvents = PublishRelay.create();

        @Inject Adapter() {}

        @Override
        public FPLItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FPLItemHolder holder = new FPLItemHolder(
                LayoutInflater.from(parent.getContext())
                              .inflate(viewType, parent, false));

            if (viewType == R.layout.fpl_item_add) {
                holder.itemView.setOnClickListener(v -> addWaypointEvents.call(null));
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(FPLItemHolder holder, int position) {
            // TODO
        }

        @Override
        public int getItemCount() {
            return 1; // TODO
        }

        @Override
        public int getItemViewType(int position) {
            final GpsRoute route = this.route;
            if (route == null || position >= route.size()) {
                return R.layout.fpl_item_add;
            }

            // TODO step types
            return R.layout.fpl_item_add;
        }

        public void setRoute(GpsRoute route) {
            GpsRoute oldRoute = this.route;
            this.route = route;

            DiffUtil.calculateDiff(new DiffCallback(oldRoute, route))
                    .dispatchUpdatesTo(this);
        }

        static class DiffCallback extends DiffUtil.Callback {
            private final GpsRoute oldRoute;
            private final GpsRoute newRoute;

            public DiffCallback(GpsRoute oldRoute, GpsRoute newRoute) {
                this.oldRoute = oldRoute;
                this.newRoute = newRoute;
            }

            @Override
            public int getOldListSize() {
                return oldRoute.size();
            }

            @Override
            public int getNewListSize() {
                return newRoute.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return areContentsTheSame(oldItemPosition, newItemPosition);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldRoute.steps().get(oldItemPosition)
                               .equals(newRoute.steps().get(newItemPosition));
            }
        }
    }

    static class FPLItemHolder extends RecyclerView.ViewHolder {
        public FPLItemHolder(View view) {
            super(view);
        }
    }
}
