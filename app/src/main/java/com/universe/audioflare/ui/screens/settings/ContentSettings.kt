package com.universe.audioflare.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.universe.audioflared.utils.parseCookieString
import com.universe.audioflare.LocalPlayerAwareWindowInsets
import com.universe.audioflare.R
import com.universe.audioflare.constants.AccountChannelHandleKey
import com.universe.audioflare.constants.AccountEmailKey
import com.universe.audioflare.constants.AccountNameKey
import com.universe.audioflare.constants.ContentCountryKey
import com.universe.audioflare.constants.ContentLanguageKey
import com.universe.audioflare.constants.CountryCodeToName
import com.universe.audioflare.constants.InnerTubeCookieKey
import com.universe.audioflare.constants.LanguageCodeToName
import com.universe.audioflare.constants.ProxyEnabledKey
import com.universe.audioflare.constants.ProxyTypeKey
import com.universe.audioflare.constants.ProxyUrlKey
import com.universe.audioflare.constants.SYSTEM_DEFAULT
import com.universe.audioflare.ui.component.EditTextPreference
import com.universe.audioflare.ui.component.IconButton
import com.universe.audioflare.ui.component.ListPreference
import com.universe.audioflare.ui.component.PreferenceEntry
import com.universe.audioflare.ui.component.PreferenceGroupTitle
import com.universe.audioflare.ui.component.SwitchPreference
import com.universe.audioflare.ui.utils.backToMain
import com.universe.audioflare.utils.rememberEnumPreference
import com.universe.audioflare.utils.rememberPreference
import java.net.Proxy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")
    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")


    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceEntry(
            title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
            description = if (isLoggedIn) {
                accountEmail.takeIf { it.isNotEmpty() }
                    ?: accountChannelHandle.takeIf { it.isNotEmpty() }
            } else null,
            icon = { Icon(painterResource(R.drawable.person), null) },
            onClick = { navController.navigate("login") }
        )
        ListPreference(
            title = { Text(stringResource(R.string.content_language)) },
            icon = { Icon(painterResource(R.drawable.language), null) },
            selectedValue = contentLanguage,
            values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
            valueText = {
                LanguageCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentLanguageChange
        )
        ListPreference(
            title = { Text(stringResource(R.string.content_country)) },
            icon = { Icon(painterResource(R.drawable.location_on), null) },
            selectedValue = contentCountry,
            values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
            valueText = {
                CountryCodeToName.getOrElse(it) {
                    stringResource(R.string.system_default)
                }
            },
            onValueSelected = onContentCountryChange
        )

        PreferenceGroupTitle(
            title = "PROXY"
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_proxy)) },
            checked = proxyEnabled,
            onCheckedChange = onProxyEnabledChange
        )

        if (proxyEnabled) {
            ListPreference(
                title = { Text(stringResource(R.string.proxy_type)) },
                selectedValue = proxyType,
                values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                valueText = { it.name },
                onValueSelected = onProxyTypeChange
            )
            EditTextPreference(
                title = { Text(stringResource(R.string.proxy_url)) },
                value = proxyUrl,
                onValueChange = onProxyUrlChange
            )
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.content)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
