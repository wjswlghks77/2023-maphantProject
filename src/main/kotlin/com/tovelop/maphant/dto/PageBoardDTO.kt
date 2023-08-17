package com.tovelop.maphant.dto

import java.time.LocalDateTime

data class PageBoardDTO(
    val boardId: Int,
    val title: String,
    val body: String,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime?,
    val userNickname: String,
    val commentCnt: Int,
    val likeCnt: Int,
    val isAnonymous: Int,
    val isHide: Int,
    var isLike: Boolean,
    private val imagesUrlString: String? = null,
    private val tagStrings: String?
) {

    val imagesUrl: List<String>
        get() = imagesUrlString?.split(",") ?: listOf()

    val tags: List<String>
        get() = tagStrings?.split(",") ?: listOf()

}


