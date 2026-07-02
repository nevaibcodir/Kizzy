/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * ServiceModule.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.data.di

import com.my.kizzy.data.rpc.Constants
import com.my.kizzy.data.rpc.KizzyRPC
import com.my.kizzy.domain.interfaces.Logger
import com.my.kizzy.domain.repository.KizzyRepository
import com.my.kizzy.preference.Prefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import kizzy.gateway.DiscordWebSocket
import kizzy.gateway.DiscordWebSocketImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @Provides
    fun providesDiscordWebsocket(
        logger: Logger
    ): DiscordWebSocket {
        // Spoof the gateway IDENTIFY "browser" (what platform-indicator plugins read)
        // based on the platform selected in Experimental RPC. Read fresh on each
        // service start so changing the platform + restarting takes effect.
        val (browser, os, device) = Constants.spoofIdentifyProperties(
            Prefs[Prefs.EXPERIMENTAL_RPC_PLATFORM, ""]
        )
        return DiscordWebSocketImpl(
            token = Prefs[Prefs.TOKEN, ""],
            logger = logger,
            identifyBrowser = browser,
            identifyOs = os,
            identifyDevice = device
        )
    }

    @Provides
    fun provideKizzyRpc(
        kizzyRepository: KizzyRepository,
        discordWebSocket: DiscordWebSocket,
        logger: Logger
    ) = KizzyRPC(Prefs[Prefs.TOKEN, ""], kizzyRepository, discordWebSocket, logger)

    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}