package com.smallcake.demo.adgun

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ADGunService  : AccessibilityService() {
    private val TAG = "ADGunService"
    companion object {
        var instance: ADGunService? = null  // 单例
        val isServiceEnable: Boolean get() = instance != null   // 判断无障碍服务是否可用
        var executor: ExecutorService = Executors.newFixedThreadPool(4) // 执行任务的线程池
        var ruleList:List<RuleEntity>?=null


    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            // 在这里写跳过广告的逻辑
            Log.d(TAG, "$it")
            executor.execute {
                searchNode("跳过")?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                ruleList?.forEach {
                    it.popupRules.forEach { rule ->
                        // 如果定位到匹配结点，查找要点击的结点并点击
                        if (searchNode(rule.id) != null) searchNode(rule.action)?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        executor.execute {
            val ruleList = readJsonToRuleList()
            ruleList?.forEach { Log.d(TAG,"$it") }
            Log.d(TAG,"自定义规则列表已加载...")
        }
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
    /**
     * 获得当前视图根节点
     * */
    private fun getCurrentRootNode() = try {
        rootInActiveWindow
    } catch (e: Exception) {
        e.message?.let { Log.e(TAG, it) }
        null
    }
    /**
     * 读取json文件生成规则实体列表
     * */
    private fun readJsonToRuleList(): List<RuleEntity>? {
        try {
            val inputStream = resources.openRawResource(R.raw.basic_rules)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            reader.use {
                var line: String? = it.readLine()
                while (line != null) {
                    sb.append(line)
                    line = it.readLine()
                }
            }
            val ruleEntityList = arrayListOf<RuleEntity>()
            val jsonArray = JSONArray(sb.toString())
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.getString(key)
                    val ruleEntityJson = JSONObject(value)
                    val popupRules = ruleEntityJson.getJSONArray("popup_rules")
                    val ruleEntity = RuleEntity(arrayListOf())
                    for (j in 0 until popupRules.length()) {
                        val ruleObject = popupRules.getJSONObject(j)
                        Log.d(TAG,"ruleObject==$ruleObject")
                        val ruleDetail = RuleDetail(ruleObject.getString("id"), ruleObject.getString("action"))
                        ruleEntity.popupRules.add(ruleDetail)
                    }
                    ruleEntityList.add(ruleEntity)
                }
            }
            return ruleEntityList
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }
    /**
     * 递归遍历查找匹配文本或id结点
     * 结点id的构造规则：包名:id/具体id
     * */
    private fun searchNode(filter: String): AccessibilityNodeInfo? {
        val rootNode = getCurrentRootNode()
        if (rootNode != null) {
            rootNode.findAccessibilityNodeInfosByText(filter).takeUnless { it.isNullOrEmpty() }?.let {
                Log.d(TAG,"通过[文字]找到可点击的节点[$filter]  it==${it.size}")
                return it[0]
            }
            if (!rootNode.packageName.isNullOrBlank()) {
                rootNode.findAccessibilityNodeInfosByViewId("${rootNode.packageName}:id/$filter")
                    .takeUnless { it.isNullOrEmpty() }?.let {
                        Log.d(TAG,"通过[ID]找到可点击的节点[$filter]")
                        return it[0]
                    }
            }
        }
        return null
    }
}