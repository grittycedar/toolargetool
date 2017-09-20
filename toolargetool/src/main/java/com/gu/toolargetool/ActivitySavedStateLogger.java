package com.gu.toolargetool;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link android.app.Application.ActivityLifecycleCallbacks} implementation that logs information
 * about the saved state of Activities.
 */
public class ActivitySavedStateLogger extends EmptyActivityLifecycleCallbacks {
    @NonNull Formatter formatter;
    @NonNull Logger logger;
    @Nullable private final FragmentSavedStateLogger fragmentLogger;
    @NonNull private final Map<Activity, Bundle> savedStates = new HashMap<>();

    public ActivitySavedStateLogger(@NonNull Formatter formatter, @NonNull Logger logger, @Nullable FragmentSavedStateLogger fragmentLogger) {
        this.formatter = formatter;
        this.logger = logger;
        this.fragmentLogger = fragmentLogger;
    }

    public ActivitySavedStateLogger(@NonNull Formatter formatter, @NonNull Logger logger, boolean logFragments) {
        this(formatter, logger, logFragments ? new FragmentSavedStateLogger(formatter, logger) : null);
    }

    public ActivitySavedStateLogger(boolean logFragments) {
        this(Formatter.DEFAULT_FORMATTER, Logger.DEFAULT_LOGGER, logFragments);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof FragmentActivity && fragmentLogger != null) {
            ((FragmentActivity) activity)
                    .getSupportFragmentManager()
                    .registerFragmentLifecycleCallbacks(fragmentLogger, true);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof FragmentActivity && fragmentLogger != null) {
            ((FragmentActivity) activity)
                    .getSupportFragmentManager()
                    .unregisterFragmentLifecycleCallbacks(fragmentLogger);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        savedStates.put(activity, outState);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Bundle savedState = savedStates.remove(activity);
        if (savedState != null) {
            try {
                String message = formatter.format(activity, savedState);
                logger.log(message);
            } catch (RuntimeException e) {
                logger.logException(e);
            }
        }
    }
}
