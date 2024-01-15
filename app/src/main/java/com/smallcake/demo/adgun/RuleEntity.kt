package com.smallcake.demo.adgun

data class RuleEntity (val popupRules: ArrayList<RuleDetail>)

/**
 *
 * @property id String     匹配结点的id或文本
 * @property action String 点击结点的id或文本
 * @constructor
 */
data class RuleDetail(
    val id: String,
    val action: String
)