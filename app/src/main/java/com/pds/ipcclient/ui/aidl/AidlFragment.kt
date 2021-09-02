package com.pds.ipcclient.ui.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.provider.Settings
import android.util.Log
import com.nicomahnic.ipcclient.R
import com.nicomahnic.ipcclient.databinding.FragmentAidlBinding
import com.pds.mercadopago_disp_captura_service.IIPCExample


class AidlFragment : Fragment(), ServiceConnection {

    private lateinit var binding: FragmentAidlBinding
    var iRemoteService: IIPCExample? = null


    private lateinit var androidId : String
    private var state = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAidlBinding.inflate(inflater, container, false)
        val view = binding.root

        androidId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        
        binding.btnCancel.setOnClickListener(cancelOnClick)
        binding.btnCreate.setOnClickListener(createOnClick)
        binding.btnGetStatus.setOnClickListener(statusOnClick)

        return view
    }

    private var createOnClick: View.OnClickListener = View.OnClickListener {
        state = "CREATE"
        connectToRemoteService()
    }
    private var cancelOnClick: View.OnClickListener = View.OnClickListener {
        state = "CANCEL"
        connectToRemoteService()
    }
    private var statusOnClick: View.OnClickListener = View.OnClickListener {
        state = "STATUS"
        connectToRemoteService()
    }

    private fun connectToRemoteService() {
        val intent = Intent("aidlexample")
        val pack = IIPCExample::class.java.`package`
        pack?.let {
            intent.setPackage(pack.name)
            activity?.applicationContext?.bindService(
                intent, this, Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        // Gets an instance of the AIDL interface named IIPCExample,
        // which we can use to call on the service
        iRemoteService = IIPCExample.Stub.asInterface(service)
        binding.txtServerPid.text = iRemoteService?.pid.toString()
        binding.txtServerConnectionCount.text = iRemoteService?.connectionCount.toString()
        val response = when(state){
            "CREATE" -> {
                iRemoteService?.createSaleIntent(123.4, "#1", androidId)

            }
            "CANCEL" -> {
                iRemoteService?.cancelSaleIntent("#1_1")

            }
            "STATUS" -> {
                iRemoteService?.getPaymentStatus("#1_1")
            }
            else -> { null }
        }
        Log.d("NM", "$state ResponseCode: $response")
        activity?.applicationContext?.unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Toast.makeText(context, "IPC server has disconnected unexpectedly", Toast.LENGTH_LONG).show()
        iRemoteService = null
    }
}
