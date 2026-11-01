package com.screentime.blocker.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.screentime.blocker.BlockerApp
import com.screentime.blocker.data.local.BlockMode
import com.screentime.blocker.ui.BlockOverlayActivity
import kotlinx.coroutines.*

class BlockerAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var blockedPackages = mapOf<String, String>() // packageName -> appName

    @Volatile
    private var blockedSites = setOf<String>()

    private val knownBrowserPackages = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.yandex.browser",
        "com.sec.android.app.sbrowser",
        "com.mi.globalbrowser",
        "com.brave.browser",
        "com.microsoft.emmx",
        "com.duckduckgo.mobile.android"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        val dao = BlockerApp.instance.database.blockerDao()

        // Фоновый опрос базы данных для актуализации списка заблокированных приложений и сайтов
        serviceScope.launch {
            while (isActive) {
                try {
                    val softSet = dao.getSettings(BlockMode.SOFT)
                    val hardSet = dao.getSettings(BlockMode.HARD)

                    val newBlockedPkgs = mutableMapOf<String, String>()
                    val newBlockedSites = mutableSetOf<String>()

                    if (softSet?.isLockActive == true) {
                        dao.getBlockedAppsList(BlockMode.SOFT).filter { it.isSelected }.forEach {
                            newBlockedPkgs[it.packageName] = it.appName
                        }
                        dao.getBlockedSitesList(BlockMode.SOFT).forEach {
                            normalizeDomain(it.url)?.let { domain -> newBlockedSites.add(domain) }
                        }
                    }

                    if (hardSet?.isLockActive == true) {
                        dao.getBlockedAppsList(BlockMode.HARD).filter { it.isSelected }.forEach {
                            newBlockedPkgs[it.packageName] = it.appName
                        }
                        dao.getBlockedSitesList(BlockMode.HARD).forEach {
                            normalizeDomain(it.url)?.let { domain -> newBlockedSites.add(domain) }
                        }
                    }

                    blockedPackages = newBlockedPkgs
                    blockedSites = newBlockedSites
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1200L)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val currentPackage = event.packageName?.toString() ?: return

        // Игнорируем системные пакеты и наше собственное приложение
        if (currentPackage == packageName ||
            currentPackage == "com.android.systemui" ||
            currentPackage == "com.miui.home" ||
            currentPackage == "com.google.android.apps.nexuslauncher"
        ) {
            return
        }

        // 1. Проверка блокировки приложений
        if (blockedPackages.containsKey(currentPackage)) {
            val appName = blockedPackages[currentPackage] ?: "Приложение"
            performGlobalAction(GLOBAL_ACTION_HOME)
            BlockOverlayActivity.start(
                this,
                title = appName,
                subtitle = "Доступ к этому приложению ограничен"
            )
            return
        }

        // 2. Проверка блокировки сайтов в браузерах
        if (blockedSites.isNotEmpty() && knownBrowserPackages.contains(currentPackage)) {
            val rootNode = rootInActiveWindow ?: return
            checkBrowserUrlRecursively(rootNode)
        }
    }

    private fun checkBrowserUrlRecursively(node: AccessibilityNodeInfo?) {
        if (node == null) return
        val text = node.text?.toString()?.lowercase() ?: ""
        
        for (site in blockedSites) {
            if (text.contains(site)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                BlockOverlayActivity.start(
                    this,
                    title = "Сайт заблокирован",
                    subtitle = "Доступ к $site ограничен правилами экранного времени"
                )
                return
            }
        }

        for (i in 0 until node.childCount) {
            checkBrowserUrlRecursively(node.getChild(i))
        }
    }

    private fun normalizeDomain(url: String): String? {
        val clean = url.trim().lowercase()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
        val slashIndex = clean.indexOf('/')
        val domain = if (slashIndex != -1) clean.substring(0, slashIndex) else clean
        return if (domain.isNotBlank()) domain else null
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${BlockerAccessibilityService::class.java.canonicalName}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServicesSetting.contains(expectedServiceName)
        }
    }
}
