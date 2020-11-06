package com.sofar.utility;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ToastUtil {

  public static void startShort(@NonNull Context context, String text) {
    innerToast(context, text, Toast.LENGTH_SHORT);
  }

  public static void startLong(@NonNull Context context, String text) {
    innerToast(context, text, Toast.LENGTH_LONG);
  }

  private static void innerToast(@NonNull Context context, String text, int duration) {
    Toast toast = Toast.makeText(context, null, duration);
    toast.setText(text);
    toast.show();
  }
}
