package com.kshitij.startup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Log.INFO
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ticcket.view.*
import java.util.logging.Level.INFO
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var database: Any
    var listNotes=ArrayList<Note>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        // Add dummy data
        //listNotes.add(Note(1," meet professor","Create any pattern of your own - tiles, texture, skin, wallpaper, comic effect, website background and more.  Change any artwork of pattern you found into different flavors and call them your own."))
        //listNotes.add(Note(2," meet doctor","Create any pattern of your own - tiles, texture, skin, wallpaper, comic effect, website background and more.  Change any artwork of pattern you found into different flavors and call them your own."))
       // listNotes.add(Note(3," meet friend","Create any pattern of your own - tiles, texture, skin, wallpaper, comic effect, website background and more.  Change any artwork of pattern you found into different flavors and call them your own."))

        //Toast.makeText(this,"onCreate",Toast.LENGTH_LONG).show()

        //Load from DB


        LoadQuery("%")


    }

    override  fun onResume() {
        super.onResume()
        LoadQuery("%")
        //Toast.makeText(this,"onResume",Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        //Toast.makeText(this,"onStart",Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()

        //Toast.makeText(this,"onPause",Toast.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()

        //Toast.makeText(this,"onStop",Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()

        //Toast.makeText(this,"onDestroy",Toast.LENGTH_LONG).show()
    }

    override fun onRestart() {
        super.onRestart()
        //Toast.makeText(this,"onRestart",Toast.LENGTH_LONG).show()
    }


    @SuppressLint("Range")
    fun LoadQuery(title:String){



        var dbManager=DbManager(this)
        val projections= arrayOf("ID","Title","Description")
        val selectionArgs= arrayOf(title)
        val cursor=dbManager.Query(projections,"Title like ?",selectionArgs,"Title")
        listNotes.clear()
        if(cursor.moveToFirst()){

            do{
                val ID=cursor.getInt(cursor.getColumnIndex("ID"))
                val Title=cursor.getString(cursor.getColumnIndex("Title"))
                val Description=cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID,Title,Description))

            }while (cursor.moveToNext())
        }

        var myNotesAdapter= MyNotesAdpater(this, listNotes)
        lvNotes.adapter=myNotesAdapter


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

          menuInflater.inflate(R.menu.main_menu, menu)

          val sv: SearchView = menu.findItem(R.id.app_bar_search).actionView as SearchView

          val sm= getSystemService(Context.SEARCH_SERVICE) as SearchManager
          sv.setSearchableInfo(sm.getSearchableInfo(componentName))
          sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
              override fun onQueryTextSubmit(query: String): Boolean {
                  Toast.makeText(applicationContext, query, Toast.LENGTH_LONG).show()
                  LoadQuery("%$query%")
                  return false
              }

              override fun onQueryTextChange(newText: String): Boolean {
                  return false
              }
          })


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item != null) {
            when(item.itemId){
                R.id.addNote->{
                    //Got to add paage
                    var intent= Intent(this,AddNotes::class.java)
                    startActivity(intent)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }


    inner class  MyNotesAdpater:BaseAdapter{
        var listNotesAdpater=ArrayList<Note>()
        var context:Context?=null
        constructor(context:Context, listNotesAdpater:ArrayList<Note>):super(){
            this.listNotesAdpater=listNotesAdpater
            this.context=context
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

            var myView=layoutInflater.inflate(R.layout.ticcket,null)

            var myNote=listNotesAdpater[p0]
            myView.tvTitle.text=myNote.nodeName
            myView.tvDes.text=myNote.nodeDes
            myView.ivDelete.setOnClickListener{
                var dbManager=DbManager(this.context!!)
                val selectionArgs= arrayOf(myNote.nodeID.toString())
                dbManager.Delete("ID=?",selectionArgs)
                LoadQuery("%")
            }
            myView.ivEdit.setOnClickListener{

                GoToUpdate(myNote)

            }
            return myView
        }

        override fun getItem(p0: Int): Any {
        return listNotesAdpater[p0]
         }

        override fun getItemId(p0: Int): Long {
           return p0.toLong()
         }

        override fun getCount(): Int {

            return listNotesAdpater.size

        }



    }


   fun GoToUpdate(note:Note){
       var intent=  Intent(this,AddNotes::class.java)
       intent.putExtra("ID",note.nodeID)
       intent.putExtra("name",note.nodeName)
       intent.putExtra("des",note.nodeDes)
       startActivity(intent)
   }

    fun init() {
        database = FirebaseFirestore.getInstance()
        Toast.makeText(applicationContext, "Checking updates", Toast.LENGTH_LONG).show()

        (database as FirebaseFirestore).collection("updates").get().addOnCompleteListener(object:
            OnCompleteListener<QuerySnapshot> {

            override fun onComplete(p0: Task<QuerySnapshot>) {
                if(p0.isSuccessful){
                    for(data in p0.result!!){
                        var avail = data.get("version")
                        try {
                            val pInfo: PackageInfo =
                                applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0)
                            val versionCode = pInfo.versionName
                            Toast.makeText(applicationContext, "current version is : "+versionCode, Toast.LENGTH_LONG).show()
                            if(avail != versionCode){
                                Toast.makeText(applicationContext, "Update available", Toast.LENGTH_LONG).show()
                                if (!(this@MainActivity as Activity).isFinishing) {
                                    MaterialAlertDialogBuilder(this@MainActivity)
                                        .setTitle("Update Available !!")
                                        .setMessage("Compulsory update avaiable,  please update app to latest version to continue!!!")
                                        .setPositiveButton("update",
                                            DialogInterface.OnClickListener { p0, p1 ->
                                                val browserIntent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(data.get("path").toString())
                                                )
                                                startActivity(browserIntent)
                                                this@MainActivity.finish()
                                                exitProcess(0)
                                            })
                                        .setNegativeButton(
                                            "No",
                                            DialogInterface.OnClickListener { p0, p1 ->
                                                //this@MainActivity.finish()
                                                //exitProcess(0)
                                            })
                                        .setCancelable(false)
                                        .show()
                                }
                            }
                            else{
                                Toast.makeText(applicationContext, "App is upto date", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                        }

                    }
                }
                else{
                    Toast.makeText(applicationContext, "Connection error", Toast.LENGTH_LONG).show()
                }

            }
        })

    }
}
