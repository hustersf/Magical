package com.sofar.apollo;

import android.os.Bundle;
import android.widget.Toast;

import com.sofar.apollo.home.HomeFragment;
import com.sofar.apollo.mock.MockManager;
import com.sofar.apollo.word.WordDataManager;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  private static final int MAX_BACK_PRESS_INTERVAL = 2500;
  private long lastBackPressed;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new HomeFragment())
      .commitAllowingStateLoss();

    WordDataManager.get().setWords(MockManager.get().convertData(this));
  }

  @Override
  public void onBackPressed() {
    long current = System.currentTimeMillis();
    if (current - lastBackPressed < MAX_BACK_PRESS_INTERVAL) {
      finish();
    } else {
      lastBackPressed = current;
      Toast.makeText(this, getString(com.sofar.core.legacy.R.string.back_press_again),
        Toast.LENGTH_SHORT).show();
    }
  }
}