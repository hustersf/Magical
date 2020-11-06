package com.sofar.share;

import androidx.annotation.StringDef;

@StringDef({SharePlatformId.QQ, SharePlatformId.QQ_ZONE,
  SharePlatformId.MORE})
public @interface SharePlatformId {
  String QQ = "qq";
  String QQ_ZONE = "qq_zone";

  String MORE = "more";
}
