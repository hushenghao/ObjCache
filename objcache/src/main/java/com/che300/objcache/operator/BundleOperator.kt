package com.che300.objcache.operator

import android.os.Bundle
import com.che300.objcache.annotation.KeyFactor

/**
 * Bundle 序列化
 */
@KeyFactor("Bundle")
internal open class BundleOperator : ParcelableOperator.Get<Bundle>(Bundle.CREATOR)