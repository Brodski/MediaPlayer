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
    private var listener: FragmentItemsListener? = null
    private lateinit var textView: TextView
    private lateinit var btnOkay: Button

    interface FragmentItemsListener {
        fun onInputItemsSent(input: CharSequence)
    }

    @Throws(IOException::class)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(TAG, "onAttach() Items")
        if (context is FragmentItemsListener) {
            listener = context as FragmentItemsListener
        } else {
            throw RuntimeException(context.toString() + "must implement FragmentItemsListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate() Items")

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun updateEditTest(newText: CharSequence){
        Log.e(TAG ,"recieved soemthing")
        Log.e(TAG , newText.toString())
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        var v: View = inflater.inflate(R.layout.fragment_items, container, false)
        textView = v.findViewById(R.id.fragTextId)
        btnOkay = v.findViewById(R.id.btnA)

        var bundle: Bundle = this.arguments!!
        var x: String = textView.text.toString()
        var x2 =" nope"
        if (bundle.containsKey("keyOther")){
            x2 = x + bundle?.getString("keyOther").toString()
        }
        //textView.text = x2
        Log.e(TAG, "IN ITEMSz")
        Log.e(TAG, x)
        Log.e(TAG, x2)

//        val btn:Button = v.findViewById(R.id.btnA) as Button
//        btn.setOnClickListener {
//            Log.e(TAG, "FRAG A CLICK")
//            var input: CharSequence = "from frag Items" as CharSequence
//            listener?.onInputItemsSent(input)
//        }
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
    }


}
