package net.dhleong.opengps.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import net.dhleong.opengps.modules.PrefsModule;

import java.util.List;

import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class DialogPrompter {
    public interface PrompterView<T, R> {
        Single<R> result(T input);
    }

    public static <R> Observable<R> prompt(
            Context context, CharSequence title,
            List<R> candidates,
            Func1<R, CharSequence> describer) {
        final int len = candidates.size();
        final CharSequence[] items = new CharSequence[len];
        for (int i=0; i < len; i++) {
            items[i] = describer.call(candidates.get(i));
        }

        PublishSubject<R> result = PublishSubject.create();

        setKeepScreenOn(
            new AlertDialog.Builder(context)
                .setTitle(title)
                .setOnDismissListener(dialog -> result.onCompleted())
                .setItems(items, (dialog, which) -> {
                    result.onNext(candidates.get(which));
                    dialog.dismiss();
                })
                .show()
        );

        return result;
    }

    public static <R, T, V extends PrompterView<T, R>> Observable<R> prompt(
            Context context, Class<V> viewClass, @LayoutRes int res, T input) {
        Timber.v("prompt via %s <- %s...", viewClass, input);
        final View v = LayoutInflater.from(context).inflate(res, null);
        if (!viewClass.isAssignableFrom(v.getClass())) {
            throw new IllegalArgumentException("Inflated " + v + " but expected " + viewClass);
        }

        final V prompterView = viewClass.cast(v);
        return prompt(prompterView, input);
    }

    public static <R, T, V extends PrompterView<T, R>> Observable<R> prompt(V view, T input) {

        Timber.v("prompt via %s <- %s", view, input);

        if (!(view instanceof View)) {
            throw new IllegalArgumentException(view + " is not actually a View");
        }
        View v = (View) view;

        final PublishSubject<R> results = PublishSubject.create();

        final AlertDialog dialog =
            new AlertDialog.Builder(v.getContext())
                .setView(v)
                .setOnDismissListener(any -> results.onCompleted())
                .show();

        view.result(input)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(result -> {
                Timber.v("Got %s; dismiss dialog", result);
                results.onNext(result);
                dialog.dismiss();
        }, error -> dialog.dismiss());

        setKeepScreenOn(dialog);

        return results;
    }

    private static void setKeepScreenOn(AlertDialog dialog) {
        boolean keepScreenOn = PrefsModule.get(dialog.getContext())
                                          .getBoolean(PrefsModule.PREF_KEEP_SCREEN, false);
        if (keepScreenOn) {
            Window w = dialog.getWindow();
            if (w == null) {
                Timber.w("Dialog didn't have a window; can't set KEEP_SCREEN_ON");
            } else {
                w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Timber.v("setKeepScreenOn(%s, true)", w);
            }
        }
    }
}
