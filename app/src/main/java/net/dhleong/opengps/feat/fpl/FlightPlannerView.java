package net.dhleong.opengps.feat.fpl;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dhleong.opengps.App;
import net.dhleong.opengps.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class FlightPlannerView extends CoordinatorLayout {

    @Inject Adapter adapter;

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
    }

    static class Adapter extends RecyclerView.Adapter<FPLItemHolder> {

        @Inject Adapter() {}

        @Override
        public FPLItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FPLItemHolder holder = new FPLItemHolder(
                LayoutInflater.from(parent.getContext())
                              .inflate(viewType, parent, false));
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
            return R.layout.fpl_item_add;
        }
    }

    static class FPLItemHolder extends RecyclerView.ViewHolder {
        public FPLItemHolder(View view) {
            super(view);
        }
    }
}
