package com.sofar.aurora.feature.home.block.binder;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

import com.sofar.aurora.R;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.aurora.model.Category;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.core.ui.flowlayout.FlowTagList;

public class CategoryBlockViewBinder extends RecyclerViewBinder<HomeBlock<Category>> {

  FlowTagList mTagList;

  @Override
  protected void onCreate() {
    super.onCreate();
    mTagList = bindView(R.id.category_list);
    mTagList.setColor(ContextCompat.getColor(context, com.sofar.core.legacy.R.color.theme_color));
  }

  @Override
  protected void onBind(HomeBlock<Category> data) {
    super.onBind(data);
    if (data.results != null && !data.results.isEmpty()) {
      List<String> tags = new ArrayList<>();
      for (Category item : data.results) {
        tags.add(item.categoryName);
      }
      mTagList.setTags(tags);
    }
  }

}
