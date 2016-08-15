package com.reactnativenavigation.controllers;

import android.support.annotation.Nullable;

import com.reactnativenavigation.activities.BaseReactActivity;
import com.reactnativenavigation.core.objects.Screen;
import com.reactnativenavigation.modal.RnnModal;
import com.reactnativenavigation.utils.ContextProvider;
import com.reactnativenavigation.utils.RefUtils;

import java.lang.ref.WeakReference;
import java.util.Stack;

/**
 * Created by guyc on 06/05/16.
 */
public class ModalController {
    private static ModalController sInstance;

    private final Stack<WeakReference<RnnModal>> mModals;

    private ModalController() {
        mModals = new Stack<>();
    }

    public static synchronized ModalController getInstance() {
        if (sInstance == null) {
            sInstance = new ModalController();
        }

        return sInstance;
    }

    public void add(RnnModal modal) {
        mModals.add(new WeakReference<>(modal));
    }

    public boolean isModalDisplayed() {
        return mModals.size() != 0;
    }

    @Nullable
    public RnnModal get() {
        return isModalDisplayed() ? RefUtils.get(mModals.peek()) : null;
    }

    public void remove() {
        if (isModalDisplayed()) {
            mModals.pop();
        }

        if (!isModalDisplayed()) {
            // After modal is dismissed, update Toolbar with screen from parent activity or previously displayed modal
            BaseReactActivity context = ContextProvider.getActivityContext();
            if (context != null) {
                Screen currentScreen = context.getCurrentScreen();
                context.updateStyle(currentScreen);
            }
        }
    }

    public void dismissAllModals() {
        while (isModalDisplayed()) {
            dismissModal();
        }
    }

    public void dismissModal() {
        RnnModal modal = this.get();
        if (modal != null) {
            modal.dismiss();
        }
    }

    public void removeAllReactViews() {
        while (isModalDisplayed()) {
            RnnModal modal = RefUtils.get(mModals.pop());
            modal.removeAllReactViews();
        }
    }
}
