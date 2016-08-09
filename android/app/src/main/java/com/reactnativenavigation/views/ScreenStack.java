package com.reactnativenavigation.views;

import android.animation.LayoutTransition;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.facebook.react.ReactInstanceManager;
import com.reactnativenavigation.activities.BaseReactActivity;
import com.reactnativenavigation.core.RctManager;
import com.reactnativenavigation.core.objects.Screen;

import java.util.Stack;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ScreenStack extends android.support.design.widget.CoordinatorLayout {

    private static final int DISAPPEAR_ANIMATION_DELAY = 200;

    private static class ScreenView {
        Screen screen;
        RctView view;

        public ScreenView(Screen screen, RctView view) {
            this.screen = screen;
            this.view = view;
        }
    }

    private final Stack<ScreenView> mStack = new Stack<>();
    private final ReactInstanceManager mReactInstanceManager = RctManager.getInstance().getReactInstanceManager();
    private BaseReactActivity mReactActivity;

    public ScreenStack(BaseReactActivity context) {
        super(context);
        init(context);
    }

    public ScreenStack(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mReactActivity = (BaseReactActivity) context;
        setLayoutTransition(new LayoutTransition());
    }

    public void push(Screen screen) {
        push(screen, null);
    }

    public void push(Screen screen, RctView.OnDisplayedListener onDisplayed) {
        RctView oldView = mStack.isEmpty() ? null : mStack.peek().view;
        RctView view = new RctView(mReactActivity, mReactInstanceManager, screen, onDisplayed);
        if (oldView != null) {
            addView(view, MATCH_PARENT, MATCH_PARENT);

            oldView.onTemporallyRemovedFromScreen();
            getLayoutTransition().setStartDelay(LayoutTransition.DISAPPEARING, DISAPPEAR_ANIMATION_DELAY);
            removeView(oldView);
            getLayoutTransition().setStartDelay(LayoutTransition.DISAPPEARING, 0);
        } else {
            addView(view, MATCH_PARENT, MATCH_PARENT);
        }
        mStack.push(new ScreenView(screen, view));
    }

    public Screen pop() {
        if (mStack.isEmpty() || getStackSize() == 1) {
            return null;
        }

        ScreenView popped = mStack.pop();

        RctView newView = mStack.peek().view;
        addView(newView);
        newView.onReAddToScreen();

        popped.view.onRemoveFromScreen();
        removeView(popped.view);
        return popped.screen;
    }

    public Screen popToRoot() {
        if (mStack.isEmpty() || getStackSize() <= 1) {
            return null;
        }

        ScreenView oldScreenView = null;
        while (getStackSize() > 1) {
            ScreenView popped = mStack.pop();
            popped.view.onRemoveFromScreen();
            removeView(popped.view);
            if (oldScreenView == null) {
                oldScreenView = popped;
            }
        }

        if (!mStack.isEmpty()) {
            addView(mStack.peek().view, 0);
        }

        return oldScreenView != null ? oldScreenView.screen : null;
    }

    public Screen resetTo(Screen screen) {
        return resetTo(screen, null);
    }

    public Screen resetTo(Screen screen, RctView.OnDisplayedListener onDisplayed) {
        RctView view = new RctView(mReactActivity, mReactInstanceManager, screen, onDisplayed);
        addView(view, MATCH_PARENT, MATCH_PARENT);

        final List<ScreenView> oldScreenViews = new ArrayList<>();
        if (!mStack.isEmpty()) {
            while (getStackSize() > 0) {
                ScreenView popped = mStack.pop();
                oldScreenViews.add(popped);
            }
        }

        // Add screen to stack after it's clear
        this.push(screen, new RctView.OnDisplayedListener() {
            @Override
            public void onDisplayed() {
                for ( ScreenView screenView : oldScreenViews ) {
                    screenView.view.onRemoveFromScreen();
                    // Enabling this next line causes screen flicker. I believe the view is already
                    // removed from  the push and not needed - RKH
                    //removeView(screenView.view);
                }
            }
        });

        return null;
    }

    public boolean isEmpty() {
        return mStack.isEmpty();
    }

    public int getStackSize() {
        return mStack.size();
    }

    public Screen peek() {
        return mStack.peek().screen;
    }

    /**
     * Remove the ScreenStack from {@code parent} while preventing all child react views from getting unmounted
     */
    public void removeFromScreen(ViewGroup parent) {
        mStack.peek().view.onTemporallyRemovedFromScreen();

        parent.removeView(this);
    }

    /**
     * Add ScreenStack to {@code parent}
     */
    public void addToScreen(ViewGroup parent) {
        mStack.peek().view.onReAddToScreen();

        parent.addView(this, new CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    public void removeAllReactViews() {
        while (!mStack.empty()) {
            RctView view = mStack.pop().view;
            // Ensure view will be properly detached and unmounted
            view.onRemoveFromScreen();
            // Unmount the view
            view.detachReactRootView(mReactInstanceManager);
        }
        removeAllViews();
    }
}
