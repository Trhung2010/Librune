package com.librune.app.api;

import com.librune.app.model.Book;
import com.librune.app.model.BorrowRequest;
import com.librune.app.model.Category;
import com.librune.app.model.Notification;
import com.librune.app.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    @POST("api/auth/register")
    Call<Map<String, Object>> register(@Body User user);

    @POST("api/auth/logout")
    Call<Map<String, String>> logout();

    @GET("api/books/featured")
    Call<List<Book>> getFeaturedBooks();

    @GET("api/books/newest")
    Call<List<Book>> getNewestBooks();

    @GET("api/books")
    Call<List<Book>> searchBooks(
            @Query("keyword") String keyword,
            @Query("categoryId") Long categoryId,
            @Query("availableOnly") boolean availableOnly
    );

    @GET("api/books/{id}")
    Call<Book> getBookDetail(@Path("id") long id);

    @GET("api/categories")
    Call<List<Category>> getCategories();

    @GET("api/users/me")
    Call<User> getMyProfile();

    @PUT("api/users/me")
    Call<User> updateMyProfile(@Body User user);

    @PUT("api/users/me/password")
    Call<Map<String, String>> changePassword(@Body Map<String, String> body);

    @POST("api/borrows")
    Call<BorrowRequest> createBorrowRequest(@Body Map<String, Object> body);

    @GET("api/borrows/my")
    Call<List<BorrowRequest>> getMyBorrows(@Query("status") String status);

    @PUT("api/borrows/{id}/cancel")
    Call<BorrowRequest> cancelBorrowRequest(@Path("id") long id);

    @GET("api/notifications")
    Call<List<Notification>> getMyNotifications();

    @GET("api/notifications/unread-count")
    Call<Map<String, Long>> getUnreadNotificationCount();

    @PUT("api/notifications/{id}/read")
    Call<Notification> markNotificationRead(@Path("id") long id);

    @PUT("api/notifications/read-all")
    Call<Map<String, String>> markAllNotificationsRead();
}
