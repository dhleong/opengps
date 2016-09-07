package net.dhleong.opengps.feat.airport.pages;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.App;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.feat.airport.AirportPageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author dhleong
 */
public class FreqsPageView
        extends RecyclerView
        implements AirportPageView {

    @Inject ConnectionDelegate connection;

    public FreqsPageView(Context context) {
        super(context);
    }

    public FreqsPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FreqsPageView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        App.activityComponent(this)
           .inject(this);

        setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void bind(Airport airport) {
        ArrayList<LabeledFrequency> freqs = new ArrayList<>();
        freqs.addAll(airport.frequencies(Airport.FrequencyType.ATIS));
        freqs.addAll(airport.frequencies(Airport.FrequencyType.DELIVERY));
        freqs.addAll(airport.frequencies(Airport.FrequencyType.GROUND));
        freqs.addAll(airport.frequencies(Airport.FrequencyType.TOWER));
        // TODO DEP/APP
        freqs.addAll(airport.frequencies(Airport.FrequencyType.OTHER));
        final int comFreqs = freqs.size();
        int actualComFreqs = comFreqs;
        freqs.addAll(airport.frequencies(Airport.FrequencyType.NAV));

        // remove unusable frequencies (the garmin does this)
        int i=0;
        for (Iterator<LabeledFrequency> iter=freqs.iterator(); iter.hasNext(); i++) {
            LabeledFrequency freq = iter.next();
            if (freq.frequency >= 200) {
                iter.remove();

                if (i < comFreqs) {
                    actualComFreqs--;
                }
            }
        }

        setAdapter(new FrequenciesAdapter(freqs, actualComFreqs));
    }

    class FrequenciesAdapter extends Adapter<FrequencyHolder> {
        private final ArrayList<LabeledFrequency> freqs;
        private final int comFreqs;

        public FrequenciesAdapter(ArrayList<LabeledFrequency> freqs, int comFreqs) {
            this.freqs = freqs;
            this.comFreqs = comFreqs;
        }

        @Override
        public FrequencyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrequencyHolder holder = new FrequencyHolder(
                LayoutInflater.from(parent.getContext())
                              .inflate(R.layout.feat_airport_page_freqs_item, parent, false)
            );

            holder.itemView.setOnClickListener(v -> {
                final int index = holder.getAdapterPosition();
                final LabeledFrequency freq = freqs.get(index);
                if (index < comFreqs) {
                    connection.setCom1Standby((float) freq.frequency);
                } else {
                    connection.setNav1Standby((float) freq.frequency);
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(FrequencyHolder holder, int position) {
            holder.bind(freqs.get(position));
        }

        @Override
        public int getItemCount() {
            return freqs.size();
        }
    }

    static class FrequencyHolder extends ViewHolder {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.freq) TextView freq;

        public FrequencyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(LabeledFrequency labeledFrequency) {
            label.setText(labeledFrequency.label);
            freq.setText(String.format(Locale.US, "%.3f", labeledFrequency.frequency));
        }
    }
}
