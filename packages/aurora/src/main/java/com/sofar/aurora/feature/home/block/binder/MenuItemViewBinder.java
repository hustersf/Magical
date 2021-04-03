package com.sofar.aurora.feature.home.block.binder;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.sofar.aurora.R;
import com.sofar.aurora.model.Menu;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class MenuItemViewBinder extends RecyclerViewBinder<Menu> {

  ImageView iconIv;
  TextView nameTv;

  @Override
  protected void onCreate() {
    super.onCreate();
    iconIv = bindView(R.id.icon);
    nameTv = bindView(R.id.name);
  }

  @Override
  protected void onBind(Menu data) {
    super.onBind(data);
    nameTv.setText(data.name);
    data.resId = R.drawable.home_tab_icon_music;
    if (TextUtils.equals(data.module, "artist")) {
      data.resId = R.drawable.menu_icon_artist;
    } else if (TextUtils.equals(data.module, "billboard")) {
      data.resId = R.drawable.menu_icon_rank;
    } else if (TextUtils.equals(data.module, "tracklist")) {
      data.resId = R.drawable.menu_icon_songs;
    }
    iconIv.setImageResource(data.resId);
  }
}
