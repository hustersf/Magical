package com.sofar.apollo;

import android.os.Bundle;

import com.sofar.apollo.home.HomeFragment;
import com.sofar.apollo.mock.MockManager;
import com.sofar.apollo.word.WordDataManager;
import com.sofar.base.BaseActivity;

public class MainActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new HomeFragment())
      .commitAllowingStateLoss();

    WordDataManager.get().setWords(MockManager.get().convertData(this));
  }
}