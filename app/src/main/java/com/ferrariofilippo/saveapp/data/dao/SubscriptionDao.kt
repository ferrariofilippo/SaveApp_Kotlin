package com.ferrariofilippo.saveapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query(
        "SELECT s.id, s.amount, s.description, s.renewal_type as renewalType, " +
                "s.creation_date as creationDate, s.last_paid as lastPaid, " +
                "s.next_renewal as nextRenewal, s.budgetId, s.tagId, t.name AS tagName, " +
                "t.color AS tagColor " +
                "FROM subscriptions AS s JOIN tags AS t ON s.tagId = t.id"
    )
    fun getAllTagged(): Flow<List<TaggedSubscription>>

    @Query(
        "SELECT s.id, s.amount, s.description, s.renewal_type as renewalType, " +
                "s.creation_date as creationDate, s.last_paid as lastPaid, " +
                "s.next_renewal as nextRenewal, s.budgetId, s.tagId, t.name AS tagName, " +
                "t.color AS tagColor " +
                "FROM subscriptions AS s JOIN tags AS t ON s.tagId = t.id " +
                "WHERE s.id = :id LIMIT 1"
    )
    suspend fun getTaggedById(id: Int): TaggedSubscription?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(subscription: Subscription)

    @Update
    suspend fun update(subscription: Subscription)

    @Delete
    suspend fun delete(subscription: Subscription)
}
