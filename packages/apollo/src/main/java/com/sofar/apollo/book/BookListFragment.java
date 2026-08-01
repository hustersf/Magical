package com.sofar.apollo.book;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sofar.apollo.book.model.Book;
import com.sofar.apollo.book.model.BookPageList;
import com.sofar.base.page.PageList;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.recycler.RecyclerFragment;
import com.sofar.core.ui.recyclerview.LinearMarginItemDecoration;
import com.sofar.core.ui.util.DimensExtKt;

public class BookListFragment extends RecyclerFragment<Book> {

  @Override
  protected RecyclerAdapter<Book> onCreateAdapter() {
    return new BookAdapter();
  }

  @Override
  protected PageList<?, Book> onCreatePageList() {
    return new BookPageList();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    int sideSpace = DimensExtKt.dp2pxInt(getActivity(), 10);
    int betweenSpace = DimensExtKt.dp2pxInt(getActivity(), 15);
    LinearMarginItemDecoration decoration = new LinearMarginItemDecoration(RecyclerView.VERTICAL, sideSpace, betweenSpace);
    getRecyclerView().addItemDecoration(decoration);
  }
}
