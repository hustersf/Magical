package com.sofar.aurora;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

  private List<Fragment> mList = new ArrayList<>();

  public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  public void setList(@NonNull List<Fragment> list) {
    mList.clear();
    mList.addAll(list);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return mList.get(position);
  }

  @Override
  public int getItemCount() {
    return mList.size();
  }
}
