package com.sofar.apollo.mine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.sofar.base.R;
import com.sofar.base.BaseActivity;

public class MineActivity extends BaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new MineFragment())
      .commitAllowingStateLoss();
  }

  public static void launch(@NonNull Activity activity) {
    Intent intent = new Intent(activity, MineActivity.class);
    activity.startActivity(intent);
  }

}
