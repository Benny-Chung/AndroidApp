package hkucs.example.comp3330project

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import hkucs.example.comp3330project.databinding.ActivityMainBinding

//app:showAsAction="ifRoom|collapseActionView"
class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    public var date : String? = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val go_frag = intent.getStringExtra("to")
        if(go_frag=="ListFragment") {
            replaceFragment(ListFragment())
        } else if (go_frag=="ChatbotFragment") {
            replaceFragment(ChatbotFragment())
        }
        else {
            replaceFragment(SummaryFragment())
        }
        intent.removeExtra("to")
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.foodlist_item -> replaceFragment(ListFragment())
                R.id.summary_item -> replaceFragment(SummaryFragment())
                R.id.chatbot_item -> replaceFragment(ChatbotFragment())
                else -> {}
            }
            true

        }


        //initilize food database
        DataBaseCopy(this)?.CopyDBFile()

    }
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }


}