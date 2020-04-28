package com.example.mediaplayer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.io.IOException
import java.lang.RuntimeException

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ItemsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerFragment : Fragment() {
    //private var listener: FragmentItemsListener? = null
    private lateinit var textView: TextView
    private lateinit var btnOkay: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun updateEditTest(newText: CharSequence){
        Log.e(TAG ,"recieved soemthing")
        Log.e(TAG , newText.toString())
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)
        textView = v.findViewById(R.id.fragTextIdB)
        btnOkay = v.findViewById(R.id.btnB)

        var bundle: Bundle = this.arguments!!
        var x: String = textView.text.toString()
        var x2 = "no way - player "
        if (bundle.containsKey("keyOther")) {
            x2 = x + bundle?.getString("keyOther").toString()
        }
        textView.text = x2
        Log.e(ItemsFragment.TAG, "In Othersz")
        Log.e(ItemsFragment.TAG, x)
        Log.e(ItemsFragment.TAG, x2)

        val btn:Button = v.findViewById(R.id.btnB) as Button
        btn.setOnClickListener {
            Log.e(TAG, "FRAG OTHER CLICK")
        }
        return v
    }

    companion object {
        const val TAG = "PlayerFragment"
        @JvmStatic
        fun newInstance(param1: String, param2: String) : PlayerFragment {
            val playerFragment = PlayerFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            playerFragment.arguments = args
            return playerFragment
        }
    }

    fun doSomething(v: View) {
        Log.e(TAG, "SOMETHIGN! OTHER")
    }


}
//btn.setOnClickListener {
//    Log.e(TAG, "FRAG OTHER CLICK")
//            var bundle: Bundle = Bundle()
//            bundle.putString("keyOther", " HI ")
//            var frag: ItemsFragment = ItemsFragment()
//            frag.arguments = bundle
//            fragmentManager
//                ?.beginTransaction()
//                ?.replace(R.id.container_a, frag)
//                ?.commit()