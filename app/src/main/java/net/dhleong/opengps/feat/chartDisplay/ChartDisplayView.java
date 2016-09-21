package net.dhleong.opengps.feat.chartDisplay;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.AttributeSet;

import com.github.barteksc.pdfviewer.PDFView;

import net.dhleong.opengps.App;
import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class ChartDisplayView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<ChartInfo, Void> {

    Pattern pdfPattern = Pattern.compile("src=\"(.*PDF)\"");

    @Inject OkHttpClient client;

    @BindView(R.id.chart) PDFView chart;
    @BindView(R.id.loading) ContentLoadingProgressBar loading;

    CompositeSubscription subs = new CompositeSubscription();

    public ChartDisplayView(Context context) {
        super(context);
    }

    public ChartDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        App.component(getContext())
           .inject(this);
    }

    @Override
    public Single<Void> result(ChartInfo input) {

        File chartFile = new File(getContext().getCacheDir(), "last-chart.pdf");
        //noinspection ResultOfMethodCallIgnored
        chartFile.delete();

        loading.show();

        subs.add(
            newCall(input.url)
            .subscribeOn(Schedulers.io())
            .flatMap(resp -> Observable.fromCallable(() -> {
                BufferedSink sink = Okio.buffer(Okio.sink(chartFile));
                sink.writeAll(resp.body().source());
                sink.close();
                return chartFile;
            })).observeOn(AndroidSchedulers.mainThread())
               .subscribe(file -> {

                Timber.v("Downloaded chat to %s", file);
                chart.fromFile(file)
                     .enableDoubletap(true)
                     .onLoad(pagesLoaded -> {
                         // TODO
                         Timber.v("loaded %d", pagesLoaded);
                         loading.hide();
                     })
                     .load();

            }, e -> {
                // TODO something else?
                Timber.w(e, "Error loading chart!");
            })
        );

        return Observable.<Void>empty().toSingle();
    }

    Observable<Response> newCall(String url) {
        return Observable.fromCallable(() -> client.newCall(
            new Request.Builder()
                .url(url)
                .cacheControl(
                    new CacheControl.Builder()
                        .maxAge(30, TimeUnit.DAYS)
                        .build()
                )
                .build()
        ).execute());
    }
}
