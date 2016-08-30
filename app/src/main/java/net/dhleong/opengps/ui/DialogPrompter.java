package net.dhleong.opengps.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

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

        new AlertDialog.Builder(context)
            .setTitle(title)
            .setOnDismissListener(dialog -> result.onCompleted())
            .setItems(items, (dialog, which) -> {
                result.onNext(candidates.get(which));
                dialog.dismiss();
            })
            .show();

        return result;
    }

    public static <R, T, V extends PrompterView<T, R>> Observable<R> prompt(
            Context context, Class<V> viewClass, @LayoutRes int res, T input) {
        final View v = LayoutInflater.from(context).inflate(res, null);
        if (!viewClass.isAssignableFrom(v.getClass())) {
            throw new IllegalArgumentException("Inflated " + v + " but expected " + viewClass);
        }

        final V prompterView = viewClass.cast(v);
        return prompt(prompterView, input);
    }

    public static <R, T, V extends PrompterView<T, R>> Observable<R> prompt(V view, T input) {

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

        view.result(input).subscribe(result -> {
            results.onNext(result);
            dialog.dismiss();
        }, error -> dialog.dismiss());

        return results;
    }
}
