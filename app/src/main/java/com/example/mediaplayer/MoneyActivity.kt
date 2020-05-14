package com.example.mediaplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.billingclient.api.*
import com.google.android.exoplayer2.util.Log

//https://blog.mindorks.com/implement-in-app-purchases-in-android
class MoneyActivity : AppCompatActivity(), PurchasesUpdatedListener {

    val TAG = "MoneyActivity"
    private lateinit var billingClient: BillingClient
    private lateinit var skuList: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money)
        val skuList = resources.getStringArray(R.array.money_array)

    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "onBillingSetupFinished: The BillingClient is setup successfully")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e(TAG, "onBillingServiceDisconnected: Disconnected :(")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

            }
        })

    }

    fun returnToPlayer(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onPurchasesUpdated(p0: BillingResult?, p1: MutableList<Purchase>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
