package com.archives

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.*
import android.view.animation.Animation.AnimationListener
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var startScreenImageView: ImageView = ImageView(this)
        var exploreButton: Button = Button(this)
        startScreenImageView.scaleType = ImageView.ScaleType.FIT_XY;
        startScreenImageView = findViewById<ImageView>(R.id.splashScreenImage)
        startScreenImageView.setImageResource(R.drawable.old_construction_workers)
        val pictures: IntArray = intArrayOf(R.drawable.coloseum,R.drawable.iwagima,
            R.drawable.old_construction_workers,R.drawable.jesse_owens)
        animate(startScreenImageView,pictures,1,true)

        exploreButton = findViewById<Button>(R.id.explore)
        exploreButton.setOnClickListener {
             startActivity(Intent(this@MainActivity,MapsActivity::class.java))
        }
    }

    private fun animate(imageView: ImageView, images: IntArray, imageIndex: Int, forever: Boolean) {
        // Configure values for fading between pictures
        val fadeInDuration = 500
        val timeBetween = 4000
        val fadeOutDuration = 1000
        imageView.visibility = View.INVISIBLE
        imageView.setImageResource(images[imageIndex])


        val fadeIn: Animation = AlphaAnimation(0.0f,1.0f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = fadeInDuration.toLong()

        val fadeOut: Animation = AlphaAnimation(1.0f, 0.0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.startOffset = (fadeInDuration + timeBetween).toLong()
        fadeOut.duration = fadeOutDuration.toLong()

        val animation = AnimationSet(false)
        animation.addAnimation(fadeIn)
        animation.addAnimation(fadeOut)

        animation.repeatCount = 1
        imageView.animation = animation
        animation.setAnimationListener(object : AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                if (images.size - 1 > imageIndex) {
                    animate(
                        imageView,
                        images,
                        imageIndex + 1,
                        forever
                    )
                } else {
                    if (forever) {
                        animate(
                            imageView,
                            images,
                            0,
                            forever
                        )
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation) {
                // TODO Auto-generated method stub
            }

            override fun onAnimationStart(animation: Animation) {
                // TODO Auto-generated method stub
            }
        })
    }


}