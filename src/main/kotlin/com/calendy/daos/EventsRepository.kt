package com.calendy.daos

import com.calendy.models.CalenderEvent
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EventsRepository : CassandraRepository<CalenderEvent, String> {

    @Query("select * from event where hostuserid=:userId")
    fun findAllEventsForHostId(@Param("userId") userId: String): List<CalenderEvent>
}