package com.example.data.repository

import android.util.Log
import com.example.data.model.Subscriber
import com.example.data.model.FinancialTransaction
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Firebase is not initialized. Please configure google-services.json", e)
            null
        }
    }

    // 1. Add new subscriber
    suspend fun addSubscriber(subscriber: Subscriber): Boolean {
        if (db == null) return false
        return try {
            val subscriberData = hashMapOf(
                "uniqueCode" to subscriber.uniqueCode,
                "name" to subscriber.name,
                "phone" to subscriber.phone,
                "address" to subscriber.address,
                "notes" to subscriber.notes,
                "status" to subscriber.status,
                "totalDebt" to subscriber.totalDebt,
                "totalPaid" to subscriber.totalPaid,
                "nextDueDate" to subscriber.nextDueDate,
                "createdAt" to subscriber.createdAt
            )
            
            // Use local DB ID as the document ID in Firestore for consistency, or auto-generate
            db?.collection("subscribers")
                ?.document(subscriber.id.toString())
                ?.set(subscriberData)
                ?.await()
            Log.d("FirestoreService", "Subscriber added successfully")
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error adding subscriber", e)
            false
        }
    }

    // 2. Update subscriber
    suspend fun updateSubscriber(subscriber: Subscriber): Boolean {
        if (db == null) return false
        return try {
            val subscriberData = mapOf(
                "name" to subscriber.name,
                "phone" to subscriber.phone,
                "address" to subscriber.address,
                "notes" to subscriber.notes,
                "status" to subscriber.status,
                "totalDebt" to subscriber.totalDebt,
                "totalPaid" to subscriber.totalPaid,
                "nextDueDate" to subscriber.nextDueDate
            )
            
            db?.collection("subscribers")
                ?.document(subscriber.id.toString())
                ?.update(subscriberData)
                ?.await()
            Log.d("FirestoreService", "Subscriber updated successfully")
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error updating subscriber", e)
            false
        }
    }
    
    // Add transaction
    suspend fun addTransaction(transaction: FinancialTransaction): Boolean {
        if (db == null) return false
        return try {
            val txData = hashMapOf(
                "subscriberId" to transaction.subscriberId,
                "type" to transaction.type,
                "amount" to transaction.amount,
                "notes" to transaction.notes,
                "timestamp" to transaction.timestamp,
                "createdBy" to transaction.createdBy,
                "createdByRole" to transaction.createdByRole
            )
            
            db?.collection("transactions")
                ?.document(transaction.id.toString())
                ?.set(txData)
                ?.await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreService", "Error adding transaction", e)
            false
        }
    }
}
