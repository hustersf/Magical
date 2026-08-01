package com.sofar.apollo.book;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.apollo.R;
import com.sofar.apollo.book.model.Book;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class BookAdapter extends RecyclerAdapter<Book> {

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<Book> onCreateViewBinder(int viewType) {
    return new BookViewBinder();
  }
}
