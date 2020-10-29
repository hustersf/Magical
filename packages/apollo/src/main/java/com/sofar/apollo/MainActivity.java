package com.sofar.apollo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sofar.apollo.book.BookListFragment;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportFragmentManager().beginTransaction()
      .replace(R.id.fragment_container, new BookListFragment())
      .commitAllowingStateLoss();
  }
}