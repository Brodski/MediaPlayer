package com.bskimusicplayer.mediaplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.android.billingclient.api.*
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.donate1.*

//https://blog.mindorks.com/implement-in-app-purchases-in-android
class MoneyActivity : AppCompatActivity(), PurchasesUpdatedListener {

    val TAG = "MoneyActivity"
    private lateinit var billingClient: BillingClient
    private lateinit var skuList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money)
        skuList = resources.getStringArray(R.array.money_array).toMutableList()
//        preskuList.forEach { skuList.add(it as String) }
//        skuList.forEach { Log.e(TAG, "onCreate: $it") }
        setupBillingClient()
    }

    private fun setupBillingClient() {
        Log.e(TAG, "setupBillingClient: 1")
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "onBillingSetupFinished: The BillingClient is setup successfully")
                    loadAllSkus()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e(TAG, "onBillingServiceDisconnected: Disconnected :(")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

            }
        })

    }

    private fun loadAllSkus() = if (billingClient.isReady) {
        Log.e(TAG, "loadAllSkus: billingClient.isReady true")
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                Log.e(TAG, "loadAllSkus: BillingClient.BillingResponseCode.OK)")
                for (skuDetails in skuDetailsList) {
                    //this will return both the SKUs from Google Play Console
                    Log.e(TAG, "loadAllSkus: $skuDetails")
                    Log.e(TAG, "loadAllSkus: ${skuDetails.title}")
                    if (skuDetails.sku == "donation_2")
                        buttonBuyProduct.setOnClickListener {
                            val billingFlowParams = BillingFlowParams
                                .newBuilder()
                                .setSkuDetails(skuDetails)
                                .build()
                            billingClient.launchBillingFlow(this, billingFlowParams)
                        }
                }
            }
        }

    } else {
        println("Billing Client not ready")
    }

    fun returnToPlayer(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        Log.e(TAG, "onPurchasesUpdated: boom")
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.e(TAG, "onPurchasesUpdated: purchase $purchase")
                acknowledgePurchase(purchase.purchaseToken)

            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.e(TAG, "onPurchasesUpdated: user canceld")
            // Handle an error caused by a user cancelling the purchase flow.

        } else {
            Log.e(TAG, "onPurchasesUpdated: why this??")
            // Handle any other error codes.
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        Log.e(TAG, "acknowledgePurchase: ")
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

        }
    }


}
