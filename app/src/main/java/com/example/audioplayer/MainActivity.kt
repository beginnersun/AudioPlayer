package com.example.audioplayer


import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.example.audioplayer.adapter.FragmentAdapter
import com.example.audioplayer.fragment.VoiceFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),ViewPager.OnPageChangeListener {

    private var oldMenuItem:MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view_pager.apply {
            adapter = FragmentAdapter(arrayListOf(VoiceFragment()),supportFragmentManager)
            addOnPageChangeListener(this@MainActivity)
        }

        navigation_bottom.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.item_voice -> view_pager.currentItem = 0
                R.id.item_merge -> view_pager.currentItem = if(view_pager.size > 1) 1 else 0
                R.id.item_more -> view_pager.currentItem = if(view_pager.size > 2) 2 else 0
                R.id.item_my -> view_pager.currentItem = if(view_pager.size > 3) 3 else 0
            }
            false
        }

    }


    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (oldMenuItem!=null){
            oldMenuItem!!.isChecked = false
        }
        navigation_bottom.menu[position].isChecked = true
        oldMenuItem = navigation_bottom.menu[position]
    }

}
