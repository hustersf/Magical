package com.sofar.base.tab;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {

  private List<Fragment> mFragments = new ArrayList<>();

  public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  public FragmentAdapter(@NonNull Fragment fragment) {
    super(fragment);
  }

  public FragmentAdapter(@NonNull FragmentManager fragmentManager,
    @NonNull Lifecycle lifecycle) {
    super(fragmentManager, lifecycle);
  }

  public void setFragments(@NonNull List<Fragment> list) {
    mFragments.clear();
    mFragments.addAll(list);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return mFragments.get(position);
  }

  @Override
  public int getItemCount() {
    return mFragments.size();
  }
}
