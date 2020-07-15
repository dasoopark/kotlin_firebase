package kr.co.korearental.kotlinfirebase

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
    var tokenText = ""

    var pickImageFromAlbum = 0
    var fbStorage: FirebaseStorage? = null
    var uriPhoto: Uri? = null


    //Autrhentication 인증, 로그인 (이메일 형태로)
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

    //storage에 저장된 이미지 불러오기
    private fun displayImage(){
        val storageRef = mFirebaseStorage!!.getReferenceFromUrl("gs://testfire-6f469.appspot.com/test.jpg")
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // Use the bytes to display the image
            Log.d("test", "getBytes success")
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bmp)
        }.addOnFailureListener { Log.d("test", "getBytes Failed") }
    }


    //displayconfig ~ onfechButton : remoteconfig를 사용하기 위해 사용
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

    //데이터베이스
    fun onWriteData(v: View?) {
        val database = FirebaseDatabase.getInstance()
        var myRef = database.getReference("message")
        myRef.setValue("sample_dat")
        Toast.makeText(this, "데이터베이스에 추가되었습니다", Toast.LENGTH_SHORT).show()
        FCMToken()
        //child를 이용해 자식 생성하기
        val childRef = database.getReference("users")
        val userid = "park"
        val username = "dasoo"
        childRef.child("name").child(userid).setValue(username)
        childRef.child("token").child(username).setValue(tokenText)

        //push(), 고유한 아이디를 갖는 자식 노드 생성하기, Map<String, Object> 형태 값 저장 예
        val uniquemyRef = database.getReference("posts")
        val key = uniquemyRef.push().key
        val postValues: HashMap<String, Any> = HashMap()
        postValues["uid"] = "aloverlace"
        postValues["author"] = "Ada Lovelace"
        postValues["title"] = "hello post"
        postValues["body"] = "hello body"
        postValues["starCount"] = 0
        uniquemyRef.child(key!!).setValue(postValues)

        // 데이터 정렬하기
        val TopPostsQuery : Query = database.reference.child("posts").orderByChild("starCount")
        TopPostsQuery.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    // TODO: handle the post
                }
            }
        })

        //하나만 있을 때 읽는 리스너
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                dataid.setText(value).toString()
            }
            override fun onCancelled(error: DatabaseError) {
                    error.toException()
            }
        }) //리스너

    }

    ////////// 클라우드 메시지 서비스
    open fun FCMToken(): String {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w( "FragmentActivity.TAG","getInstanceId failed",task.exception  )
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result.token
                // Log and toast
                val msg = "FCM token:$token"
                tokenText = msg
                tokentext.setText(msg).toString()
                Log.d("FragmentActivity.TAG", msg)
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            })
        return tokenText
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

        database.setOnClickListener{
            onWriteData(database)
        }
        cloude.setOnClickListener{
            FCMToken()
        }


        mFirebaseStorage = FirebaseStorage.getInstance();
    }

}

