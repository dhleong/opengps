package net.dhleong.opengps.feat.fpl;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.App;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import timber.log.Timber;

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
        App.activityComponent(this)
           .newFlightPlannerComponent()
           .inject(this);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        presenter.onViewCreated(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.onViewAttached(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.onViewDetached(this);
    }

    public void setRoute(GpsRoute route) {
        adapter.setRoute(route);
    }

    public Observable<Void> addWaypointEvents() {
        return adapter.addWaypointEvents;
    }

    public Observable<AeroObject> removeWaypointEvents() {
        return adapter.removeWaypointEvents;
    }

    public Observable<AeroObject> removeAfterWaypointEvents() {
        return adapter.removeAfterWaypointEvents;
    }

    public Observable<AeroObject> loadAirwayEvents() {
        return adapter.loadAirwayEvents;
    }

    public Observable<AeroObject> viewWaypointEvents() {
        return adapter.viewWaypointEvents;
    }

    static class Adapter extends RecyclerView.Adapter<FPLItemHolder> {

        private GpsRoute route;
        public PublishRelay<Void> addWaypointEvents = PublishRelay.create();
        public PublishRelay<AeroObject> removeWaypointEvents = PublishRelay.create();
        public PublishRelay<AeroObject> removeAfterWaypointEvents = PublishRelay.create();
        public PublishRelay<AeroObject> viewWaypointEvents = PublishRelay.create();
        public PublishRelay<AeroObject> loadAirwayEvents = PublishRelay.create();

        @Inject Adapter() {}

        @Override
        public FPLItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                                            .inflate(viewType, parent, false);

            switch (viewType) {
            default:
            case R.layout.feat_fpl_item_add:
                view.setOnClickListener(v -> {
                    Timber.v("Add new waypoint!");
                    addWaypointEvents.call(null);
                });
                return new FPLItemHolder(view);

            case R.layout.feat_fpl_item_fix:
                FixHolder holder = new FixHolder(view);
                // show a menu of options:
                view.setOnClickListener(v -> {
                    ListPopupWindow win = popupFor(this,
                        parent.getContext(),
                        route.step(holder.getAdapterPosition()).ref);
                    win.setAnchorView(v);
                    win.show();
                });
                return holder;

            case R.layout.feat_fpl_item_bearing_to:
                return new BearingHolder(view);

            case R.layout.feat_fpl_item_airway:
            case R.layout.feat_fpl_item_airway_exit:
                return new AirwayHolder(view);
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
                return R.layout.feat_fpl_item_add;
            }

            final GpsRoute.Step step = route.step(position);
            switch (step.type) {
            default:
            case FIX: return R.layout.feat_fpl_item_fix;
            case BEARING_TO: return R.layout.feat_fpl_item_bearing_to;
            case AIRWAY: return R.layout.feat_fpl_item_airway;
            case AIRWAY_EXIT: return R.layout.feat_fpl_item_airway_exit;
            }
        }

        public void setRoute(GpsRoute route) {
            final GpsRoute oldRoute = this.route;
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
                final GpsRoute oldRoute = this.oldRoute;
                if (oldRoute == null) return 0;
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
            bearing.setText(String.format(Locale.US, "%d\u00b0", Math.round(step.bearing)));
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

    static class AirwayHolder extends FPLItemHolder {
        TextView label;

        public AirwayHolder(View view) {
            super(view);
            label = (TextView) view;
        }

        @Override
        public void bind(GpsRoute.Step step) {
            label.setText(step.ref.name());
        }
    }

    static ListPopupWindow popupFor(Adapter adapter, Context context, AeroObject ref) {
        final List<CharSequence> items = new ArrayList<>(3);
        items.add(context.getString(R.string.fpl_waypoint_remove));
        items.add(context.getString(R.string.fpl_waypoint_remove_after));
        items.add(context.getString(R.string.fpl_waypoint_info));
        if (!(ref instanceof Airport)) {
            items.add(context.getString(R.string.fpl_waypoint_load_airway));
        }

        ListPopupWindow win = new ListPopupWindow(context);
        win.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
            items));
        win.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
            case 0: adapter.removeWaypointEvents.call(ref); break;
            case 1: adapter.removeAfterWaypointEvents.call(ref); break;
            case 2: adapter.viewWaypointEvents.call(ref); break;
            case 3: adapter.loadAirwayEvents.call(ref); break;
            }

            win.dismiss();
        });

        return win;
    }

}
