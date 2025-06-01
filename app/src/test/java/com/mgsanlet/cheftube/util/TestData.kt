package com.mgsanlet.cheftube.util

import com.google.firebase.Timestamp
import com.mgsanlet.cheftube.data.model.CommentResponse
import com.mgsanlet.cheftube.data.model.Product
import com.mgsanlet.cheftube.data.model.ProductResponse
import com.mgsanlet.cheftube.data.model.RecipeResponse
import com.mgsanlet.cheftube.data.model.UserResponse
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.domain.model.DomainProduct
import com.mgsanlet.cheftube.domain.model.DomainRecipe
import com.mgsanlet.cheftube.domain.model.DomainStats
import com.mgsanlet.cheftube.domain.model.DomainUser
import java.time.Instant.now
import java.util.*

/**
 * Objeto que contiene datos de prueba reutilizables para las pruebas unitarias.
 */
object TestData {
    
    // Usuario de prueba
    val testUser = DomainUser(
        id = "user123",
        username = "Test User",
        email = "test@example.com",
        profilePictureUrl = "https://example.com/photo.jpg"
    )

    // Comentario de prueba
    val testComment = DomainComment(
        author = testUser,
        content = "This is a test comment",
        timestamp = System.currentTimeMillis()
    )

    // Receta de prueba
    val testRecipe = DomainRecipe(
        id = "1",
        title = "Test Recipe",
        imageUrl = "https://example.com/recipe.jpg",
        videoUrl = "https://example.com/video1",
        ingredients = listOf("Ingredient 1", "Ingredient 2"),
        steps = listOf("Step 1", "Step 2"),
        categories = listOf("Test", "Demo"),
        comments = listOf(testComment),
        favouriteCount = 10,
        durationMinutes = 30,
        difficulty = 0,
        author = testUser
    )
    
    // UserResponse de prueba
    val testUserResponse = UserResponse(
        username = testUser.username,
        email = testUser.email,
        bio = "Test bio",
        hasProfilePicture = true,
        createdRecipes = emptyList(),
        favouriteRecipes = emptyList(),
        followersIds = emptyList(),
        followingIds = emptyList(),
        lastLogin = Timestamp.now()
    )
    
    // CommentResponse de prueba
    val testCommentResponse = CommentResponse(
        authorId = testUser.id,
        authorName = testUser.username,
        authorEmail = testUser.email,
        authorHasProfilePicture = true,
        content = testComment.content,
        timestamp = testComment.timestamp
    )
    
    // RecipeResponse de prueba
    val testRecipeResponse = RecipeResponse(
        id = testRecipe.id,
        title = testRecipe.title,
        videoUrl = testRecipe.videoUrl,
        ingredients = testRecipe.ingredients,
        steps = testRecipe.steps,
        categories = testRecipe.categories,
        comments = listOf(testCommentResponse),
        favouriteCount = testRecipe.favouriteCount,
        durationMinutes = testRecipe.durationMinutes,
        difficulty = testRecipe.difficulty,
        authorId = testUser.id,
        authorEmail = testUser.email,
        authorName = testUser.username,
        authorHasProfilePicture = true
    )
    
    // Product de prueba
    val testProduct = Product(
        code = "1234567890123",
        product_name = "Test Product",
        product_name_en = "Test Product EN",
        product_name_it = "Prodotto di Test IT",
        product_name_es = "Producto de Prueba ES"
    )
    
    // ProductResponse de prueba
    val testProductResponse = ProductResponse(
        status = "success",
        product = testProduct
    )
    
    // DomainProduct de prueba
    val testDomainProduct = DomainProduct(
        barcode = testProduct.code,
        englishName = testProduct.product_name_en ?: testProduct.product_name ?: "",
        italianName = testProduct.product_name_it ?: testProduct.product_name ?: "",
        spanishName = testProduct.product_name_es ?: testProduct.product_name ?: ""
    )

    val testStats = DomainStats(
        loginTimestamps = List(24) { now().minusSeconds((0L..(30L * 24 * 60 * 60)).random()) },
        interactionTimestamps = List(83) { now().minusSeconds((0L..(30L * 24 * 60 * 60)).random()) },
        scanTimestamps = List(15) { now().minusSeconds((0L..(30L * 24 * 60 * 60)).random()) }
    )
}
