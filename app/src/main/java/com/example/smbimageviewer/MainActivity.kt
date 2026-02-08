package com.example.smbimageviewer

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.smbimageviewer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val repository = SmbImageRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imagePager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        loadImages()
    }

    private fun loadImages() {
        lifecycleScope.launch {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.errorText.visibility = View.GONE
            binding.imagePager.visibility = View.GONE

            val result = repository.fetchImages()
            if (result.isSuccess) {
                val images = result.getOrDefault(emptyList())
                if (images.isEmpty()) {
                    showError("No images found in the SMB folder.")
                } else {
                    binding.imagePager.adapter = SmbImagePagerAdapter(images)
                    binding.imagePager.visibility = View.VISIBLE
                }
            } else {
                showError(getString(R.string.error_loading_images))
            }
            binding.loadingIndicator.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }
}
