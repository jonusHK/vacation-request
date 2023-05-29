package com.bhkpo.vacation.domain

import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseSyncRepository<T, ID> {
    fun flush()

    fun save(entity: T): T

    fun saveAndFlush(entity: T): T
}
