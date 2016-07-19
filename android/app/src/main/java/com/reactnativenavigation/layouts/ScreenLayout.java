package com.reactnativenavigation.layouts;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.facebook.react.ReactInstanceManager;
import com.reactnativenavigation.core.Button;
import com.reactnativenavigation.views.ContentView;
import com.reactnativenavigation.views.ScrollDirectionListener;
import com.reactnativenavigation.views.TitleBar;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ScreenLayout extends LinearLayout implements ScrollDirectionListener.OnScrollChanged {

    public static class Params {
        String moduleName;
        Bundle passProps;
        List<Button> buttons;
    }

    private final ReactInstanceManager reactInstanceManager;
    private final String moduleName;
    private final Bundle passProps;
    private final List<Button> buttons;
    private ContentView contentView;
    private TopBar topBar;

    public ScreenLayout(Context context, ReactInstanceManager reactInstanceManager, Params screenParams) {
        super(context);
        this.reactInstanceManager = reactInstanceManager;
        moduleName = screenParams.moduleName;
        passProps = screenParams.passProps;
        buttons = screenParams.buttons;
        setOrientation(VERTICAL);

        createViews();
    }

    private void createViews() {
        addTopBar();
        addTitleBarAndSetButtons();
        addContentView();
    }

    private void addTitleBarAndSetButtons() {
        TitleBar titleBar = new TitleBar(getContext());
        titleBar.setButtons(buttons);
        topBar.addView(titleBar);
    }

    private void addTopBar() {
        topBar = new TopBar(getContext());
        addView(topBar, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    private void addContentView() {
        contentView = new ContentView(getContext(), reactInstanceManager, moduleName, passProps, this);
        addView(contentView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    public void onScrollChanged(ScrollDirectionListener.Direction direction) {

    }
}