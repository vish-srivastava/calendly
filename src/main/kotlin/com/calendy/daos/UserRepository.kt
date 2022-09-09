package com.calendy.daos

import com.calendy.models.User
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface UserRepository : CassandraRepository<User, String> {
    fun findByuserId(userId: String): MutableList<User>

}