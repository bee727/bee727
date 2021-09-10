//package com.example.homeTproject
//
//import androidx.room.*
//
//@Entity(tableName = "wishlist")
//class WishList (
//    @PrimaryKey(autoGenerate = true) var id: Int,
//    @ColumnInfo(name = "exercise") var exercise : String?,
//    @ColumnInfo(name = "name") var name : String?,
//    @ColumnInfo(name = "info") var info : String?
//)
//
//@Dao
//interface wishListDao {
//    @Query("SELECT * FROM wishlist")
//    fun  getAll(): List<WishList>
//
//    @Insert(onConflict = REPLACE)
//    fun insert(wishList: WishList)
//
//    @Query("DELETE from wishlist")
//    fun deleteAll()
//}