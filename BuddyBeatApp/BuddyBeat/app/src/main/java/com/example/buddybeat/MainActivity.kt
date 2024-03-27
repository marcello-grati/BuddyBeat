package com.example.buddybeat

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.buddybeat.data.models.Song
import com.example.buddybeat.databinding.MainActivityBinding
import com.example.buddybeat.playlist.PlaylistFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(PlaylistFragment(this),"playlist")
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
            replaceFragment(PlaylistFragment(this), "playlist")
        }
    }

    //creazione del menu che contiene le opzioni "Aggiungi canzone" e "Elimina canzone"
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(applicationContext).inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    //metodo chiamato quando una delle opzioni del menu viene selezionata
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add -> {
                //se seleziono la voce "Aggiungi canzone" controllo se ci sono i permessi per leggere la memoria esterna e
                //se non ci sono viene richiesto all'utente di consentire l'accesso alla memoria. Se viene dato il permesso,
                //chiamo il metodo addMusic() che gestisce l'aggiunta di una canzone
                addMusic()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addMusic() {
        val mockSongs = ArrayList<Song>()
        mockSongs.add(
            Song(
                artist = "Taylor Swift",
                description = "First song",
                duration = "3:31",
                title = "Antihero",
                uri = "test"
            )
        )
        mockSongs.add(
            Song(
                artist = "Harry Styles",
                description = "Second song",
                duration = "2:58",
                title = "Kiwi",
                uri = "test"
            )
        )
        mockSongs.add(
            Song(
                artist = "Teddy Swims",
                description = "Third song",
                duration = "2:23",
                title = "Lose Control",
                uri = "test"
            )
        )
        mockSongs.add(
            Song(
                artist = "Ariana Grande",
                description = "Fourth song",
                duration = "3:31",
                title = "the boy is mine",
                uri = "test"
            )
        )
        mockSongs.add(
            Song(
                artist = "Taylor Swift",
                description = "Fifth song",
                duration = "4:01",
                title = "Cruel Summer",
                uri = "test"
            )
        )
        mockSongs.add(
            Song(
                artist = "Noah Kahan",
                description = "Sixth song",
                duration = "3:31",
                title = "Stick Season",
                uri = "test"
            )
        )
        mockSongs.add(
            Song(
                artist = "Duo Lipa",
                description = "Seventh song",
                duration = "3:42",
                title = "Houdini",
                uri = "test"
            )
        )
        val f1: Fragment? =
            supportFragmentManager.findFragmentByTag("playlist")
        val index = supportFragmentManager.fragments.indexOf(f1)
        val f = supportFragmentManager.fragments.get(index) as PlaylistFragment
        f.addMusic(mockSongs)
    }
}