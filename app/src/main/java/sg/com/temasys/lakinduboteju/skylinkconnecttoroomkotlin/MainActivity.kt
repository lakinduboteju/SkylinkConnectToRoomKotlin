package sg.com.temasys.lakinduboteju.skylinkconnecttoroomkotlin

import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sg.com.temasys.skylink.sdk.listener.LifeCycleListener
import sg.com.temasys.skylink.sdk.listener.MessagesListener
import sg.com.temasys.skylink.sdk.listener.RemotePeerListener
import sg.com.temasys.skylink.sdk.rtc.*
import java.util.*

class MainActivity : AppCompatActivity(), LifeCycleListener, RemotePeerListener, MessagesListener {

    private val TAG : String = "slconntoroomlog"

    private lateinit var mSkylinkConnection : SkylinkConnection

    private lateinit var mTV : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTV = findViewById(R.id.tv)

        // Create an Skylink connection instance
        mSkylinkConnection = SkylinkConnection.getInstance()

        // Prepare Skylink Config for messaging
        val skylinkConfig = SkylinkConfig()
        skylinkConfig.setAudioVideoSendConfig(SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO)
        skylinkConfig.setAudioVideoReceiveConfig(SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO)
        skylinkConfig.skylinkRoomSize = SkylinkConfig.SkylinkRoomSize.EXTRA_SMALL
        skylinkConfig.setMaxRemotePeersConnected(3, SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO)
        skylinkConfig.setP2PMessaging(true)
        skylinkConfig.setTimeout(SkylinkConfig.SkylinkAction.GET_MESSAGE_STORED, 5000)

        // Init Skylink connection
        mSkylinkConnection.init(skylinkConfig, applicationContext, object : SkylinkCallback {
            override fun onError(skylinkError: SkylinkError, hashMap: HashMap<String, Any>) {
                Log.e(TAG, "Failed to init Skylink connection. " +
                        skylinkError.description + " " + hashMap[SkylinkEvent.CONTEXT_DESCRIPTION])
            }
        })

        // Set Skylink connection listeners
        mSkylinkConnection.lifeCycleListener = this
        mSkylinkConnection.remotePeerListener = this
        mSkylinkConnection.messagesListener = this
    }

    override fun onResume() {
        super.onResume()

        mTV.text = "Connecting to room..."

        // Connect to Skylink room
        mSkylinkConnection.connectToRoom(getString(R.string.skylink_key_id),
            getString(R.string.skylink_key_secret),
            getString(R.string.skylink_room_name),
            Settings.System.getString(contentResolver, "device_name"),  // Peer username
            object : SkylinkCallback {
                override fun onError(skylinkError: SkylinkError, hashMap: HashMap<String, Any>) {
                    Log.e(TAG, "Failed to connect to Skylink room. " +
                            skylinkError.description + " " + hashMap[SkylinkEvent.CONTEXT_DESCRIPTION])
                }
            })

        // Create a coroutine to move the execution off the main thread to an IO thread
        CoroutineScope(Dispatchers.IO).launch {
            // Wait until connected to Skylink room
            while (mSkylinkConnection.skylinkState != SkylinkConnection.SkylinkState.CONNECTED) {
                delay(500)
            }
            val skylinkState = mSkylinkConnection.skylinkState.toString()

            // New coroutine to update UI from the main thread
            CoroutineScope(Dispatchers.Main).launch {
                mTV.text = skylinkState
            }
        }
    }

    override fun onPause() {
        // Disconnect from Skylink room
        mSkylinkConnection.disconnectFromRoom(object : SkylinkCallback {
            override fun onError(skylinkError: SkylinkError, hashMap: HashMap<String, Any>) {
                Log.e(TAG, "Failed to disconnect from Skylink room. " +
                        skylinkError.description + " " + hashMap[SkylinkEvent.CONTEXT_DESCRIPTION])
            }
        })

        mTV.text = "Disconnected from room"

        super.onPause()
    }

    override fun onDestroy() {
        // Clear Skylink connection
        mSkylinkConnection.clearInstance()

        super.onDestroy()
    }

    // LifeCycleListener callbacks

    override fun onConnectToRoomSucessful() {
        Log.d(TAG, "onConnectToRoomSucessful")
    }

    override fun onConnectToRoomFailed(p0: String?) {
        Log.e(TAG, "onConnectToRoomFailed $p0")
    }

    override fun onDisconnectFromRoom(p0: SkylinkEvent?, p1: String?) {
        Log.d(TAG, "onDisconnectFromRoom " + p1 + " " + p0?.description)
    }

    override fun onChangeRoomLockStatus(p0: Boolean, p1: String?) {
        Log.d(TAG, "onChangeRoomLockStatus $p1 $p0")
    }

    override fun onReceiveInfo(p0: SkylinkInfo?, p1: HashMap<String, Any>?) {
        Log.d(TAG, "onReceiveInfo " + p0?.description + " " + p1?.get(SkylinkEvent.CONTEXT_DESCRIPTION))
    }

    override fun onReceiveWarning(p0: SkylinkError?, p1: HashMap<String, Any>?) {
        Log.d(TAG, "onReceiveWarning " + p0?.description + " " + p1?.get(SkylinkEvent.CONTEXT_DESCRIPTION))
    }

    override fun onReceiveError(p0: SkylinkError?, p1: HashMap<String, Any>?) {
        Log.d(TAG, "onReceiveError " + p0?.description + " " + p1?.get(SkylinkEvent.CONTEXT_DESCRIPTION))
    }

    // RemotePeerListener callbacks

    override fun onReceiveRemotePeerJoinRoom(p0: String?, p1: UserInfo?) {
        Log.d(TAG, "onReceiveRemotePeerJoinRoom " + p0 + " " + p1?.userData.toString())
    }

    override fun onConnectWithRemotePeer(p0: String?, p1: UserInfo?, p2: Boolean) {
    }

    override fun onRefreshRemotePeerConnection(p0: String?, p1: UserInfo?, p2: Boolean, p3: Boolean) {
    }

    override fun onReceiveRemotePeerUserData(p0: Any?, p1: String?) {
    }

    override fun onOpenRemotePeerDataConnection(p0: String?) {
    }

    override fun onDisconnectWithRemotePeer(p0: String?, p1: UserInfo?, p2: Boolean) {
    }

    override fun onReceiveRemotePeerLeaveRoom(p0: String?, p1: SkylinkInfo?, p2: UserInfo?) {
    }

    override fun onErrorForRemotePeerConnection(p0: SkylinkError?, p1: HashMap<String, Any>?) {
    }

    // MessagesListener callbacks

    override fun onReceiveServerMessage(p0: Any?, p1: Boolean, p2: Long?, p3: String?) {
        Log.d(TAG, "onReceiveServerMessage " + p0.toString() + " " + p3)
    }

    override fun onReceiveP2PMessage(p0: Any?, p1: Boolean, p2: Long?, p3: String?) {
    }
}