package net.dhleong.opengps.feat.navfix;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
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
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.NavigateUtil;
import net.dhleong.opengps.ui.TextUtil;
import net.dhleong.opengps.ui.WaypointHeaderView;
import net.dhleong.opengps.views.MorseView;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class NavFixInfoView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<NavFix, Void> {

    @Inject ConnectionDelegate conn;

    @BindView(R.id.waypoint) WaypointHeaderView headerView;
    @BindView(R.id.recycler) RecyclerView recycler;

    CompositeSubscription subs = new CompositeSubscription();

    public NavFixInfoView(Context context) {
        super(context);
    }

    public NavFixInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavFixInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .inject(this);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public Single<Void> result(NavFix input) {

        headerView.bind(input);

        Adapter adapter = new Adapter(input.references());
        recycler.setAdapter(adapter);

        subs.add(
            adapter.selectWaypointEvents.observeOn(AndroidSchedulers.mainThread())
            .subscribe(fix -> {
                // this belongs in a presenter!
                if (fix instanceof Navaid) {
                    conn.setNav1Standby((float) ((Navaid) fix).freq());
                } else {
                    NavigateUtil.intoWaypoint(getContext(), fix);
                }
            })
        );

        // just something that never auto-completes
        return Observable.<Void>never().toSingle();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    static class Adapter extends RecyclerView.Adapter<RefHolder> {
        PublishRelay<AeroObject> selectWaypointEvents = PublishRelay.create();

        private final List<NavFix.Reference> references;

        public Adapter(@NonNull List<NavFix.Reference> references) {
            this.references = references;
        }

        @Override
        public RefHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RefHolder holder = new RefHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feat_navfix_item, parent, false)
            );
            holder.itemView.setOnClickListener(v ->
                selectWaypointEvents.call(references.get(holder.getAdapterPosition()).obj)
            );
            return holder;
        }

        @Override
        public void onBindViewHolder(RefHolder holder, int position) {
            holder.bind(references.get(position));
        }

        @Override
        public int getItemCount() {
            return references.size();
        }
    }

    static class RefHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.id) TextView id;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.freq) TextView freq;
        @BindView(R.id.ident) MorseView ident;

        @BindView(R.id.radial) TextView radial;
        @BindView(R.id.distance) TextView distance;

        public RefHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(NavFix.Reference reference) {
            id.setText(reference.obj.id());
            radial.setText(String.format(Locale.US, "R%03.0f", reference.bearingFrom));

            if (reference.distance > 0) {
                distance.setText(String.format(Locale.US, "%.0f NM", reference.distance));
                distance.setVisibility(VISIBLE);
            } else {
                distance.setVisibility(GONE);
            }

            if (reference.obj instanceof NavFix) {
                name.setText(null);
            } else {
                name.setText(reference.obj.name());
            }

            if (reference.obj instanceof Navaid) {
                freq.setText(TextUtil.formatFreq(((Navaid) reference.obj).freq()));
                ident.setText(reference.obj.id());

                freq.setVisibility(VISIBLE);
                ident.setVisibility(VISIBLE);
            } else {
                freq.setVisibility(GONE);
                ident.setVisibility(GONE);
            }
        }
    }
}
