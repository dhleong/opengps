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
import android.widget.TextView;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.App;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.R;

import java.util.Locale;

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
        App.component(this)
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
        return adapter.loadAirwayEvents;
    }

    static class Adapter extends RecyclerView.Adapter<FPLItemHolder> {

        private GpsRoute route;
        public PublishRelay<Void> addWaypointEvents = PublishRelay.create();
        public PublishRelay<AeroObject> loadAirwayEvents = PublishRelay.create();

        @Inject Adapter() {}

        @Override
        public FPLItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                                            .inflate(viewType, parent, false);

            switch (viewType) {
            default:
            case R.layout.fpl_item_add:
                view.setOnClickListener(v -> addWaypointEvents.call(null));
                return new FPLItemHolder(view);

            case R.layout.fpl_item_fix:
                // TODO menu of options, actually
                FixHolder holder = new FixHolder(view);
                view.setOnClickListener(v ->
                    loadAirwayEvents.call(
                        route.step(holder.getAdapterPosition()).ref
                    ));
                return holder;

            case R.layout.fpl_item_bearing_to:
                return new BearingHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(FPLItemHolder holder, int position) {
            final GpsRoute route = this.route;
            if (route != null && position < route.size()) {
                holder.bind(route.step(position));
            }
        }

        @Override
        public int getItemCount() {
            final GpsRoute route = this.route;
            final int routeSize = route == null
                ? 0
                : route.size();
            return routeSize + 1;
        }

        @Override
        public int getItemViewType(int position) {
            final GpsRoute route = this.route;
            if (route == null || position >= route.size()) {
                return R.layout.fpl_item_add;
            }

            GpsRoute.Step step = route.step(position);
            switch (step.type) {
            default:
            case FIX: return R.layout.fpl_item_fix;

            // TODO actual layouts:
            case BEARING_TO:
            case BEARING_FROM:
                return R.layout.fpl_item_bearing_to;
            }
        }

        public void setRoute(GpsRoute route) {
            GpsRoute oldRoute = this.route;
            this.route = route.copy();

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
                return oldRoute.step(oldItemPosition)
                               .equals(newRoute.step(newItemPosition));
            }
        }
    }

    static class FPLItemHolder extends RecyclerView.ViewHolder {
        public FPLItemHolder(View view) {
            super(view);
        }

        public void bind(GpsRoute.Step step) {
            // nop
        }
    }

    static class BearingHolder extends FPLItemHolder {

        @BindView(R.id.bearing) TextView bearing;
        @BindView(R.id.distance) TextView distance;

        public BearingHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bind(GpsRoute.Step step) {
            // FIXME degree symbol; remove @
            bearing.setText(String.format(Locale.US, "%.1f", step.bearing));
            distance.setText(String.format(Locale.US, " @ %.1f nm", step.distance));
        }
    }

    static class FixHolder extends FPLItemHolder {

        @BindView(R.id.id) TextView id;
        @BindView(R.id.freq) TextView freq;

        public FixHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bind(GpsRoute.Step step) {
            // TODO name, ident morse, ...
            id.setText(step.ref.id());

            if (step.ref instanceof Navaid) {
                freq.setText(String.format(Locale.US, "%.2f", ((Navaid) step.ref).freq()));
                freq.setVisibility(VISIBLE);
            } else {
                freq.setVisibility(GONE);
            }
        }
    }
}
