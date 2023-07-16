package com.mikirinkode.pikul.data.model

data class Category(
    val categoryId: String,
    val name: String,
    val photoUrl: String,
) {
    companion object {
        fun getCategoryList(): List<Category> {
            val list = listOf<Category>(
                Category(
                    "1",
                    "Makanan Ringan",
                    "https://cdn2.iconfinder.com/data/icons/food-solid-icons-vol-4/48/185-1024.png"
                ),
                Category(
                    "2",
                    "Minuman",
                    ""
                ),
                Category(
                    "3",
                    "Jajan Tradisional",
                    ""
                ),
            )
            return list
        }
    }
}
