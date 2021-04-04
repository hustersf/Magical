package com.sofar.base.page;

import java.util.List;

import androidx.annotation.NonNull;

public interface PageList<PAGE, MODEL> {

  boolean hasMore();

  /**
   * 刷新列表数据
   */
  void refresh();

  /**
   * 加载更多
   */
  void load();

  /**
   * 列表是否为空
   */
  boolean isEmpty();

  void addAll(@NonNull List<MODEL> list);

  /**
   * 列表数据集合
   */
  List<MODEL> getItems();

  void setLatestPage(PAGE latestPage);

  /**
   * 列表数据
   */
  PAGE getLatestPage();

  /**
   * 注册数据监听者
   */
  void registerObserver(PageListObserver observer);

  /**
   * 注销数据监听者
   */
  void unRegisterObserver(PageListObserver observer);
}
