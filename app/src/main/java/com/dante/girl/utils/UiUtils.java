package com.dante.girl.utils;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;


/**
 * Util to show hint such as snackBar or dialog.
 */
public class UiUtils{
    private static final String TAG = "UiUtils";
    public static void showSnack(View rootView, int textId) {
        if (null != rootView) {
            Snackbar.make(rootView, textId, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void showSnack(View rootView, String text) {
        if (null != rootView) {
            Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void showSnackLong(View rootView, int textId) {
        if (null != rootView) {
            Snackbar.make(rootView, textId, Snackbar.LENGTH_LONG).show();
        }
    }

    public static void showSnackLong(View rootView, int textId, int actionTextId, View.OnClickListener onClickListener) {
        if (null != rootView) {
            Snackbar.make(rootView, textId, Snackbar.LENGTH_LONG).setAction(actionTextId, onClickListener).show();
        }
    }
    public static void showSnackLong(View rootView, String text, int actionTextId, View.OnClickListener onClickListener) {
        if (null != rootView) {
            Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).setAction(actionTextId, onClickListener).show();
        }
    }

    public static void showSnack(View rootView, int textId, int actionTextId, View.OnClickListener onClickListener) {
        if (null != rootView) {
            Snackbar.make(rootView, textId, Snackbar.LENGTH_LONG).setAction(actionTextId, onClickListener).show();
        }
    }

    public static void showSnackLong(View rootView, String text) {
        if (null != rootView) {
            Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show();
        }
    }

    public static void sleep(int seconds) {
        try {
            Log.i(TAG, "Start sleep...");
            Thread.sleep(seconds * 1000);
            Log.i(TAG, "End sleep.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
