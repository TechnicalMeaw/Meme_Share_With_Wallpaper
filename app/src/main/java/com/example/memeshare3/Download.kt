package com.example.memeshare3

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_download.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class Download : AppCompatActivity() {


    var imageCategory: String? = null

    var currentUrl: String? = null

    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val url = "https://meme-api.herokuapp.com/gimme"


    private var Custom = false

    var width = String()
    var height  = String()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        setSupportActionBar(findViewById(R.id.appBar))

        ActivityCompat.requestPermissions(this, permissions, 0)


        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

            if (memeImageView.drawable != null) {
                Snackbar.make(view, "Downloading Image...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
                downloadImage()
            }else{
                Snackbar.make(view, "Nothing to Download...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
            }
        }

        imageCategory = intent.getStringExtra("category")
        Toast.makeText(this, "Showing $imageCategory" + "s", Toast.LENGTH_SHORT).show()

        loadMeme()
    }

    private fun downloadImage(){
        val image: Bitmap = getBitmapFromView(memeImageView)
        saveToGallery(this, image, "Meme Share")
    }



    private fun getBitmapFromView(view: ImageView): Bitmap{
        val bitmap = createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap.trim(color = Color.TRANSPARENT)
    }


    /**
     * Trims a bitmap borders of a given color.
     *
     */
    private fun Bitmap.trim(@ColorInt color: Int = Color.TRANSPARENT): Bitmap {

        var top = height
        var bottom = 0
        var right = width
        var left = 0

        var colored = IntArray(width, { color })
        var buffer = IntArray(width)

        for (y in bottom until top) {
            getPixels(buffer, 0, width, 0, y, width, 1)
            if (!colored.contentEquals(buffer)) {
                bottom = y
                break
            }
        }

        for (y in top - 1 downTo bottom) {
            getPixels(buffer, 0, width, 0, y, width, 1)
            if (!Arrays.equals(colored, buffer)) {
                top = y
                break
            }
        }

        val heightRemaining = top - bottom
        colored = IntArray(heightRemaining, { color })
        buffer = IntArray(heightRemaining)

        for (x in left until right) {
            getPixels(buffer, 0, 1, x, bottom, 1, heightRemaining)
            if (!Arrays.equals(colored, buffer)) {
                left = x
                break
            }
        }

        for (x in right - 1 downTo left) {
            getPixels(buffer, 0, 1, x, bottom, 1, heightRemaining)
            if (!Arrays.equals(colored, buffer)) {
                right = x
                break
            }
        }
        return Bitmap.createBitmap(this, left, bottom, right - left, top - bottom)
    }




    private fun loadMeme(){

        findViewById<FloatingActionButton>(R.id.fab).visibility = View.GONE

        if (progressBar.isVisible){
            Toast.makeText(this, "Taking longer than expected", Toast.LENGTH_LONG).show()
        }else {
            progressBar.visibility = View.VISIBLE
        }
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)


// Request a string response from the provided URL.
        val stringRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Display the first 500 characters of the response string.
                if (imageCategory == "Meme") {
                    currentUrl = response.getString("url")
                } else if (Custom){
                    currentUrl =
                        "https://picsum.photos/$width/$height/?random&t=" + Date().time.toString()
                    Custom = false
                }else {
                    currentUrl =
                        "https://picsum.photos/${memeImageView.width}/${memeImageView.height}/?random&t=" + Date().time.toString()
                }

                // For a simple view:


                Glide.with(this).load(currentUrl).listener(object: RequestListener<Drawable>{
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        loadMeme()
                        return true
                    }

                }).into(memeImageView)


            },
            {
                Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show()
            })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


    fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {
        val filename = "${System.currentTimeMillis()}.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DCIM}/$albumName"
                )
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }

        Handler().postDelayed({
            Toast.makeText(this, "Image saved at: DCIM/Meme Share", Toast.LENGTH_LONG).show()
        }, 150)

    }

    private fun shareImage(){
        val bitmap: Bitmap = getBitmapFromView(memeImageView)
        // save bitmap to cache directory

        // save bitmap to cache directory
        try {
            val cachePath: File = File(this.getCacheDir(), "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val imagePath: File = File(this.getCacheDir(), "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri =
            FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.setAction(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            if (imageCategory == "Meme") {
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey check out this meme I found on Meme Share")
            }else{
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey check out this image I found on Meme Share")
            }

            startActivity(Intent.createChooser(shareIntent, "Share image via..."))
        }
    }

    fun clickShare(view: View) {

        if(memeImageView.drawable == null){
            Snackbar.make(view, "Nothing to Share...", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }else {
            shareImage()
        }
    }

    fun clickNext(view: View) {
        loadMeme()
    }


    //for options menu
    private var globalMenuItem: Menu? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.appbarmenu, menu)

        globalMenuItem = menu;

        globalMenuItem!!.findItem(R.id.btnSize).isVisible = imageCategory == "Image"

        return true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.btnAbout -> {


            Toast.makeText(this, "Meme Share v1.0.0", Toast.LENGTH_SHORT).show()
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        R.id.Server1 -> {

            if (imageCategory != "Meme") {
                imageCategory = "Meme"

                globalMenuItem!!.findItem(R.id.btnOk).isVisible = false
                globalMenuItem!!.findItem(R.id.btnBack).isVisible = false
                closeKeyboard(editTextHeight)
                closeKeyboard(editTextWidth)
                editTextWidth.visibility = View.GONE
                editTextHeight.visibility = View.GONE

                globalMenuItem!!.findItem(R.id.btnSize).isVisible = false
                Toast.makeText(this, "Now showing Memes", Toast.LENGTH_SHORT).show()
                loadMeme()
            }
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        R.id.Server2 -> {

            if (imageCategory != "Image") {
                imageCategory = "Image"
                globalMenuItem!!.findItem(R.id.btnSize).isVisible = true
                Toast.makeText(this, "Now showing Images", Toast.LENGTH_SHORT).show()
                loadMeme()
            }

            // User chose the "Settings" item, show the app settings UI...
            true
        }

        R.id.btnSize -> {

            globalMenuItem!!.findItem(R.id.btnOk).isVisible = true
            globalMenuItem!!.findItem(R.id.btnBack).isVisible = true
            editTextWidth.visibility = View.VISIBLE
            editTextHeight.visibility = View.VISIBLE
            editTextWidth.requestFocus()
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        R.id.btnExit -> {

            finish()
            System.exit(0)
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            true
        }

        R.id.btnOk -> {


            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            height = editTextHeight.text.toString()
            width = editTextWidth.text.toString()
            item.isVisible = false
            globalMenuItem!!.findItem(R.id.btnBack).isVisible = false

            closeKeyboard(editTextHeight)
            closeKeyboard(editTextWidth)
            editTextWidth.visibility = View.GONE
            editTextHeight.visibility = View.GONE

            if (height != null && width != null) {
                Custom = true
            }
            loadMeme()
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            true
        }

        R.id.btnBack -> {

            closeKeyboard(editTextHeight)
            closeKeyboard(editTextWidth)
            editTextWidth.visibility = View.GONE
            editTextHeight.visibility = View.GONE
            item.setVisible(false)
            globalMenuItem!!.findItem(R.id.btnOk).setVisible(false);
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            true
        }



        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    //for closing keyboard

    private fun closeKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}