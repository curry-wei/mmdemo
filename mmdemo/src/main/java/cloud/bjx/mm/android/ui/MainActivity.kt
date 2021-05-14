package cloud.bjx.mm.android.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cloud.bjx.mm.android.ACTION_LOGOUT
import cloud.bjx.mm.android.R
import cloud.bjx.mm.android.databinding.ActivityMainBinding
import cloud.bjx.mm.android.utils.LogUtil
import cloud.bjx.mm.android.utils.PermissionUtil
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mLogoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LogUtil.d(" ===== receive logout broadcast")
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            this@MainActivity.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filter = IntentFilter(ACTION_LOGOUT)
        LocalBroadcastManager.getInstance(this).registerReceiver(mLogoutReceiver, filter)

        binding.btnJoinChannel.setOnClickListener { actionJoinChannel() }
        binding.btnJoinVideoChannel.setOnClickListener { actionJoinVideoChannel() }
        binding.btnTestFlex.setOnClickListener { actionFlexTest() }
        requestPermissions()

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLogoutReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_user) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.CAMERA
        )
        PermissionUtil.with(this).permissions(permissions).request()
    }

    private fun actionJoinChannel() {
        startActivity(Intent(this, LiveConfigActivity::class.java))
    }

    private fun actionJoinVideoChannel() {
        startActivity(Intent(this, VideoConfigActivity::class.java))
    }

    private fun actionFlexTest() {
        startActivity(Intent(this, FlexTestActivity::class.java))
    }


}