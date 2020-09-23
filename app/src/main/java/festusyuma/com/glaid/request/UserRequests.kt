package festusyuma.com.glaid.request

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import festusyuma.com.glaid.API_LOG_TAG
import festusyuma.com.glaid.R
import festusyuma.com.glaid.defaultRetryPolicy
import festusyuma.com.glaid.gson
import festusyuma.com.glaid.helpers.Api
import festusyuma.com.glaid.model.User
import org.json.JSONObject

class UserRequests(private val c: Activity): Authentication(c) {

    fun uploadImage(imageUri: Uri, callback: (response: String) -> Unit) {
        if (!operationRunning) {
            setLoading(true)

            getAuthentication { authorization ->

                cloudinaryUpload(imageUri) { imageUrl ->
                    val imageUploadJson = JSONObject(gson.toJson(mapOf("imageUrl" to imageUrl)))
                    val req = object : JsonObjectRequest(
                        Method.POST,
                        Api.UPLOAD_IMAGE,
                        imageUploadJson,
                        Response.Listener { response ->
                            if (response.getInt("status") == 200) {
                                val dataPref = c.getSharedPreferences(c.getString(R.string.cached_data), Context.MODE_PRIVATE)
                                val userJson = dataPref.getString(c.getString(R.string.sh_user_details), null)
                                if (userJson != null) {
                                    val user = gson.fromJson(userJson, User::class.java)
                                    user.profileImage = imageUrl

                                    with(dataPref.edit()) {
                                        putString(c.getString(R.string.sh_user_details), gson.toJson(user))
                                        commit()
                                    }
                                }

                                callback(imageUrl)
                            }else showError(response.getString("message"))
                        },
                        Response.ErrorListener { response->
                            if (response.networkResponse != null) {
                                showError(c.getString(R.string.error_occurred))
                                response.printStackTrace()
                            }else showError(c.getString(R.string.internet_error_msg))
                        }
                    ){
                        override fun getHeaders(): MutableMap<String, String> {
                            return authorization
                        }
                    }

                    req.retryPolicy = defaultRetryPolicy
                    req.tag = "image_upload"
                    queue.add(req)
                }
            }
        }
    }

    private fun cloudinaryUpload(imageUri: Uri, callback: (response: String) -> Unit) {
        MediaManager.get().upload(imageUri).unsigned("glaid_upload").callback(
            object: UploadCallback {
                override fun onSuccess(
                    requestId: String?,
                    resultData: MutableMap<Any?, Any?>?
                ) {
                    if (resultData != null) {
                        val url = resultData["secure_url"].toString()
                        callback(url)
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    showError("An error occurred")
                }

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) {
                    Log.v(API_LOG_TAG, "Progress: ${(bytes/totalBytes) * 100}%")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                override fun onStart(requestId: String?) {}

            }
        ).dispatch()
    }
}