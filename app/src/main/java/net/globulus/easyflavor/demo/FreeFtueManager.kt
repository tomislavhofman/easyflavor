package net.globulus.easyflavor.demo

import android.util.Log
import net.globulus.easyflavor.annotation.Flavored

@Flavored(flavors = [AppFlavors.FREE])
class FreeFtueManager : FtueManager {
    override fun signup(email: String, password: String, callback: Callback?) {
        val tag = this::class.java.simpleName
        Log.e(tag, "Free called $email $password")
        callback?.handle(tag)
    }
}