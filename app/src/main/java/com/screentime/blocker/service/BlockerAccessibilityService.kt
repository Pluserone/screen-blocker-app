package com.screentime.blocker.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.screentime.blocker.BlockerApp
import com.screentime.blocker.data.local.BlockMode
import com.screentime.blocker.data.local.BlockerPreferences
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

    override fun onCreate() {
        super.onCreate()
        // Мгновенная загрузка из быстрого кэша еще до первого события
        blockedPackages = BlockerPreferences.getBlockedPackages(this)
        blockedSites = BlockerPreferences.getBlockedSites(this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        blockedPackages = BlockerPreferences.getBlockedPackages(this)
        blockedSites = BlockerPreferences.getBlockedSites(this)

        // Фоновый опрос базы данных для актуализации
        serviceScope.launch {
            val dao = BlockerApp.instance.database.blockerDao()
            while (isActive) {
                try {
                    val softSet = dao.getSettings(BlockMode.SOFT)
                    val hardSet = dao.getSettings(BlockMode.HARD)

                    val isSoftActive = softSet?.isLockActive == true
                    val isHardActive = hardSet?.isLockActive == true

                    val newBlockedPkgs = mutableMapOf<String, String>()
                    val newBlockedSites = mutableSetOf<String>()

                    if (isSoftActive) {
                        dao.getBlockedAppsList(BlockMode.SOFT).filter { it.isSelected }.forEach {
                            newBlockedPkgs[it.packageName] = it.appName
                        }
                        dao.getBlockedSitesList(BlockMode.SOFT).forEach {
                            normalizeDomain(it.url)?.let { domain -> newBlockedSites.add(domain) }
                        }
                    }

                    if (isHardActive) {
                        dao.getBlockedAppsList(BlockMode.HARD).filter { it.isSelected }.forEach {
                            newBlockedPkgs[it.packageName] = it.appName
                        }
                        dao.getBlockedSitesList(BlockMode.HARD).forEach {
                            normalizeDomain(it.url)?.let { domain -> newBlockedSites.add(domain) }
                        }
                    }

                    blockedPackages = newBlockedPkgs
                    blockedSites = newBlockedSites

                    // Сохраняем в мгновенный постоянный кэш
                    BlockerPreferences.updateCache(
                        this@BlockerAccessibilityService,
                        isSoftActive = isSoftActive,
                        isHardActive = isHardActive,
                        blockedPackagesMap = newBlockedPkgs,
                        blockedSitesSet = newBlockedSites
                    )

                    // Если любая блокировка активна — держим Foreground Service активным
                    if (isSoftActive || isHardActive) {
                        BlockerForegroundService.start(this@BlockerAccessibilityService)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000L)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val currentPackage = event.packageName?.toString() ?: return

        // Игнорируем лаунчер рабочего стола, системную шторку и наше собственное приложение
        if (currentPackage == packageName ||
            currentPackage == "com.android.systemui" ||
            currentPackage == "com.miui.home" ||
            currentPackage == "com.google.android.apps.nexuslauncher"
        ) {
            return
        }

        // Если кэш в памяти пуст, немедленно перечитываем быстрый кэш
        if (blockedPackages.isEmpty()) {
            blockedPackages = BlockerPreferences.getBlockedPackages(this)
            blockedSites = BlockerPreferences.getBlockedSites(this)
        }

        // 1. Защита от обхода через Настройки в режиме жесткой блокировки
        if (BlockerPreferences.isHardLockActive(this) && currentPackage == "com.android.settings") {
            val rootNode = rootInActiveWindow
            if (isSettingsBypassAttempt(rootNode)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                BlockOverlayActivity.start(
                    this,
                    title = "Настройки заблокированы",
                    subtitle = "В режиме жесткой блокировки доступ к изменению системных разрешений закрыт"
                )
                return
            }
        }

        // 2. Проверка блокировки приложений
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

        // 3. Проверка блокировки сайтов в браузерах
        if (blockedSites.isNotEmpty() && knownBrowserPackages.contains(currentPackage)) {
            val rootNode = rootInActiveWindow ?: return
            checkBrowserUrlRecursively(rootNode)
        }
    }

    private fun isSettingsBypassAttempt(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""

        val bypassKeywords = listOf(
            "screen blocker",
            "спец. возможности",
            "специальные возможности",
            "доступность",
            "accessibility",
            "администраторы устройства",
            "device admin",
            "удалить",
            "остановить",
            "force stop",
            "uninstall"
        )

        for (kw in bypassKeywords) {
            if (text.contains(kw) || contentDesc.contains(kw)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isSettingsBypassAttempt(node.getChild(i))) {
                return true
            }
        }
        return false
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
