package com.sofar.take.picture.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sofar.base.BaseActivity;
import com.sofar.image.widget.SofarImageView;
import com.sofar.take.picture.R;
import com.sofar.take.picture.core.PhotoHelper;
import com.sofar.take.picture.core.PhotoObserveProvider;
import com.sofar.take.picture.model.ImageInfo;
import com.sofar.utility.BitmapUtil;
import com.sofar.utility.ToastUtil;

import java.io.File;

/**
 * 图片预览界面
 */
public class PhotoPreviewActivity extends BaseActivity {

  public static final String KEY_PHOTO = "photo";

  ImageInfo imageInfo;

  SofarImageView photoView;

  TextView uploadTv;
  TextView editTv;
  TextView recaptureTv;
  TextView deleteTv;

  @NonNull
  PhotoHelper helper;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.photo_preview_activity);
    photoView = findViewById(R.id.photo);
    uploadTv = findViewById(R.id.upload);
    editTv = findViewById(R.id.edit);
    recaptureTv = findViewById(R.id.recapture);
    deleteTv = findViewById(R.id.delete);

    updateImageInfo(getIntent());

    uploadTv.setOnClickListener(v -> upload());
    editTv.setOnClickListener(v -> edit());
    recaptureTv.setOnClickListener(v -> capture());
    deleteTv.setOnClickListener(v -> delete());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    updateImageInfo(intent);
  }

  private void updateImageInfo(Intent intent) {
    imageInfo = (ImageInfo) intent.getSerializableExtra(KEY_PHOTO);
    helper = new PhotoHelper(this, imageInfo.taskId);
    showPhoto();
  }

  private void showPhoto() {
    Uri uri = Uri.fromFile(new File(helper.getPhotoThumbDir(), imageInfo.name));
    if (imageInfo.width > 0 && imageInfo.height > 0) {
      photoView.setAspectRatio(1.0f * imageInfo.width / imageInfo.height);
    }
    photoView.bindUrl(uri.toString());
  }

  private void upload() {

  }

  private void edit() {
    int degrees = 90;
    rotateImage(degrees);
    rotateThumb(degrees);
    viewRotate(degrees);

    ToastUtil.startShort(this, "旋转90度");
  }

  private void rotateImage(int degrees) {
    File file = new File(helper.getPhotoDir(), imageInfo.name);
    Bitmap srcBt = BitmapFactory.decodeFile(file.getAbsolutePath());
    Bitmap bitmap = BitmapUtil.rotateBitmap(srcBt, degrees);
    PhotoHelper.saveBitmapFile(bitmap, file);
  }

  private void rotateThumb(int degrees) {
    File file = new File(helper.getPhotoThumbDir(), imageInfo.name);
    Bitmap srcBt = BitmapFactory.decodeFile(file.getAbsolutePath());
    Bitmap bitmap = BitmapUtil.rotateBitmap(srcBt, degrees);
    PhotoHelper.saveBitmapFile(bitmap, file);
  }

  private void viewRotate(int degrees) {
    ObjectAnimator anim = ObjectAnimator.ofFloat(photoView, "rotation", photoView.getRotation(), photoView.getRotation() + degrees);
    anim.setDuration(200);
    anim.start();
  }

  private void capture() {

  }

  private void delete() {
    File file = new File(helper.getPhotoDir(), imageInfo.name);
    file.delete();

    File thumbFile = new File(helper.getPhotoThumbDir(), imageInfo.name);
    thumbFile.delete();

    ToastUtil.startShort(this, "删除成功");
  }

  public static void launch(@NonNull Activity activity, ImageInfo imageInfo) {
    Intent intent = new Intent(activity, PhotoPreviewActivity.class);
    intent.putExtra(KEY_PHOTO, imageInfo);
    activity.startActivity(intent);
  }

}
