package com.example.evotingmobileapp

import android.app.Application
import android.util.Log
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType

class EVotingApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            val appMetaData = Core.Model.AppMetaData(
                name = "Decentralized E-Voting",
                description = "Mobile decentralized e-voting with QR polling-station check-in",
                url = "https://example.com",
                icons = listOf(
                    "https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"
                ),
                redirect = "${BuildConfig.APPLICATION_ID}://request"
            )

            CoreClient.initialize(
                projectId = BuildConfig.REOWN_PROJECT_ID,
                connectionType = ConnectionType.AUTOMATIC,
                application = this,
                metaData = appMetaData,
                onError = { error ->
                    Log.e("EVotingApplication", "Reown core initialization failed: $error")
                }
            )

            Log.d("EVotingApplication", "Reown core initialized")
        } catch (throwable: Throwable) {
            Log.e("EVotingApplication", "Unexpected Reown core setup failure", throwable)
        }
    }
}