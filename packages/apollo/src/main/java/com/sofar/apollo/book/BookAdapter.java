package com.sofar.apollo.book;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.apollo.R;
import com.sofar.apollo.book.model.Book;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.utility.ViewUtil;

public class BookAdapter extends RecyclerAdapter<Book> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return ViewUtil.inflate(parent, R.layout.book_item);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Book> onCreateViewBinder(int viewType) {
    return new BookViewBinder();
  }
}
