package com.sofar.snapu.viewbinder;

import android.net.Uri;

import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.snapu.R;
import com.sofar.snapu.core.PhotoHelper;
import com.sofar.snapu.model.ImageInfo;
import com.sofar.snapu.ui.PhotoPreviewActivity;

import java.io.File;

public class PhotoItemViewBinder extends RecyclerViewBinder<ImageInfo> {

  SofarImageView photoView;

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
    if (data.width > 0 && data.height > 0) {
      photoView.setAspectRatio(1.0f * data.width / data.height);
    }
    photoView.bindUrl(uri.toString());

    view.setOnClickListener(v -> {
      PhotoPreviewActivity.launch(getActivity(), data);
    });
  }
}
