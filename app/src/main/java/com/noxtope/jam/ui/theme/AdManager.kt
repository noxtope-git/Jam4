package com.noxtope.jam.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// IDs de PRUEBA de AdMob. Reemplazar por los reales al publicar.
object AdIds {
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdIds.BANNER
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

object AdManager {
    private var interstitial: InterstitialAd? = null
    private var isLoading = false

    fun cargarInterstitial(activity: Activity) {
        if (isLoading || interstitial != null) return
        isLoading = true
        InterstitialAd.load(
            activity,
            AdIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    isLoading = false
                }
            }
        )
    }

    fun mostrarInterstitial(activity: Activity, onClosed: () -> Unit = {}) {
        val ad = interstitial
        if (ad == null) {
            onClosed()
            return
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onClosed()
                cargarInterstitial(activity)
            }

            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                onClosed()
            }
        }
        ad.show(activity)
    }
}
