package com.example.evotingmobileapp

import android.app.Application
import android.util.Log

class EVotingApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /*
         * Reown / WalletConnect startup initialization is intentionally disabled here.
         *
         * Reason:
         * - The current tested FYP prototype uses the configured local blockchain contract,
         *   app asset contract-info.json, and supervised wallet flow.
         * - Initializing Reown automatically during Application.onCreate can delay app launch
         *   before MainActivity is displayed and may trigger "app isn't responding" on emulator.
         *
         * Future improvement:
         * - Re-enable wallet-provider initialization lazily only when a real external wallet
         *   connection screen is opened.
         */
        Log.d("EVotingApplication", "Application started without eager wallet-provider initialization")
    }
}