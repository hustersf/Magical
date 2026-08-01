package com.sofar.apollo.learn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.core.legacy.R;
import androidx.appcompat.app.AppCompatActivity;

public class LearnActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new LearnFragment())
      .commitAllowingStateLoss();
  }

  public static void launch(@NonNull Activity activity) {
    Intent intent = new Intent(activity, LearnActivity.class);
    activity.startActivity(intent);
  }
}
