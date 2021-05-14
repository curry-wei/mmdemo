package cloud.bjx.mm.android

import cloud.bjx.mm.sdk.RtcEngine

object RtcEngineProxy {

    fun get(): RtcEngine = RtcEngine.create(ContextProvider.get(), UserSettings.getAppKey())

}