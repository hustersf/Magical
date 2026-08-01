package com.sofar.base.page.retrofit;

import java.util.List;

import com.sofar.base.page.response.ListResponse;

public abstract class SofarRetrofitPageList<PAGE extends ListResponse<MODEL>, MODEL>
    extends RetrofitPageList<PAGE, MODEL> {

  @Override
  protected boolean getHasMoreFromResponse(PAGE response) {
    return response.hasMore();
  }

  @Override
  public void onLoadItemFromResponse(PAGE page, List<MODEL> items) {

    if (isFirstPage()) {
      items.clear();
    }

    List<MODEL> newItems = page.getItems();
    if (newItems == null || newItems.isEmpty()) {
      return;
    }

    if (allowDuplicate()) {
      items.addAll(newItems);
      return;
    }

    for (MODEL item : newItems) {
      if (!items.contains(item)) {
        items.add(item);
      }
    }
  }

  protected boolean allowDuplicate() {
    return true;
  }
}
