package com.example.artbookkotlin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.MacAddress
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artbookkotlin.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var artList:ArrayList<Art>
    private lateinit var artAdaptor:ArtAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)

        artList= ArrayList<Art>()
        artAdaptor= ArtAdaptor(artList)

        binding.recylerView.layoutManager=LinearLayoutManager(this)
        binding.recylerView.adapter=artAdaptor


        try {

            val database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor=database.rawQuery("SELECT*FROM arts",null)
            val artNameIx=cursor.getColumnIndex("artname")
            val idIx=cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name =cursor.getString(artNameIx)
                val id=cursor.getInt(idIx)
                val art=Art(name, id)
                artList.add(art)
            }

                artAdaptor.notifyDataSetChanged()

            cursor.close()



        }catch (e :Exception){
            e.printStackTrace()
        }

    }






    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)



        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        if (item.itemId==R.id.addArt){

            val intent= Intent(MainActivity@this,ArtActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }
}