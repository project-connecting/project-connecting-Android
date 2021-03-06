package com.example.jamcom.connecting.Jemin.Fragment

/**
 * Created by JAMCOM on 2018-03-27.
 */
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.jamcom.connecting.Jemin.Activity.SettingActivity
import com.example.jamcom.connecting.Jemin.Adapter.MyDibsAdapter
import com.example.jamcom.connecting.Jemin.Item.MyDibsListItem
import com.example.jamcom.connecting.Network.Get.GetFavoriteListMessage
import com.example.jamcom.connecting.Network.Get.Response.GetConnectingCountResponse
import com.example.jamcom.connecting.Network.Get.Response.GetUserImageUrlResponse
import com.example.jamcom.connecting.Network.NetworkService
import com.example.jamcom.connecting.Network.Post.Response.UpdateProfileImgResponse
import com.example.jamcom.connecting.Network.ApiClient

import com.example.jamcom.connecting.R
import kotlinx.android.synthetic.main.fragment_mypage.*
import kotlinx.android.synthetic.main.fragment_mypage.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * A simple [Fragment] subclass.
 */
class MyPageFragment : Fragment(), View.OnClickListener {

    private val REQ_CODE_SELECT_IMAGE = 100
    lateinit var networkService : NetworkService
    lateinit var mydibsListItem: ArrayList<MyDibsListItem>
    lateinit var myDibsAdapter: MyDibsAdapter
    var connectingSumPoint : Int = 0

    lateinit var data : Uri
    private var image : MultipartBody.Part? = null
    lateinit var requestManager: RequestManager

    lateinit var favoriteListData : ArrayList<GetFavoriteListMessage>

    var userName : String = ""

    override fun onClick(v: View?) {
        when(v)
        {
            // '너와나의 연결고리' 클릭 시
            mypage_connecting_point_tv -> {
                mypage_mydibs_tv.isSelected = false
                mypage_connecting_point_tv.isSelected = true
                mypage_mydibs_tv.setTextColor(Color.parseColor("#636363"))
                mypage_connecting_point_tv.setTextColor(Color.parseColor("#7665bf"))
                mypage_connecting_view.setVisibility(View.VISIBLE)
                mypage_mydibs_view.setVisibility(View.GONE)
                replaceFragment(MypageConnectingTab())
            }
        // '찜한 장소' 클릭 시
            mypage_mydibs_tv -> {
                mypage_mydibs_tv.isSelected = true
                mypage_connecting_point_tv.isSelected = false
                mypage_connecting_point_tv.setTextColor(Color.parseColor("#636363"))
                mypage_mydibs_tv.setTextColor(Color.parseColor("#7665bf"))
                mypage_mydibs_view.setVisibility(View.VISIBLE)
                mypage_connecting_view.setVisibility(View.GONE)
                replaceFragment(MypageMydibsTab())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_mypage, container, false)
        v.mypage_all_scrollview.fullScroll(ScrollView.FOCUS_UP);
        requestManager = Glide.with(this)

        val pref = this.activity!!.getSharedPreferences("auto", Activity.MODE_PRIVATE)
        userName = pref.getString("userName","")
        v.mypage_username_tv.text = userName

        getUserImageUrl()
        v.mypage_mydibs_view.setVisibility(View.GONE)

        // 프로필 사진 클릭 시
        v.mypage_profile_img.setOnClickListener {
            changeImage()
        }

        // 설정 버튼 클릭 시
        v.mypage_setting_btn.setOnClickListener {
            var intent = Intent(activity!!, SettingActivity::class.java)
            startActivity(intent)
        }

        v.mypage_connecting_point_tv.setOnClickListener(this)
        v.mypage_mydibs_tv.setOnClickListener(this)

        v.mypage_connecting_point_tv.isSelected = true
        v.mypage_connecting_point_tv.setTextColor(Color.parseColor("#7665bf"))
        v.mypage_mydibs_tv.setTextColor(Color.parseColor("#636363"))

        addFragment(MypageConnectingTab())
        getConnectingCout(v)

        return v
    }

    // 처음 프래그먼트 추가
    fun addFragment(fragment : Fragment){
        val fm = childFragmentManager
        val transaction = fm.beginTransaction()
        transaction.add(R.id.mypage_content_layout, fragment)
        transaction.commit()
    }

    // 프래그먼트 교체
    fun replaceFragment(fragment: Fragment)
    {
        val fm = childFragmentManager
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.mypage_content_layout, fragment)
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    //if(ApplicationController.getInstance().is)
                    this.data = data!!.data

                    val options = BitmapFactory.Options()

                    var input: InputStream? = null // here, you need to get your context.
                    try {
                        input = context!!.contentResolver.openInputStream(this.data)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }

                    val bitmap = BitmapFactory.decodeStream(input, null, options) // InputStream 으로부터 Bitmap 을 만들어 준다.
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                    val photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray())
                    val img = File(getRealPathFromURI(context!!,this.data).toString()) // 가져온 파일의 이름을 알아내려고 사용합니다

