package net.dhleong.opengps.feat.charts;

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
import android.widget.Toast;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class ChartPickerView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<Airport, AirportCharts.ChartInfo> {

    @Inject ChartsService service;

    @BindView(R.id.recycler) RecyclerView recycler;
    @BindView(R.id.loading) ContentLoadingProgressBar loading;

    CompositeSubscription subs = new CompositeSubscription();

    private ChartsAdapter adapter;

    public ChartPickerView(Context context) {
        super(context);
    }

    public ChartPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        App.activityComponent(this)
           .newChartPickerComponent(new ChartPickerModule())
           .inject(this);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        loading.show();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    @Override
    public Single<AirportCharts.ChartInfo> result(Airport input) {

        recycler.setAdapter(adapter = new ChartsAdapter(input));

        subs.add(
            service.getCharts(input.id())
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(result -> {
                       Timber.v("Got charts: %s", result.get(input.id()));
                       loading.hide();
                       adapter.setCharts(result);
                   }, e -> {
                       Timber.w(e);
                       Toast.makeText(getContext(), R.string.charts_error, Toast.LENGTH_SHORT).show();
                       adapter.selectedCharts.call(null);
                   })
        );

        return adapter.selectedCharts.first().toSingle();
    }

    static class ChartsAdapter extends RecyclerView.Adapter<ChartHolder> {

        final PublishRelay<AirportCharts.ChartInfo> selectedCharts = PublishRelay.create();
        final Airport input;

        List<AirportCharts.ChartInfo> charts;

        public ChartsAdapter(Airport input) {
            this.input = input;
        }

        @Override
        public ChartHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final ChartHolder holder = new ChartHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feat_charts_item, parent, false)
            );
            holder.itemView.setOnClickListener(v ->
                selectedCharts.call(charts.get(holder.getAdapterPosition())));
            return holder;
        }

        @Override
        public void onBindViewHolder(ChartHolder holder, int position) {
            holder.bind(charts.get(position));
        }

        @Override
        public int getItemCount() {
            final List<AirportCharts.ChartInfo> charts = this.charts;
            if (charts == null) return 0;
            return charts.size();
        }

        public void setCharts(AirportCharts charts) {
            if (charts == null) {
                this.charts = null;
                notifyDataSetChanged();
                return;
            }

            AirportCharts.Result result = charts.get(input.id());
            if (result == null) {
                this.charts = null;
                notifyDataSetChanged();
                return;
            }

            this.charts = result.charts;
            notifyDataSetChanged(); // lazy; won't be much changing
        }
    }

    static class ChartHolder extends RecyclerView.ViewHolder {
        TextView label;

        public ChartHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView;
        }

        public void bind(AirportCharts.ChartInfo info) {
            label.setText(info.name);
        }
    }
}
