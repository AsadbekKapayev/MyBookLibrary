package com.book.example.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.book.example.util.ConnectionManager
import org.json.JSONObject
import androidx.room.Room
import com.book.example.R
import com.book.example.database.BookDatabase
import com.book.example.database.BookEntity
import com.book.example.databinding.ActivityDescriptionBinding
import com.squareup.picasso.Picasso

class DescriptionActivity : AppCompatActivity() {

    lateinit var binding: ActivityDescriptionBinding
    var bookId:String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater).also { setContentView(it.root) }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Book Details"

        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (bookId == "100") {
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id", bookId)


        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val jsonRequest =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                    try {
                        with (binding) {

                            val success = it.getBoolean("success")
                            if (success) {
                                val bookJsonObject = it.getJSONObject("book_data")
                                progressLayout.visibility = View.GONE

                                val bookImageUrl = bookJsonObject.getString("image")
                                Picasso.get().load(bookJsonObject.getString("image"))
                                    .error(R.drawable.default_book_cover).into(imgBookImage)
                                txtBookName.text = bookJsonObject.getString("name")
                                txtBookAuthor.text = bookJsonObject.getString("author")
                                txtBookPrice.text = bookJsonObject.getString("price")
                                txtBookRating.text = bookJsonObject.getString("rating")
                                txtBookDesc.text = bookJsonObject.getString("description")

                                val bookEntity = BookEntity(
                                    bookId?.toInt() as Int,
                                    txtBookName.text.toString(),
                                    txtBookAuthor.text.toString(),
                                    txtBookPrice.text.toString(),
                                    txtBookRating.text.toString(),
                                    txtBookDesc.text.toString(),
                                    bookImageUrl
                                )

                                val checkFav =
                                    DBAsyncTask(applicationContext, bookEntity, 1).execute()
                                val isFav = checkFav.get()

                                if (isFav) {
                                    btnAddToFav.text = "Return the book"
                                    val favColor = ContextCompat.getColor(
                                        applicationContext,
                                        R.color.colorFavourite
                                    )
                                    btnAddToFav.setBackgroundColor(favColor)
                                } else {
                                    btnAddToFav.text = "Rent the book"
                                    val noFavColor =
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.colorPrimary
                                        )
                                    btnAddToFav.setBackgroundColor(noFavColor)
                                }

                                btnAddToFav.setOnClickListener {

                                    if (!DBAsyncTask(
                                            applicationContext,
                                            bookEntity,
                                            1
                                        ).execute().get()
                                    ) {

                                        val async =
                                            DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                        val result = async.get()
                                        if (result) {
                                            Toast.makeText(
                                                this@DescriptionActivity,
                                                "Book added to favourites",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            btnAddToFav.text = "Return the book"
                                            val favColor = ContextCompat.getColor(
                                                applicationContext,
                                                R.color.colorFavourite
                                            )
                                            btnAddToFav.setBackgroundColor(favColor)
                                        } else {
                                            Toast.makeText(
                                                this@DescriptionActivity,
                                                "Some error occurred!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {

                                        val async =
                                            DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                        val result = async.get()

                                        if (result) {
                                            Toast.makeText(
                                                this@DescriptionActivity,
                                                "Book removed from favourites",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            btnAddToFav.text = "Rent the book"
                                            val noFavColor =
                                                ContextCompat.getColor(
                                                    applicationContext,
                                                    R.color.colorPrimary
                                                )
                                            btnAddToFav.setBackgroundColor(noFavColor)
                                        } else {
                                            Toast.makeText(
                                                this@DescriptionActivity,
                                                "Some error occurred!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
                                }

                            } else {
                                Toast.makeText(
                                    this@DescriptionActivity,
                                    "Some Error Occurred!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "Some error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }, Response.ErrorListener {

                    Toast.makeText(this@DescriptionActivity, "Volley Error $it", Toast.LENGTH_SHORT)
                        .show()

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "ee17fe916c6474"
                        return headers
                    }
                }

            queue.add(jsonRequest)
        } else {
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }
    }
    class DBAsyncTask(val context: Context , val bookEntity: BookEntity , val mode: Int) : AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg p0: Void?) :Boolean{

            when(mode){
                1 ->{
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book !=null
                }
                2 ->{
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 ->{
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}
