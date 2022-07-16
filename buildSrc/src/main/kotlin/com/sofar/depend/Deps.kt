package com.sofar.depend

import com.sofar.depend.repo.Google
import com.sofar.depend.repo.Kotlin
import com.sofar.depend.repo.OpenSource
import com.sofar.depend.repo.Test

object Deps {

  val google = Google()

  val test = Test()

  val kotlin = Kotlin()

  val openSource = OpenSource()
}