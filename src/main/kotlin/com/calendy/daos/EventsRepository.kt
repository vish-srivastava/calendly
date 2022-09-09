package com.calendy.daos

import com.calendy.models.CalenderEvent
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EventsRepository : CassandraRepository<CalenderEvent, String> {

}