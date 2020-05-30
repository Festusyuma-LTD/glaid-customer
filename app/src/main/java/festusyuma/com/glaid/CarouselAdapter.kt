package festusyuma.com.glaid

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import festusyuma.com.glaid.utilities.DataServices.fragmentPageDatas

class CarouselAdapter(fm: FragmentManager?, lifecycle: Lifecycle) :
    FragmentStateAdapter(fm!!, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = CarouselFragment("page1")
            1 -> fragment = CarouselFragment("page2")
            2 -> fragment = CarouselFragment("page3")
        }
        return fragment!!
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int {
        return fragmentPageDatas.count()
    }


}

