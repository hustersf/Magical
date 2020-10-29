package com.sofar.apollo.book;

import androidx.annotation.NonNull;

import com.sofar.apollo.R;
import com.sofar.apollo.book.model.Book;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BookAdapter extends RecyclerAdapter<Book> {

  @Override
  protected int getItemLayoutId(int viewType) {
    return R.layout.book_item;
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Book> onCreateViewBinder(int viewType) {
    return new BookViewBinder();
  }
}
