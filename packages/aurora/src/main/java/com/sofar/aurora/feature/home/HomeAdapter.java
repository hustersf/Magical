package com.sofar.aurora.feature.home;

import java.util.Map;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.sofar.aurora.feature.home.block.BlockType;
import com.sofar.aurora.feature.home.block.item.BlockItem;
import com.sofar.aurora.feature.home.model.HomeBlock;
import com.sofar.base.recycler.RecyclerAdapter;
import com.sofar.base.viewbinder.RecyclerViewBinder;

public class HomeAdapter extends RecyclerAdapter<HomeBlock> {

  @NonNull
  private final Map<BlockType, BlockItem> blocks;

  @NonNull
  private final SparseArray<BlockItem> viewTypeToBlocks;

  public HomeAdapter() {
    blocks = BlockType.buildFullBlock();
    viewTypeToBlocks = new SparseArray<>();
    for (Map.Entry<BlockType, BlockItem> entry : blocks.entrySet()) {
      viewTypeToBlocks.put(entry.getKey().ordinal(), entry.getValue());
    }
  }

  @Override
  protected View onCreateView(ViewGroup parent, int viewType) {
    return getBlockItem(viewType).createView(parent);
  }

  @NonNull
  @Override
  protected RecyclerViewBinder<HomeBlock> onCreateViewBinder(int viewType) {
    return getBlockItem(viewType).createViewBinder();
  }

  @Override
  public int getItemViewType(int position) {
    HomeBlock block = getItem(position);
    BlockType blockType = BlockType.getBlockType(block);
    return blockType.ordinal();
  }

  @NonNull
  private BlockItem getBlockItem(int viewType) {
    if (viewTypeToBlocks.get(viewType) != null) {
      return viewTypeToBlocks.get(viewType);
    }
    return BlockItem.DEFAULT;
  }

}
