package com.example.buddybeat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.buddybeat.databinding.MainActivityBinding
import com.example.buddybeat.playlist.PlaylistFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(PlaylistFragment(),"playlist")
    }

    fun replaceFragment(fragment: Fragment, tag: String?) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, tag)
        transaction.commit()
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment?.tag == "playlist") {
            super.onBackPressed()
        } else {
            replaceFragment(PlaylistFragment(), "playlist")
        }
    }
}