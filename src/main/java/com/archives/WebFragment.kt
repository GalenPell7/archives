package com.archives

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.fragment.app.Fragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple WebFragment subclass.
 * Contains a WebView that URL will be supplied to.
 */
class WebFragment(private val url : String) : Fragment() {
    // TODO: Rename and change types of parameters



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Create View, set attributes and load url.
     * @return a view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_web, container, false)
        val webView : WebView = view.findViewById(R.id.webView) as WebView
        val button : Button = view.findViewById(R.id.backtrack) as Button
        webView.settings.javaScriptEnabled
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
        // Inflate the layout for this fragment
        button.setOnClickListener {
            activity?.onBackPressed()
        }
        return view
    }
}