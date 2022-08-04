package com.example.artbookkotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.artbookkotlin.databinding.ActivityArtBinding
import com.example.artbookkotlin.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.jar.Manifest

class ArtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher:ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    var selectedBitmap:Bitmap?=null
    private lateinit var database:SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityArtBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)


        registerLauncher()



        val intent=intent
        val info=intent.getStringExtra("info")
        if (info.equals("new")){
            binding.nameText.setText("")
            binding.artistNameText.setText("")
            binding.yearText.setText("")
            binding.save.visibility=View.VISIBLE
            binding.imageView2.setImageResource(R.drawable.select)


        }else{
            binding.save.visibility=View.INVISIBLE
            val selectedId=intent.getIntExtra("id",1)
            val cursor=database.rawQuery("SELECT*FROM arts WHERE id =?", arrayOf(selectedId.toString()))

            val artNameIx=cursor.getColumnIndex("artname")
            val artistNameIx=cursor.getColumnIndex("artistname")
            val yearIx=cursor.getColumnIndex("year")
            val imageIx=cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.nameText.setText(cursor.getString(artNameIx))
                binding.artistNameText.setText(cursor.getString(artistNameIx))
                binding.yearText.setText(cursor.getString(yearIx))
                val byteArray=cursor.getBlob(imageIx)
                val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView2.setImageBitmap(bitmap)
            }
            cursor.close()


        }


    }

    fun save (view: View){
        val artName=binding.nameText.text.toString()
        val artistName=binding.artistNameText.text.toString()
        val year=binding.yearText.text.toString()

        if (selectedBitmap!=null){
            val smallBitmap=makeSmallerBitmap(selectedBitmap!!,300)
            val outputStream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR,year VARCHAR, image BLOB )")
                val sqlString="INSERT INTO arts(artname,artistname,year,image) VALUES (?,?,?,?)"
                val statment=database.compileStatement(sqlString)
                statment.bindString(1,artName)
                statment.bindString(2,artistName)
                statment.bindString(3,year)
                statment.bindBlob(4,byteArray)
                statment.execute()



            }catch (e :Exception){
                e.printStackTrace()
            }
            val intent=Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)


        }

    }
    private fun makeSmallerBitmap(image:Bitmap,maxsimumSize:Int):Bitmap{
        var width=image.width
        var height=image.height

        val bitmapRatio:Double=width.toDouble()/height.toDouble()

        if (bitmapRatio > 1 ){
            width=maxsimumSize
            val scaledHeight=width/bitmapRatio
            height=scaledHeight.toInt()
        }else{
            height=maxsimumSize
            val scaledWidth=height*bitmapRatio
            width=scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)

    }

    fun selectImage(view: View){
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE))


            {
                Snackbar.make(view,"Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                    permissionLauncher.launch( android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }



       } else{
            val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)

        }


    }
    private fun registerLauncher(){

        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode== RESULT_OK){
                val intentFromResult=result.data
                if (intentFromResult!=null){
                    val imageData=intentFromResult.data
                    if (imageData!=null){
                        try {
                            if (Build.VERSION.SDK_INT>=28){
                                val source=ImageDecoder.createSource(contentResolver,imageData)
                                selectedBitmap=ImageDecoder.decodeBitmap(source)
                                binding.imageView2.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap=MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView2.setImageBitmap(selectedBitmap)
                            }

                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }

        }
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if (result){
                val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)




            }else{
                Toast.makeText(this@ArtActivity,"Permission Needed",Toast.LENGTH_LONG).show()
            }

        }

    }

  }





