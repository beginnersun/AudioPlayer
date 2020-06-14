package com.example.audioplayer.ui


import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.example.audioplayer.R
import com.example.audioplayer.adapter.FragmentAdapter
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.ui.fragment.MergeFragment
import com.example.audioplayer.ui.fragment.MyFragment
import com.example.audioplayer.ui.fragment.VoiceFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ViewPager.OnPageChangeListener {

    private var oldMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view_pager.apply {
            adapter = FragmentAdapter(
                arrayListOf(VoiceFragment(), MergeFragment(), MyFragment()),
                supportFragmentManager
            )
            addOnPageChangeListener(this@MainActivity)
        }

        navigation_bottom.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_voice -> view_pager.currentItem = 0
                R.id.item_merge -> view_pager.currentItem = 1
                R.id.item_my -> {
                    view_pager.currentItem = 2
                }
            }
            false
        }
    }


    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (oldMenuItem != null) {
            oldMenuItem!!.isChecked = false
        }
        navigation_bottom.menu[position].isChecked = true
        oldMenuItem = navigation_bottom.menu[position]
    }

    fun switchFragment(type: Int, mergeList: MutableList<Voice>? = null) {
        when (type) {
            TAG_VOICE_FRAGMENT, TAG_MY_FRAGMENT -> {
                if (view_pager.currentItem != type) {
                    view_pager.currentItem = type
                }
            }
            TAG_MERGE_FRAGMENT -> {
                if (view_pager.currentItem != type) {
                    if (!mergeList.isNullOrEmpty()) {
                        ((view_pager.adapter as FragmentAdapter).fragmentList[type] as MergeFragment).setMergeVoiceList(
                            mergeList!!
                        )
                    }
                    view_pager.currentItem = type
                }
            }
        }
    }

    companion object {
        const val TAG_VOICE_FRAGMENT = 0
        const val TAG_MERGE_FRAGMENT = 1
        const val TAG_MY_FRAGMENT = 2
    }

}
