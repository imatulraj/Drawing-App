package com.gupta.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(cntxt :Context,attrb:AttributeSet): View(cntxt,attrb) {

      private var mDrawPath: CustomPath? = null
      private var mCanvasBitmap: Bitmap? = null
      private var mDrawPaint: Paint? = null
      private var mCanvasPaint: Paint? = null
      private var mBrushSize: Float = 0.toFloat()
      private var color = Color.BLACK
      private var canvas: Canvas? = null
     private var mpaths = ArrayList<CustomPath>()
      private var mundo = ArrayList<CustomPath>()

      init {
          setoperation()
      }

      fun setoperation() {
          mDrawPaint = Paint()
          mDrawPath = CustomPath(color, mBrushSize)
          mDrawPaint!!.color = color
          mDrawPaint!!.style = Paint.Style.STROKE
          mDrawPaint!!.strokeJoin = Paint.Join.ROUND
          mDrawPaint!!.strokeCap = Paint.Cap.ROUND
          mCanvasPaint = Paint(Paint.DITHER_FLAG)
          setSizeForBrush(20.toFloat())
      }

      override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
          super.onSizeChanged(w, h, oldw, oldh)
          mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
          canvas = Canvas(mCanvasBitmap!!)
      }

      override fun onDraw(canvas: Canvas) {
          super.onDraw(canvas)
          canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)
          for (path in mpaths) {
              mDrawPaint!!.strokeWidth = path.brushThickness
              mDrawPaint!!.color = path.color
              canvas.drawPath(path, mDrawPaint!!)
          }
          if (!mDrawPath!!.isEmpty) {
              mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
              mDrawPaint!!.color = mDrawPath!!.color
              canvas.drawPath(mDrawPath!!, mDrawPaint!!)
          }
      }

      override fun onTouchEvent(event: MotionEvent?): Boolean {
          var touchx = event?.x
          var touchy = event?.y
          when (event!!.action) {
              MotionEvent.ACTION_DOWN -> {
                  mDrawPath!!.color = color
                  mDrawPath!!.brushThickness = mBrushSize
                  mDrawPath!!.reset()
                  if (touchx != null) {
                      if (touchy != null) {
                          mDrawPath!!.moveTo(touchx, touchy)
                      }
                  }

              }
              MotionEvent.ACTION_MOVE -> {
                  if (touchx != null) {
                      if (touchy != null) {
                          mDrawPath!!.lineTo(touchx, touchy)
                      }
                  }
              }
              MotionEvent.ACTION_UP -> {
                  mpaths.add(mDrawPath!!)
                  mDrawPath = CustomPath(color, mBrushSize)
              }
              else -> return false
          }
          invalidate()
          return true
      }


      inner class CustomPath(var color: Int, var brushThickness: Float) : Path()

      fun setSizeForBrush(newSize: Float) {
          mBrushSize = TypedValue.applyDimension(
                  TypedValue.COMPLEX_UNIT_DIP, newSize,
                  resources.displayMetrics
          )
          mDrawPaint!!.strokeWidth = mBrushSize
      }

      fun setcolorforBrush(newcolor: String) {
          color = Color.parseColor(newcolor)
          mDrawPaint!!.color = color
      }

      fun undopaths() {
          if (mpaths.isNotEmpty()) {
              mundo.add(mpaths.removeAt(mpaths.size - 1))
          }
          invalidate()
      }

  }
