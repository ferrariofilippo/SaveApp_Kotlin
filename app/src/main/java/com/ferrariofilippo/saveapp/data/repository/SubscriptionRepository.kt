package com.ferrariofilippo.saveapp.data.repository

import androidx.annotation.WorkerThread
import com.ferrariofilippo.saveapp.data.dao.SubscriptionDao
import com.ferrariofilippo.saveapp.model.entities.Subscription
import com.ferrariofilippo.saveapp.model.taggeditems.TaggedSubscription
import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {
    val allTaggedSubscriptions: Flow<List<TaggedSubscription>> = subscriptionDao.getAllTagged();

    @WorkerThread
    suspend fun getAll(): List<Subscription> {
        return subscriptionDao.getAll()
    }

    @WorkerThread
    suspend fun getById(id: Int): Subscription? {
        return subscriptionDao.getById(id);
    }

    @WorkerThread
    suspend fun getTaggedById(id: Int): TaggedSubscription? {
        return subscriptionDao.getTaggedById(id);
    }

    @WorkerThread
    suspend fun insert(subscription: Subscription) {
        subscriptionDao.insert(subscription);
    }

    @WorkerThread
    suspend fun update(subscription: Subscription) {
        subscriptionDao.update(subscription);
    }

    @WorkerThread
    suspend fun delete(subscription: Subscription) {
        subscriptionDao.delete(subscription);
    }
}
