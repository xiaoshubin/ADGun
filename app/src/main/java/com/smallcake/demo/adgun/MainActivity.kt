package com.smallcake.demo.adgun

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

/**
 * 实现跳过开屏广告的核心点就三步：关注Event → 查找结点 → 点击结点
 */
class MainActivity : AppCompatActivity() {
    private lateinit var mServiceStatusTv: TextView
    private lateinit var mToOpenBt: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mServiceStatusTv = findViewById(R.id.tv_service_status)
        mToOpenBt = findViewById<AppCompatButton>(R.id.bt_open_service).apply {
            setOnClickListener {
                jumpAccessibilityServiceSettings()
            }
        }



    }

    private fun jumpAccessibilityServiceSettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshServiceStatusUI()
    }
    /**
     * 刷新无障碍服务状态的UI
     * */
    private fun refreshServiceStatusUI() {
        if (ADGunService.isServiceEnable) {
            mServiceStatusTv.text = "跳过广告服务状态：已开启"
            mToOpenBt.visibility = View.GONE
        } else {
            mServiceStatusTv.text = "跳过广告服务状态：未开启"
            mToOpenBt.visibility = View.VISIBLE
        }
    }


}