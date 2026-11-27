package com.example.engine

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class MonoDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(DevicePolicyManager::class.java) ?: return
        val componentName = getComponentName(context)
        runCatching {
            dpm.setProfileName(componentName, "Mono Dual-Space")
            dpm.setProfileEnabled(componentName)
        }
        val launchIntent = Intent(context, Class.forName("com.example.MainActivity")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        runCatching { context.startActivity(launchIntent) }
    }

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, MonoDeviceAdminReceiver::class.java)
        }
    }
}
