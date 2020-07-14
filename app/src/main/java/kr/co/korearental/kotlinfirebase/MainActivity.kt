package kr.co.korearental.kotlinfirebase

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*


class   MainActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null      //firebase auth
    private var mFirebaseStorage: FirebaseStorage? = null
    private var viewProfile: View? = null
    private var mFirebaseRemoteConfig = Firebase.remoteConfig

    var pickImageFromAlbum = 0
    var fbStorage: FirebaseStorage? = null
    var uriPhoto: Uri? = null

    private fun loginEmail() {
        if (email.text.toString() == "" || password.text.toString() == "") {
            Toast.makeText(this, "signInWithEmail failed.", Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth!!.signInWithEmailAndPassword(
                    email.text.toString(),
                    password.text.toString()
            ).addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "로그인 되었습니당.", Toast.LENGTH_SHORT).show()
                    //val user = firebaseAuth?.currentUser
                    startActivityForResult(intent, 0)
                } else {
                    Toast.makeText(this, "로그인 실패입니당.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayImage(){
        val storageRef = mFirebaseStorage!!.getReferenceFromUrl("gs://testfire-6f469.appspot.com/test.jpg")
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // Use the bytes to display the image
            Log.d("test", "getBytes success")
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bmp)
        }.addOnFailureListener { Log.d("test", "getBytes Failed") }
    }


    fun displayConfig() {
        val cheat_enabled = mFirebaseRemoteConfig.getBoolean("cheat_enabled")
        val price = mFirebaseRemoteConfig.getString("price")
        Toast.makeText(this, "$price", Toast.LENGTH_SHORT).show()
    }

    fun remote_data(){
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(60) // For development only not for production!, default is 12 hours
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun onFetchButton(v:View?){
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this,
                OnCompleteListener<Boolean> { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        //Log.d(TAG, "Config params updated: $updated")
                    } else {
                        // Log.d(TAG, "Fetch failed")
                    }
                    displayConfig() // 가져온 설정 읽기(다음 슬라이드)
                })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("onCreate","되나요")
        firebaseAuth = FirebaseAuth.getInstance()
        button.setOnClickListener {
            loginEmail()
        }
        btn_upload.setOnClickListener{
            displayImage()
        }

        remote_button.setOnClickListener{
            remote_data()
            onFetchButton(btn_upload)
        }

        mFirebaseStorage = FirebaseStorage.getInstance();
    }
}

