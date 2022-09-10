package com.calendy.daos

import com.calendy.models.Slot
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SlotsRepository : CassandraRepository<Slot, String> {

    @Query("select * from slot where eventid =:eventId")
    fun findSlotsForEventId(@Param("eventId") eventId: String): List<Slot>
}