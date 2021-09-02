package com.pds.ipcclient.ui.messenger

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pds.ipcclient.utils.Constants.CONNECTION_COUNT
import com.pds.ipcclient.utils.Constants.DATA
import com.pds.ipcclient.utils.Constants.PACKAGE_NAME
import com.pds.ipcclient.utils.Constants.PID
import com.nicomahnic.ipcclient.databinding.FragmentMessengerBinding


class MessengerFragment : Fragment(), ServiceConnection {

    private var _binding: FragmentMessengerBinding? = null
    private val viewBinding get() = _binding!!

    private var state = ""

    // Messenger on the server
    private var serverMessenger: Messenger? = null

    // Messenger on the client
    private var clientMessenger: Messenger? = null

    // Handle messages from the remote service
    private var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
		    // Update UI with remote process info
            val bundle = msg.data
            viewBinding.txtServerPid.text = bundle.getInt(PID).toString()
            viewBinding.txtServerConnectionCount.text = bundle.getInt(CONNECTION_COUNT).toString()

            doUnbindService()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessengerBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewBinding.btnCreate.setOnClickListener(createOnClick)
        viewBinding.btnCancel.setOnClickListener(cancelOnClick)
        viewBinding.btnGetStatus.setOnClickListener(statusOnClick)
    }

    private var createOnClick: View.OnClickListener = View.OnClickListener {
        state = "CREATE"
        doBindService()
    }
    private var cancelOnClick: View.OnClickListener = View.OnClickListener {
        state = "CANCEL"
        doBindService()
    }
    private var statusOnClick: View.OnClickListener = View.OnClickListener {
        state = "STATUS"
        doBindService()
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serverMessenger = Messenger(service)
        // Ready to send messages to remote service
        sendMessageToServer()
    }

    override fun onServiceDisconnected(className: ComponentName) {
        clearUI()
        serverMessenger = null
    }

    private fun clearUI(){
        viewBinding.txtServerPid.text = ""
        viewBinding.txtServerConnectionCount.text = ""
        viewBinding.linearLayoutClientInfo.visibility = View.INVISIBLE
    }

    private fun doBindService() {
        clientMessenger = Messenger(handler)
        Intent("messengerexample").also { intent ->
            intent.`package` = "com.pds.mercadopago_disp_captura_service"
            activity?.applicationContext?.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    private fun doUnbindService() {
        activity?.applicationContext?.unbindService(this)
    }

    private fun sendMessageToServer() {
        val message = Message.obtain(handler)
        val bundle = Bundle()
        bundle.putString(DATA, state)
        bundle.putString(PACKAGE_NAME, context?.packageName)
        bundle.putInt(PID, Process.myPid())
        message.data = bundle
        message.replyTo = clientMessenger // we offer our Messenger object for communication to be two-way
        try {
            serverMessenger?.send(message)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            message.recycle()
        }
    }
}
