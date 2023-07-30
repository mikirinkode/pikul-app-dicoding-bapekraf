package com.mikirinkode.pikul.data.model

import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    var ownerId: String? = null,
    var businessId: String? = null,
    var businessName: String? = null,
    var businessDescription: String? = null,
    var businessProductCategory: String? = null,
    var businessEmail: String? = null,
    var businessPhoneNumber: String? = null,
    var businessProvince: String? = null,
    var businessAddress: String? = null,
    var businessPhoto: String? = null,
    var businessRating: Double? = null,
    var sellingMode: Boolean? = null,
    var openJobVacancy: Boolean? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    override val objectID: ObjectID
//    var product: List<Product>
//    var reviews
): Indexable