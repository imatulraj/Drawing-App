package com.gupta.drawingapp

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BitmapCompat
import androidx.core.view.iterator
import androidx.core.view.marginTop
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brush_size_getter.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
        brush_buttom.setOnClickListener {
            ChangeBrushSize()
            var tost=Toast.makeText(this," BRUSH SIZE:$progrs",Toast.LENGTH_SHORT)
            tost.setMargin(0f,0f)
            tost.show()
        }
        gallary_pick.setOnClickListener {
            checkPermissions()
            loadImage()
        }
        undooption.setOnClickListener {
            drawingpage.undopaths()
        }
        save.setOnClickListener {
            checkPermissions()
            Savebitmapfromview(frameLayout_drawing_view)

        }
    }
var IMAGE_PICKER_AND_SAVER=1234
    private fun checkPermissions() {
            if (ActivityCompat.checkSelfPermission(applicationContext, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(applicationContext, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)
                {
                ActivityCompat.requestPermissions(this,
                    arrayOf("android.permission.READ_EXTERNAL_STORAGE","android.permission.WRITE_EXTERNAL_STORAGE"),
                    IMAGE_PICKER_AND_SAVER
                )
                return
        }


    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            IMAGE_PICKER_AND_SAVER -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show()
                } else {
                }
            }
            else->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    val PICK_IMAGE_CODE=123
    fun loadImage(){
        var intent= Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==PICK_IMAGE_CODE  && data!=null && resultCode == RESULT_OK){

            val selectedImage=data.data
            val filePathColum= arrayOf(MediaStore.Images.Media.DATA)
            val cursor= contentResolver.query(selectedImage!!,filePathColum,null,null,null)
            cursor!!.moveToFirst()
            val coulomIndex=cursor.getColumnIndex(filePathColum[0])
            val picturePath=cursor.getString(coulomIndex)
            cursor.close()
            image.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }

    }


    var progrs:Int=0
    private fun ChangeBrushSize() {
        var brushdialog=Dialog(this)
        brushdialog.setContentView(R.layout.brush_size_getter)
        brushdialog.setTitle("BRUSH SIZE : ")
        var smllbrsh=brushdialog.small_brush
        smllbrsh.setOnClickListener {
            drawingpage.setSizeForBrush(10.toFloat())
            progrs=10
            brushdialog.dismiss()

        }
        var medium=brushdialog.medium_brush
        medium.setOnClickListener {
            drawingpage.setSizeForBrush(20.toFloat())
            progrs=20
            brushdialog.dismiss()
        }
        var large=brushdialog.large_brush
        large.setOnClickListener {
            drawingpage.setSizeForBrush(30.toFloat())
            progrs=30
            brushdialog.dismiss()
        }

        var progress_seeker=brushdialog.seek_progress
        progress_seeker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                        drawingpage.setSizeForBrush(progress.toFloat())
                    progrs= progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                        brushdialog.dismiss()
            }

        })

        brushdialog.show()

        progress_seeker.setProgress(progrs,true)
    }

    fun defaultborder(){
        for(image in ll_pair_color)
        {
            var bt=image as ImageButton
            val param = bt.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0,0,0,0)
            bt.layoutParams = param
            bt.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.normal_pallet))
        }

    }
    fun colorchange(view: View) {
        var colorbutton =view as ImageButton
        defaultborder()
        val param = colorbutton.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(10,10,10,10)
        colorbutton.layoutParams = param
        drawingpage.setcolorforBrush(colorbutton.tag.toString())
        colorbutton.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.selected_pallet))
        Toast.makeText(applicationContext, "${colorbutton.tag}", Toast.LENGTH_SHORT).show()
    }
    fun Savebitmapfromview(view:View) {
        var returnBitmap=Bitmap.createBitmap(
                view.width,view.height,
                Bitmap.Config.ARGB_8888)
        var canvas=Canvas(returnBitmap)
        var bgdrawable=view.background
        if(bgdrawable!=null)
        {
            bgdrawable.draw(canvas)
        }
        else
        {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        BitmapAsyncTask(returnBitmap).execute()
    }
    inner class BitmapAsyncTask(val mBitmap:Bitmap):AsyncTask<Any,Void,String>(){
        private lateinit var dialogprogress:Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            showpregress()
        }
        override fun doInBackground(vararg params: Any?): String {
                    var result=""
            if(mBitmap!= null)
            {
                try {
                        var bytes=ByteArrayOutputStream()
                        mBitmap.compress(Bitmap.CompressFormat.PNG,100,bytes)
                        var f=File(externalCacheDir!!.absoluteFile.toString()
                                +File.separator+"DrawingApp"+
                                System.currentTimeMillis() + ".png")
                            var fos=FileOutputStream(f)
                                fos.write(bytes.toByteArray())
                                fos.close()
                                result=f.absolutePath
                }catch (E:Exception){}
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelprogress()
            if(!result!!.isEmpty()){
                Toast.makeText(applicationContext, "FILE IS SAVED :$result", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext, "FILE IS NOT SAVED", Toast.LENGTH_SHORT).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null)
            { path: String, uri: Uri ->
                val shareintent=Intent(Intent.ACTION_SEND)
                shareintent.putExtra(Intent.EXTRA_STREAM,uri)
                shareintent.type="image/png"
                startActivity(Intent.createChooser(shareintent,"Share"))
            }

//            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null)
//            { path, uri ->
//                val shareIntent = Intent()
//                shareIntent.action = Intent.ACTION_SEND
//                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
//                shareIntent.type = "image/jpeg"
//                startActivity(Intent.createChooser(shareIntent, "Share")
//                )
//            }


        }
        fun showpregress(){
            dialogprogress= Dialog(this@MainActivity)
            dialogprogress.setContentView(R.layout.dialog_custom)
            dialogprogress.show()
        }
        fun cancelprogress(){
            dialogprogress.dismiss()
        }

    }

}