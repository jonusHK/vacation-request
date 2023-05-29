package com.bhkpo.vacation.adapter.inbound.presentation.dto.response

data class CollectionResponseDto<T>(

    val total: Long,

    val items: List<T>,

    val offset: Long,

    val limit: Long
)
