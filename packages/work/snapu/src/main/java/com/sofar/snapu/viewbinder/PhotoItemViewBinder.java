package com.sofar.snapu.viewbinder;

import java.io.File;

import android.net.Uri;
import android.widget.ImageView;

import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.ImageExtKt;
import com.sofar.snapu.R;
import com.sofar.snapu.core.PhotoHelper;
import com.sofar.snapu.model.ImageInfo;
import com.sofar.snapu.ui.PhotoPreviewActivity;

public class PhotoItemViewBinder extends RecyclerViewBinder<ImageInfo> {

  ImageView photoView;

  @Override
  protected void onCreate() {
    super.onCreate();
    photoView = bindView(R.id.photo);
  }

  @Override
  protected void onBind(ImageInfo data) {
    super.onBind(data);
    PhotoHelper helper = new PhotoHelper(getActivity(), data.taskId);
    Uri uri = Uri.fromFile(new File(helper.getPhotoThumbDir(), data.name));
    ImageExtKt.loadImage(photoView, uri.toString());

    view.setOnClickListener(v -> {
      PhotoPreviewActivity.launch(getActivity(), data);
    });
  }
}
