package com.sofar.utility.statusbar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 设置状态栏的颜色，依赖了SystemBarTintManager类
 */
public class StatusBarUtil {

  private Activity activity;


  public StatusBarUtil(Activity activity) {
    this.activity = activity;
  }

  /**
   * 设置状态栏透明
   */
  public void setStatusBarTransparent() {
    setStatusBarColor(Color.TRANSPARENT);
  }

  /**
   * 设置状态栏的颜色
   *
   * @param color
   */
  public void setStatusBarColor(int color) {
    // 4.4-5.0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
      && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      setTranslucentStatus(true);
      SystemBarTintManager tintManager = new SystemBarTintManager(activity);
      tintManager.setStatusBarTintEnabled(true);
      tintManager.setStatusBarTintResource(color);
      tintManager.setStatusBarTintColor(color);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 5.0 全透明实现
      Window window = activity.getWindow();
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      window.getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(color);
    }
  }


  @TargetApi(19)
  private void setTranslucentStatus(boolean on) {

    WindowManager.LayoutParams winParams = activity.getWindow().getAttributes();
    final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
    if (on) {
      winParams.flags |= bits;
    } else {
      winParams.flags &= ~bits;
    }
    activity.getWindow().setAttributes(winParams);
  }


  public static int getStatusBarHeight(Context context) {
    // 获得状态栏高度
    int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
    return context.getResources().getDimensionPixelSize(resourceId);
  }

  /**
   * 状态栏和底部导航栏透明
   */
  public static void setNavigationBarStatusBarTranslucent(Activity activity) {
    if (Build.VERSION.SDK_INT >= 21) {
      View decorView = activity.getWindow().getDecorView();
      int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
      decorView.setSystemUiVisibility(option);
      activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
      activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
  }

  /**
   * 设置状态栏文字颜色为黑色
   */
  public static void setLightMode(@Nullable Activity activity) {
    if (activity == null) {
      return;
    }
    setMIUIStatusBarDarkIcon(activity, true);
    setMeizuStatusBarDarkIcon(activity, true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (activity.getWindow().getDecorView().getSystemUiVisibility() ==
        (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)) {
        return;
      }
      activity.getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
  }

  /**
   * 设置状态栏文字颜色为白色
   */
  public static void setDarkMode(@Nullable Activity activity) {
    if (activity == null) {
      return;
    }
    setMIUIStatusBarDarkIcon(activity, false);
    setMeizuStatusBarDarkIcon(activity, false);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (activity.getWindow().getDecorView().getSystemUiVisibility() ==
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) {
        return;
      }
      activity.getWindow().getDecorView()
        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
  }

  /**
   * 修改 MIUI V6 以上状态栏文字颜色
   */
  private static void setMIUIStatusBarDarkIcon(@NonNull Activity activity, boolean darkIcon) {
    Class<? extends Window> clazz = activity.getWindow().getClass();
    try {
      Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
      Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
      int darkModeFlag = field.getInt(layoutParams);
      Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
      extraFlagField.invoke(activity.getWindow(), darkIcon ? darkModeFlag : 0, darkModeFlag);
    } catch (Exception e) {
      // ignore
    }
  }

  /**
   * 修改魅族状态栏字体颜色 Flyme 4.0
   */
  private static void setMeizuStatusBarDarkIcon(@NonNull Activity activity, boolean darkIcon) {
    try {
      WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
      Field darkFlag =
        WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
      Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
      darkFlag.setAccessible(true);
      meizuFlags.setAccessible(true);
      int bit = darkFlag.getInt(null);
      int value = meizuFlags.getInt(lp);
      if (darkIcon) {
        value |= bit;
      } else {
        value &= ~bit;
      }
      meizuFlags.setInt(lp, value);
      activity.getWindow().setAttributes(lp);
    } catch (Exception e) {
      // ignore
    }
  }
}
