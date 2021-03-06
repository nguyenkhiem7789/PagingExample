package com.nguyen.pagingexample.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.nguyen.pagingexample.R

class CircleImageView : AppCompatImageView {
    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()
    private val mShaderMatrix = Matrix()
    private val mBitmapPaint: Paint? = Paint()
    private val mBorderPaint = Paint()
    private val mCircleBackgroundPaint = Paint()
    private var mBorderColor: Int = DEFAULT_BORDER_COLOR
    private var mBorderWidth: Int = DEFAULT_BORDER_WIDTH
    private var mCircleBackgroundColor: Int = DEFAULT_CIRCLE_BACKGROUND_COLOR
    private var mBitmap: Bitmap? = null
    private var mBitmapShader: BitmapShader? = null
    private var mBitmapWidth = 0
    private var mBitmapHeight = 0
    private var mDrawableRadius = 0f
    private var mBorderRadius = 0f
    private var mColorFilter: ColorFilter? = null
    private var mReady = false
    private var mSetupPending = false
    private var mBorderOverlay = false
    private var mDisableCircularTransformation = false

    constructor(context: Context?) : super(context) {
        init()
    }

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int = 0
    ) : super(context, attrs, defStyle) {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)
        mBorderWidth = a.getDimensionPixelSize(
            R.styleable.CircleImageView_civ_border_width,
            DEFAULT_BORDER_WIDTH
        )
        mBorderColor = a.getColor(
            R.styleable.CircleImageView_civ_border_color,
            DEFAULT_BORDER_COLOR
        )
        mBorderOverlay = a.getBoolean(
            R.styleable.CircleImageView_civ_border_overlay,
            DEFAULT_BORDER_OVERLAY
        )
        if (a.hasValue(R.styleable.CircleImageView_civ_circle_background_color)) {
            mCircleBackgroundColor = a.getColor(
                R.styleable.CircleImageView_civ_circle_background_color,
                DEFAULT_CIRCLE_BACKGROUND_COLOR
            )
        } else if (a.hasValue(R.styleable.CircleImageView_civ_fill_color)) {
            mCircleBackgroundColor = a.getColor(
                R.styleable.CircleImageView_civ_fill_color,
                DEFAULT_CIRCLE_BACKGROUND_COLOR
            )
        }
        a.recycle()
        init()
    }

    private fun init() {
        super.setScaleType(SCALE_TYPE)
        mReady = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(OutlineProvider())
        }
        if (mSetupPending) {
            setup()
            mSetupPending = false
        }
    }

    var mScaleType: ScaleType
        get() = SCALE_TYPE
        set(scaleType) {
            require(!(scaleType !== SCALE_TYPE)) {
                String.format(
                    "ScaleType %s not supported.",
                    scaleType
                )
            }
        }

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        require(!adjustViewBounds) { "adjustViewBounds not supported." }
    }

    protected override fun onDraw(canvas: Canvas) {
        if (mDisableCircularTransformation) {
            super.onDraw(canvas)
            return
        }
        if (mBitmap == null) {
            return
        }
        if (mCircleBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(
                mDrawableRect.centerX(),
                mDrawableRect.centerY(),
                mDrawableRadius,
                mCircleBackgroundPaint
            )
        }
        canvas.drawCircle(
            mDrawableRect.centerX(),
            mDrawableRect.centerY(),
            mDrawableRadius,
            mBitmapPaint!!
        )
        if (mBorderWidth > 0) {
            canvas.drawCircle(
                mBorderRect.centerX(),
                mBorderRect.centerY(),
                mBorderRadius,
                mBorderPaint
            )
        }
    }

    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
    }

    var borderColor: Int
        get() = mBorderColor
        set(borderColor) {
            if (borderColor == mBorderColor) {
                return
            }
            mBorderColor = borderColor
            mBorderPaint.color = mBorderColor
            invalidate()
        }

    @Deprecated("Use {@link #setBorderColor(int)} instead")
    fun setBorderColorResource(@ColorRes borderColorRes: Int) {
        borderColor = getContext().getResources().getColor(borderColorRes)
    }

    var circleBackgroundColor: Int
        get() = mCircleBackgroundColor
        set(circleBackgroundColor) {
            if (circleBackgroundColor == mCircleBackgroundColor) {
                return
            }
            mCircleBackgroundColor = circleBackgroundColor
            mCircleBackgroundPaint.color = circleBackgroundColor
            invalidate()
        }

    private fun setCircleBackgroundColorResource(@ColorRes circleBackgroundRes: Int) {
        circleBackgroundColor = getContext().getResources().getColor(circleBackgroundRes)
    }

    /**
     * Return the color drawn behind the circle-shaped drawable.
     *
     * @return The color drawn behind the drawable
     *
     */
    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColor The color to be drawn behind the drawable
     *
     */
    @get:Deprecated("Use {@link #getCircleBackgroundColor()} instead.")
    @set:Deprecated("Use {@link #setCircleBackgroundColor(int)} instead.")
    var fillColor: Int
        get() = circleBackgroundColor
        set(fillColor) {
            circleBackgroundColor = fillColor
        }

    /**
     * Set a color to be drawn behind the circle-shaped drawable. Note that
     * this has no effect if the drawable is opaque or no drawable is set.
     *
     * @param fillColorRes The color resource to be resolved to a color and
     * drawn behind the drawable
     *
     */
    @Deprecated("Use {@link #setCircleBackgroundColorResource(int)} instead.")
    fun setFillColorResource(@ColorRes fillColorRes: Int) {
        setCircleBackgroundColorResource(fillColorRes)
    }

    var borderWidth: Int
        get() = mBorderWidth
        set(borderWidth) {
            if (borderWidth == mBorderWidth) {
                return
            }
            mBorderWidth = borderWidth
            setup()
        }

    var isBorderOverlay: Boolean
        get() = mBorderOverlay
        set(borderOverlay) {
            if (borderOverlay == mBorderOverlay) {
                return
            }
            mBorderOverlay = borderOverlay
            setup()
        }

    var isDisableCircularTransformation: Boolean
        get() = mDisableCircularTransformation
        set(disableCircularTransformation) {
            if (mDisableCircularTransformation == disableCircularTransformation) {
                return
            }
            mDisableCircularTransformation = disableCircularTransformation
            initializeBitmap()
        }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        initializeBitmap()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
    }

    private fun applyColorFilter() {
        if (mBitmapPaint != null) {
            mBitmapPaint.colorFilter = colorFilter
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else try {
            val bitmap: Bitmap
            bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(
                    COLORDRAWABLE_DIMENSION,
                    COLORDRAWABLE_DIMENSION,
                    BITMAP_CONFIG
                )
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    BITMAP_CONFIG
                )
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initializeBitmap() {
        mBitmap = if (mDisableCircularTransformation) {
            null
        } else {
            getBitmapFromDrawable(getDrawable())
        }
        setup()
    }

    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }
        if (getWidth() === 0 && getHeight() === 0) {
            return
        }
        if (mBitmap == null) {
            invalidate()
            return
        }
        mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapPaint!!.isAntiAlias = true
        mBitmapPaint.shader = mBitmapShader
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()
        mCircleBackgroundPaint.style = Paint.Style.FILL
        mCircleBackgroundPaint.isAntiAlias = true
        mCircleBackgroundPaint.color = mCircleBackgroundColor
        mBitmapHeight = mBitmap!!.height
        mBitmapWidth = mBitmap!!.width
        mBorderRect.set(calculateBounds())
        mBorderRadius = Math.min(
            (mBorderRect.height() - mBorderWidth) / 2.0f,
            (mBorderRect.width() - mBorderWidth) / 2.0f
        )
        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(mBorderWidth - 1.0f, mBorderWidth - 1.0f)
        }
        mDrawableRadius =
            Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f)
        applyColorFilter()
        updateShaderMatrix()
        invalidate()
    }

    private fun calculateBounds(): RectF {
        val availableWidth: Int = getWidth() - getPaddingLeft() - getPaddingRight()
        val availableHeight: Int = getHeight() - getPaddingTop() - getPaddingBottom()
        val sideLength = Math.min(availableWidth, availableHeight)
        val left: Float = getPaddingLeft() + (availableWidth - sideLength) / 2f
        val top: Float = getPaddingTop() + (availableHeight - sideLength) / 2f
        return RectF(left, top, left + sideLength, top + sideLength)
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f
        mShaderMatrix.set(null)
        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight.toFloat()
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / mBitmapWidth.toFloat()
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }
        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate(
            (dx + 0.5f).toInt() + mDrawableRect.left,
            (dy + 0.5f).toInt() + mDrawableRect.top
        )
        mBitmapShader!!.setLocalMatrix(mShaderMatrix)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return inTouchableArea(event.x, event.y) && super.onTouchEvent(event)
    }

    private fun inTouchableArea(x: Float, y: Float): Boolean {
        return Math.pow(x - mBorderRect.centerX().toDouble(), 2.0) +
                Math.pow(y - mBorderRect.centerY().toDouble(), 2.0) <= Math.pow(mBorderRadius.toDouble(), 2.0)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private inner class OutlineProvider : ViewOutlineProvider() {
        override fun getOutline(
            view: View,
            outline: Outline
        ) {
            val bounds = Rect()
            mBorderRect.roundOut(bounds)
            outline.setRoundRect(bounds, bounds.width() / 2.0f)
        }
    }

    companion object {
        private val SCALE_TYPE: ScaleType = ScaleType.CENTER_CROP
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLORDRAWABLE_DIMENSION = 2
        private const val DEFAULT_BORDER_WIDTH = 0
        private const val DEFAULT_BORDER_COLOR = Color.BLACK
        private const val DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.TRANSPARENT
        private const val DEFAULT_BORDER_OVERLAY = false
    }
}