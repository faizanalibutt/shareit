package com.sharecloud.sharefiles.sharedata.anyshare.musictransfer.filetransfer.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Toast


class RemoteService : Service() {
    private var messenger //receives remote invocations
            : Messenger? = null

    override fun onBind(intent: Intent): IBinder? {
        if (messenger == null) {
            synchronized(RemoteService::class.java) {
                if (messenger == null) {
                    messenger =
                        Messenger(IncomingHandler())
                }
            }
        }
        //Return the proper IBinder instance
        return messenger!!.binder
    }

    class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            println("*****************************************")
            println("Remote Service successfully invoked!!!!!!")
            println("*****************************************")
            val what = msg.what
            Log.e(this.javaClass.name, "Remote Service invoked-($what)")

            //Setup the reply message
            val message = Message.obtain(null, 2, 0, 0)
            try {
                //make the RPC invocation
                val replyTo = msg.replyTo
                replyTo.send(message)
            } catch (rme: RemoteException) {
                //Show an Error Message

                Log.e(this.javaClass.name, "Invocation Failed!!")
            }
        }
    }
}

private class RemoteServiceConnection : ServiceConnection {
    var messenger: Messenger? = null
    var isBound = false

    override
    fun onServiceConnected(component: ComponentName?, binder: IBinder?) {
        messenger = Messenger(binder)
        isBound = true
    }

    override
    fun onServiceDisconnected(component: ComponentName?) {
        messenger = null
        isBound = false
    }
}

private class IncomingHandler : Handler() {
    override fun handleMessage(msg: Message) {
        println("*****************************************")
        println("Return successfully received!!!!!!")
        println("*****************************************")
        val what = msg.what

        Log.e(this.javaClass.name, "Remote Service replied-($what)")
    }
}