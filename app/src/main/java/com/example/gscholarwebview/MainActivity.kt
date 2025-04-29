package com.example.gscholarwebview

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gscholarwebview.ui.theme.GScholarWebViewTheme // исправил пакет на твой
import kotlinx.parcelize.Parcelize

class MainActivity : ComponentActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GScholarWebViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScholarWebViewWithSaveState()
                }
            }
        }
    }
}
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScholarWebViewWithSaveState() {
    var webView: WebView? by remember { mutableStateOf(null) }

    var webViewBundle by rememberSaveable(stateSaver = WebViewBundleSaver) {
        mutableStateOf<WebViewBundle?>(null)
    }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webView = this

                if (webViewBundle?.bundle != null) {
                    val result = restoreState(webViewBundle!!.bundle)
                    if (result == null) {
                        // Если восстановить не получилось — грузим страницу вручную
                        loadUrl("https://scholar.google.com")
                    }
                } else {
                    loadUrl("https://scholar.google.com")
                }
            }
        },
        update = { view ->
            webView = view
        }
    )

    DisposableEffect(webView) {
        onDispose {
            webView?.let { view ->
                val bundle = Bundle()
                view.saveState(bundle)
                webViewBundle = WebViewBundle(bundle)
            }
        }
    }
}



// Класс для хранения состояния WebView
@Parcelize
data class WebViewBundle(val bundle: Bundle) : Parcelable

val WebViewBundleSaver: Saver<WebViewBundle?, Any> = run {
    mapSaver(
        save = { it?.let { mapOf("bundle" to it.bundle) } ?: emptyMap() },
        restore = { map ->
            map["bundle"]?.let { WebViewBundle(it as Bundle) }
        }
    )
}



