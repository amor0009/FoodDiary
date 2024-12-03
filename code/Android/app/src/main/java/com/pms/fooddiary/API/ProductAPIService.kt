package com.pms.fooddiary.API

import com.pms.fooddiary.models.ProductCreate
import com.pms.fooddiary.models.ProductRead
import retrofit2.Call
import retrofit2.http.*

interface ProductAPIService {

    @GET("/product/products")
    fun getAllProducts(): Call<List<ProductRead>>

    @GET("/product/search")
    fun searchProducts(
        @Query("query") query: String?
    ): Call<List<ProductRead>>

    @POST("/product/product")
    fun addProduct(
        @Body product: ProductCreate
    ): Call<ProductRead>

    @GET("/product/{productName}")
    fun getProductByName(
        @Path("productName") productName: String
    ): Call<ProductRead>

    @PUT("/product/{productName}")
    fun updateProduct(
        @Path("productName") productName: String,
        @Body product: ProductCreate
    ): Call<ProductRead>

    @DELETE("/product/{productName}")
    fun deleteProduct(
        @Path("productName") productName: String
    ): Call<Void>
}
