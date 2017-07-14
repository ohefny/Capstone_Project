package com.example.mvpframeworkedited;

/**
 * Creates a Presenter object.
 * @param <T> presenter type
 */
public interface PresenterFactory<T extends BasePresenter> {
    T create();
}
