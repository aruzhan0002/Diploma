package kz.aruzhan.care_steps.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    @GET("api/courses/public/cards/")
    suspend fun getCourseCards(
        @Query("title") title: String? = null,
        @Query("rating_min") ratingMin: Double? = null,
        @Query("price_min") priceMin: Int? = null,
        @Query("price_max") priceMax: Int? = null,
        @Query("level") level: String? = null
    ): List<CourseCardResponse>

    @GET("api/courses/public/previews/")
    suspend fun getCoursePreviews(): List<CoursePreviewResponse>

    @GET("api/courses/public/previews/")
    suspend fun getCoursePreviewsRaw(): okhttp3.ResponseBody

    @GET("api/courses/public/cards/{course_id}/description/")
    suspend fun getCourseDescription(@Path("course_id") courseId: Int): CourseDescriptionResponse

    @GET("api/courses/public/cards/{course_id}/content/")
    suspend fun getCourseContent(@Path("course_id") courseId: Int): CourseContentResponse

    @GET("api/courses/public/cards/{course_id}/content/")
    suspend fun getCourseContentRaw(@Path("course_id") courseId: Int): okhttp3.ResponseBody

    @GET("api/courses/{id}/content/")
    suspend fun getSpecialistCourseContentRaw(@Path("id") id: Int): okhttp3.ResponseBody

    @GET("api/courses/public/cards/{course_id}/specialist/")
    suspend fun getCourseSpecialist(@Path("course_id") courseId: Int): CourseSpecialistResponse

    /** Курс текущего специалиста (вкладка «Описание»). */
    @GET("api/courses/{id}/description/")
    suspend fun getSpecialistCourseDescription(@Path("id") id: Int): CourseDescriptionResponse

    /** Курс текущего специалиста (вкладка «Содержание»). */
    @GET("api/courses/{id}/content/")
    suspend fun getSpecialistCourseContent(@Path("id") id: Int): CourseContentResponse

    /** Курс текущего специалиста (вкладка «Специалист»). */
    @GET("api/courses/{id}/specialist/")
    suspend fun getSpecialistCourseSpecialist(@Path("id") id: Int): CourseSpecialistResponse

    /** Карточки специалистов для родителя (Swagger: GET /api/auth/public/specialists/cards/). */
    @GET("api/auth/public/specialists/cards/")
    suspend fun getSpecialistCards(
        @Query("q") q: String? = null,
        @Query("specialization_search") specializationSearch: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("limit") limit: Int? = null
    ): List<SpecialistCardResponse>

    /**
     * Детальная карточка специалиста для экрана профиля.
     * Схема ответа отличается от списка — см. [SpecialistDetailResponse].
     */
    @GET("api/auth/public/specialists/cards/{specialist_id}/")
    suspend fun getSpecialistCardById(@Path("specialist_id") specialistId: Int): SpecialistDetailResponse

    /** Raw-вариант детальной карточки — для парсинга вручную, если Gson не справится. */
    @GET("api/auth/public/specialists/cards/{specialist_id}/")
    suspend fun getSpecialistCardByIdRaw(@Path("specialist_id") specialistId: Int): okhttp3.ResponseBody

    /** Публичные курсы специалиста (как GET /api/courses/public/cards/). */
    @GET("api/auth/public/specialists/cards/{specialist_id}/courses/")
    suspend fun getSpecialistPublicCourses(@Path("specialist_id") specialistId: Int): List<CourseCardResponse>

    /** Raw JSON списка курсов специалиста (fallback при ошибке Gson). */
    @GET("api/auth/public/specialists/cards/{specialist_id}/courses/")
    suspend fun getSpecialistPublicCoursesRaw(@Path("specialist_id") specialistId: Int): okhttp3.ResponseBody

    /** Курсы текущего специалиста (по JWT). */
    @GET("api/courses/")
    suspend fun getSpecialistCourses(): List<CreatedCourseResponse>

    /** Raw fallback: если у одного курса кривой JSON-тип, парсим вручную на клиенте. */
    @GET("api/courses/")
    suspend fun getSpecialistCoursesRaw(): okhttp3.ResponseBody

    @GET("api/courses/{id}/")
    suspend fun getCourseById(@Path("id") id: Int): CreatedCourseResponse

    @GET("api/courses/{id}/")
    suspend fun getCourseByIdRaw(@Path("id") id: Int): okhttp3.ResponseBody

    @PUT("api/courses/{id}/")
    suspend fun updateCourse(
        @Path("id") id: Int,
        @Body body: UpdateCourseRequest
    ): CreatedCourseResponse

    @PATCH("api/courses/{id}/")
    suspend fun patchCourse(
        @Path("id") id: Int,
        @Body body: PartialCourseUpdateRequest
    ): CreatedCourseResponse

    @DELETE("api/courses/{id}/")
    suspend fun deleteCourse(@Path("id") id: Int): Response<Unit>

    @DELETE("api/courses/{course_id}/modules/{module_id}/")
    suspend fun deleteModule(
        @Path("course_id") courseId: Int,
        @Path("module_id") moduleId: Int
    ): Response<Unit>

    @Multipart
    @POST("api/courses/")
    suspend fun createCourse(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part("level") level: RequestBody,
        @Part("price") price: RequestBody,
        @Part("duration") duration: RequestBody,
        @Part("learning_outcomes") learningOutcomes: RequestBody,
        @Part tags: List<MultipartBody.Part>,
        @Part previewImage: MultipartBody.Part
    ): CreatedCourseResponse

    @Multipart
    @POST("api/courses/{course_id}/modules/")
    suspend fun createModule(
        @Path("course_id") courseId: Int,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("material_type") materialType: RequestBody,
        @Part file: MultipartBody.Part
    ): CreatedCourseModuleResponse

    @POST("api/courses/{course_id}/purchase/")
    suspend fun purchaseCourse(@Path("course_id") courseId: Int): PurchaseCourseResponse

    @POST("api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): Response<Unit>

    @POST("api/auth/login/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("api/auth/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): AccessResponse

    @POST("api/auth/password-reset/request/")
    suspend fun requestPasswordResetCode(
        @Body body: PasswordResetRequestBody
    ): Response<PasswordResetRequestResponse>

    @POST("api/auth/password-reset/verify/")
    suspend fun verifyPasswordResetCode(
        @Body body: PasswordResetVerifyBody
    ): Response<PasswordResetVerifyResponse>

    @POST("api/auth/password-reset/confirm/")
    suspend fun confirmPasswordReset(
        @Body body: PasswordResetConfirmBody
    ): Response<PasswordResetConfirmResponse>

    @POST("api/auth/profile/")
    suspend fun createProfile(@Body body: ProfileRequest): Response<Unit>

    @PATCH("api/auth/profile/")
    suspend fun patchProfile(@Body body: ProfileRequest): Response<ProfileResponse>

    @Multipart
    @PATCH("api/auth/profile/")
    suspend fun patchParentAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<ProfileResponse>

    @GET("api/auth/settings/profile/")
    suspend fun getParentSettingsProfile(): ProfileResponse

    @PUT("api/auth/settings/profile/")
    suspend fun updateParentSettingsProfile(@Body body: ProfileRequest): ProfileResponse

    @PUT("api/auth/settings/change-password/")
    suspend fun changeParentPassword(@Body body: ChangePasswordRequest): Response<Unit>

    @PUT("api/auth/settings/address/")
    suspend fun updateParentAddress(@Body body: ParentAddressRequest): ParentAddressResponse

    @POST("api/auth/settings/address/")
    suspend fun createParentAddress(@Body body: ParentAddressRequest): ParentAddressResponse

    @GET("api/auth/settings/address/")
    suspend fun getParentAddress(): ParentAddressResponse

    @POST("api/auth/children/")
    suspend fun createChildProfile(@Body body: CreateChildRequest): retrofit2.Response<ChildProfileResponse>

    @PATCH("api/auth/children/{id}/")
    suspend fun patchChildProfile(
        @Path("id") id: Int,
        @Body body: CreateChildRequest
    ): Response<ChildProfileResponse>

    @GET("api/auth/children/")
    suspend fun getChildren(): List<ChildProfileResponse>

    @GET("api/auth/settings/child/")
    suspend fun getParentSettingsChild(): ChildProfileResponse

    @PUT("api/auth/settings/child/")
    suspend fun updateParentSettingsChild(@Body body: CreateChildRequest): Response<ChildProfileResponse>

    @POST("api/auth/specialist/")
    suspend fun createSpecialist(@Body body: SpecialistRequest): Response<Unit>

    @PATCH("api/auth/specialist/")
    suspend fun patchSpecialist(@Body body: SpecialistRequest): Response<SpecialistSettingsResponse>

    @Multipart
    @PATCH("api/auth/specialist/")
    suspend fun patchSpecialistAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<SpecialistSettingsResponse>

    @POST("api/auth/specialist/description/")
    suspend fun createSpecialistDescription(@Body body: SpecialistDescriptionRequest): Response<Unit>

    @PATCH("api/auth/specialist/description/")
    suspend fun patchSpecialistDescription(@Body body: SpecialistDescriptionRequest): Response<Unit>

    @GET("api/auth/settings/specialist/")
    suspend fun getSpecialistSettings(): SpecialistSettingsResponse

    @PUT("api/auth/settings/specialist/")
    suspend fun updateSpecialistSettings(@Body body: SpecialistSettingsUpdateRequest): Response<SpecialistSettingsResponse>

    /**
     * Unified multipart PUT: обновляет любые текстовые поля и опционально аватар
     * за один запрос. Массивы (`specializations`, `methods`) передаём как JSON-строку.
     * Любое поле можно не слать — бэк просто не трогает его.
     */
    @Multipart
    @PUT("api/auth/settings/specialist/")
    suspend fun updateSpecialistSettingsMultipart(
        @Part("full_name") fullName: RequestBody? = null,
        @Part("approach_description") approachDescription: RequestBody? = null,
        @Part("specializations") specializations: RequestBody? = null,
        @Part("years_experience") yearsExperience: RequestBody? = null,
        @Part("methods") methods: RequestBody? = null,
        @Part("age_range") ageRange: RequestBody? = null,
        @Part("work_format") workFormat: RequestBody? = null,
        @Part("time_zone") timeZone: RequestBody? = null,
        @Part("city") city: RequestBody? = null,
        @Part avatar: MultipartBody.Part? = null
    ): Response<SpecialistSettingsResponse>

    @Multipart
    @PUT("api/auth/settings/specialist/")
    suspend fun putSpecialistSettingsAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<SpecialistSettingsResponse>

    @POST("api/analytics/mood-trackings/")
    suspend fun createMoodTracking(@Body body: CreateMoodTrackingRequest): MoodTrackingResponse

    @GET("api/analytics/mood-trackings/")
    suspend fun getMoodTrackings(@Query("child_id") childId: Int? = null): List<MoodTrackingResponse>

    @GET("api/analytics/mood-trackings/{id}/")
    suspend fun getMoodTracking(@Path("id") id: Int): MoodTrackingResponse

    @GET("api/analytics/mood-trackings/summary/")
    suspend fun getMoodTrackingSummary(
        @Query("child_id") childId: Int? = null,
        @Query("date") date: String? = null,
        @Query("period") period: String = "week"
    ): Response<MoodTrackingSummaryResponse>

    @POST("api/chatbot/chat/")
    suspend fun chatBot(@Body body: ChatBotRequest): ChatBotResponse
}