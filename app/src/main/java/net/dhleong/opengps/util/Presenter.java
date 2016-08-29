package net.dhleong.opengps.util;

/**
 * @author dhleong
 */
public interface Presenter<T> {

    void onViewCreated(T view);
    void onViewAttached(T view);
    void onViewDetached(T view);
}
