package com.sofar.take.picture.model;

import com.sofar.base.page.response.ListResponse;

import java.util.List;

public class PhotoPageResponse implements ListResponse<ImageInfo> {

  public List<ImageInfo> items;

  @Override
  public boolean hasMore() {
    return false;
  }

  @Override
  public List<ImageInfo> getItems() {
    return items;
  }
}
