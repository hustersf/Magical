package com.sofar.take.picture.viewbinder;

import android.net.Uri;

import com.sofar.base.viewbinder.RecyclerViewBinder;
import com.sofar.image.widget.SofarImageView;
import com.sofar.take.picture.R;
import com.sofar.take.picture.core.PhotoHelper;
import com.sofar.take.picture.model.ImageInfo;
import com.sofar.take.picture.ui.PhotoPreviewActivity;

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
    Uri uri = Uri.fromFile(new File(PhotoHelper.getPhotoThumbDir(getActivity()), data.name));
    if (data.width > 0 && data.height > 0) {
      photoView.setAspectRatio(1.0f * data.width / data.height);
    }
    photoView.bindUrl(uri.toString());

    view.setOnClickListener(v -> {
      PhotoPreviewActivity.launch(getActivity(), data);
    });
  }
}