                    image = MultipartBody.Part.createFormData("image", img.name, photoBody)

                    Glide.with(this)
                            .load(data.data)
                            .centerCrop()
                            .into(mypage_profile_img)

                    updateProfileImg()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun changeImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)

    }

    // 유저 이미지 주소 가져오기
    fun getUserImageUrl(){

        try {
            networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
            val pref = activity!!.getSharedPreferences("auto", Activity.MODE_PRIVATE)
            var userID : Int = 0
            userID = pref.getInt("userID",0)

            var getUserImageUrlResponse = networkService.getUserImageUrl(userID) // 네트워크 서비스의 getContent 함수를 받아옴

            getUserImageUrlResponse.enqueue(object : Callback<GetUserImageUrlResponse> {
                override fun onResponse(call: Call<GetUserImageUrlResponse>?, response: Response<GetUserImageUrlResponse>?) {
                    if(response!!.isSuccessful)
                    {
                        response.body()!!.result[0].userImageUrl
                        requestManager.load(response.body()!!.result[0].userImageUrl).into(mypage_profile_img)

                    }
                }

                override fun onFailure(call: Call<GetUserImageUrlResponse>?, t: Throwable?) {
                }
            })
        } catch (e: Exception) {
        }

    }

    // 유저 프로필 사진 변경
    fun updateProfileImg() {
        val pref = activity!!.getSharedPreferences("auto", Activity.MODE_PRIVATE)
        var userIDValue : Int = 0
        userIDValue = pref.getInt("userID",0)
        networkService = ApiClient.getRetrofit().create(com.example.jamcom.connecting.Network.NetworkService::class.java)

        val userID = RequestBody.create(MediaType.parse("text.plain"), userIDValue.toString())
        val updateProfileImgResponse = networkService.updateProfileImg(userID, image)

        updateProfileImgResponse.enqueue(object : retrofit2.Callback<UpdateProfileImgResponse>{

            override fun onResponse(call: Call<UpdateProfileImgResponse>, response: Response<UpdateProfileImgResponse>) {
                if(response.isSuccessful){
                    var message = response!!.body()

                }
                else{

                }
            }

            override fun onFailure(call: Call<UpdateProfileImgResponse>, t: Throwable?) {
            }

        })
    }

    // 자신의 연결고리 리스트 가져오기
    private fun getConnectingCout(v : View) {
        try {
            val pref = activity!!.getSharedPreferences("auto", Activity.MODE_PRIVATE)
            var userID : Int = 0
            userID = pref.getInt("userID",0)
            networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
            var getConnectingCountResponse = networkService.getConnectingCountList(userID) // 네트워크 서비스의 getContent 함수를 받아옴

            getConnectingCountResponse.enqueue(object : Callback<GetConnectingCountResponse> {
                override fun onResponse(call: Call<GetConnectingCountResponse>?, response: Response<GetConnectingCountResponse>?) {
                    if(response!!.isSuccessful)
                    {
                        for(i in 0..response.body()!!.result.size-1) {
                            connectingSumPoint += response.body()!!.result[i].connectingCount
                        }
                        v.mypage_pointsum_tv.text = connectingSumPoint.toString() + "P"
                    }
                }

                override fun onFailure(call: Call<GetConnectingCountResponse>?, t: Throwable?) {
                }
            })
        } catch (e: Exception) {
        }
    }
}