package si.uni_lj.fri.pbd.miniapp1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // to change title of activity
        (activity as AppCompatActivity?)!!.supportActionBar?.title = "Home"
    }

}