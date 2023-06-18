package com.example.a0509

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.a0509.databinding.NoticeViewBinding

class NoticeViewActivity : AppCompatActivity() {
	
	private lateinit var binding: NoticeViewBinding
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		binding = NoticeViewBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		
		val url=intent.getStringExtra("url").toString()
		val webView=binding.webView
		webView.settings.apply {
			javaScriptEnabled=true
			domStorageEnabled=true
			setSupportMultipleWindows(true)
		}
		webView.webViewClient= WebViewClient()
		webView.loadUrl(url)
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when(item.itemId) {
			android.R.id.home -> {
				finish()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}
}