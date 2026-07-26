package com.noxtope.jam.ui.theme

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BillingManager(context: Context) : PurchasesUpdatedListener {

    companion object {
        val SKUS = mapOf(
            5 to "apoyo_5",
            10 to "apoyo_10",
            15 to "apoyo_15",
            25 to "apoyo_25",
            50 to "apoyo_50",
            100 to "apoyo_100"
        )
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var onPurchaseSuccess: (() -> Unit)? = null
    private var onPurchaseError: ((String) -> Unit)? = null

    init {
        connect()
    }

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    queryProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
            }
        })
    }

    private fun queryProducts() {
        val productList = SKUS.values.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
            _productDetails.value = productDetailsList.associateBy { it.productId }
        }
    }

    fun launchPurchase(
        activity: Activity,
        puntos: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val sku = SKUS[puntos] ?: run {
            onError("Monto no disponible para compra directa")
            return
        }
        val details = _productDetails.value[sku] ?: run {
            onError("Producto no disponible en Google Play")
            return
        }
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken

        onPurchaseSuccess = onSuccess
        onPurchaseError = onError

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { offerToken?.let { setOfferToken(it) } }
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<com.android.billingclient.api.Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            val purchase = purchases.first()
            if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                // Acknowledge the purchase
                val acknowledgeParams = com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgeParams) { _ -> }
                onPurchaseSuccess?.invoke()
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            onPurchaseError?.invoke("Compra cancelada")
        } else if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            onPurchaseError?.invoke("Error en la compra: ${billingResult.debugMessage}")
        }
    }

    fun getPriceForPoints(puntos: Int): String {
        val sku = SKUS[puntos] ?: return "\$$puntos USD"
        val details = _productDetails.value[sku] ?: return "\$$puntos USD"
        val price = details.oneTimePurchaseOfferDetails?.formattedPrice
        return price ?: "\$$puntos USD"
    }

    fun destroy() {
        billingClient.endConnection()
    }
}
