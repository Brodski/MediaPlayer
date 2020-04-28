package com.example.mediaplayer

import android.content.Context
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
class ItemsFragment : Fragment() {

    private lateinit var textView: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG,"Attached Items")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG,"Created Items")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG,"Detached Items")
    }

    override fun onDestroyView(){
        super.onDestroyView()
        Log.e(TAG,"Destroyed Items")
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        var v: View = inflater.inflate(R.layout.fragment_items, container, false)
        textView = v.findViewById(R.id.fragTextId)

        var bundle: Bundle? = this.arguments
        var x: String = textView.text.toString()
        if (bundle != null) {
            if (bundle.containsKey("keyOther")){
                x += bundle?.getString("keyOther").toString()
            }
        }

        Log.e(TAG, "IN ITEMSz")
        Log.e(TAG, x)

        val btn:Button = v.findViewById(R.id.btnA) as Button
        btn.setOnClickListener { doSomething(it) }
        return v
    }

    companion object {
        const val TAG = "ItemsFragment"
        @JvmStatic
        fun newInstance(param1: String, param2: String) : ItemsFragment {
            val itemsFragment = ItemsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            itemsFragment.arguments = args
            return itemsFragment

        }

        fun doSomething(v: View) {
            Log.e(TAG, "Clicked in ITEMS Fragment")
        }
    }
}
