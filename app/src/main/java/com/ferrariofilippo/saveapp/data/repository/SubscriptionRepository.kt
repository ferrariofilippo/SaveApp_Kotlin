package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.SubscriptionDao
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {
    val allTaggedSubscriptions: Flow<List<TaggedSubscription>> = subscriptionDao.getAllTagged();

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAll(): List<Subscription> {
        return subscriptionDao.getAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getById(id: Int): Subscription? {
        return subscriptionDao.getById(id);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getTaggedById(id: Int): TaggedSubscription? {
        return subscriptionDao.getTaggedById(id);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(subscription: Subscription) {
        subscriptionDao.insert(subscription);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(subscription: Subscription) {
        subscriptionDao.update(subscription);
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(subscription: Subscription) {
        subscriptionDao.delete(subscription);
    }
}
