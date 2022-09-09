package com.calendy.daos

import com.calendy.models.Slot
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface SlotsRepository : CassandraRepository<Slot, String> {

}