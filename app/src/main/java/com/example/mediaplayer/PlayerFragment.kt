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

    private lateinit var textView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG,"Attached Player")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG,"Created Player")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG,"Detached Player")
    }

    override fun onDestroyView(){
        super.onDestroyView()
        Log.e(TAG,"Destroyed Player")

    }

    fun updateEditTest(newText: CharSequence){
        Log.e(TAG ,"recieved soemthing")
        Log.e(TAG , newText.toString())
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_player, container, false)
        textView = v.findViewById(R.id.fragTextIdB)

        var bundle: Bundle = this.arguments!!
        var x: String = textView.text.toString()
        if (bundle.containsKey("keyOther")) {
            x += bundle?.getString("keyOther").toString()
        }
        textView.text = x
        Log.e(ItemsFragment.TAG, "In Othersz")
        Log.e(ItemsFragment.TAG, x)

        val btn:Button = v.findViewById(R.id.btnB) as Button
        btn.setOnClickListener { v -> doSomething(v)  }
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
        Log.e(TAG, "Clicked in Player Fragment")
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