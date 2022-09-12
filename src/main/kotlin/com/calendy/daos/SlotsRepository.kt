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

    @Query("select * from slot where hostuserid=:userId ALLOW FILTERING")
    fun findSlotsForHostUserId(@Param("userId") userId: String): List<Slot>

    @Query("select * from slot where inviteeuserid=:userId ALLOW FILTERING")
    fun findSlotsForGuestUserId(@Param("userId") userId: String): List<Slot>
}