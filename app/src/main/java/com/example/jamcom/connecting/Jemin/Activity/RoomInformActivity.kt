package com.example.jamcom.connecting.Jemin.Activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.jamcom.connecting.Jemin.Fragment.*
import com.example.jamcom.connecting.Jemin.Item.RoomMemberItem
import com.example.jamcom.connecting.Network.Get.GetLocationMessage
import com.example.jamcom.connecting.Network.Get.GetParticipMemberMessage
import com.example.jamcom.connecting.Network.Get.GetRoomDetailMessage
import com.example.jamcom.connecting.Network.Get.Response.*
import com.example.jamcom.connecting.Network.NetworkService
import com.example.jamcom.connecting.Network.Post.DeleteDate
import com.example.jamcom.connecting.Network.Post.DeletePromise
import com.example.jamcom.connecting.Network.Post.DeleteRoom
import com.example.jamcom.connecting.Network.Post.Response.DeleteDateResponse
import com.example.jamcom.connecting.Network.Post.Response.DeletePromiseResponse
import com.example.jamcom.connecting.Network.Post.Response.DeleteRoomResponse
import com.example.jamcom.connecting.Network.RestApplicationController
import com.example.jamcom.connecting.Network.RestNetworkService
import com.example.jamcom.connecting.Network.ApiClient
import com.example.jamcom.connecting.R
import kotlinx.android.synthetic.main.activity_room_inform.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoomInformActivity : AppCompatActivity() {

    private val FRAGMENT1 = 1
    private val FRAGMENT2 = 2
    private val FRAGMENT3 = 3
    private val FRAGMENT4 = 4
    internal lateinit var locationManager: LocationManager
    private var decideTv: TextView? = null
    private var memberTv: TextView? = null
    private var recomPlaceTv: TextView? = null
    private var myInformTv: TextView? = null
    internal lateinit var myToolbar: Toolbar
    lateinit var locationData : java.util.ArrayList<GetLocationMessage>

    var confirmedName : String = ""
    var confirmedLat : Double = 0.0
    var confirmedLon : Double = 0.0
    var roomStatus : Int = 0
    var count : Int = 0
    var countVaule : String = ""

    var roomMemberCount : Int = 0

    lateinit var restNetworkService : RestNetworkService
    var roomDetailData : ArrayList<GetRoomDetailMessage> = ArrayList()
    lateinit var networkService : NetworkService
    lateinit var requestManager: RequestManager
    lateinit var roomMemberItems: ArrayList<RoomMemberItem>
    lateinit var memberlistData : ArrayList<GetParticipMemberMessage>

    var roomName : String = ""
    var roomStartDate : String = ""
    var roomEndDate : String = ""
    var typeName : String = ""
    var roomCreaterID : Int = 0
    var roomID : Int = 0

    var promiseLatSum : Double = 0.0
    var recomPromiseLat : Double = 0.0
    var promiseLonSum : Double = 0.0
    var recomPromiseLon : Double = 0.0
    var x : String = ""
    var y : String = ""
    var userID : Int = 0

    // 추천 장소(지하철역) 랭킹 1위 좌표
    var recom_first_x : String = ""
    var recom_first_y : String = ""
    var recom_first_name : String = ""

    // 추천 장소(지하철역) 랭킹 2위 좌표
    var recom_second_x : String = ""
    var recom_second_y : String = ""
    var recom_second_name : String = ""

    // 추천 장소(지하철역) 랭킹 3위 좌표
    var recom_third_x : String = ""
    var recom_third_y : String = ""
    var recom_third_name : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_inform)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 추가된 소스, Toolbar를 생성한다.
        val view = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = Color.parseColor("#FFFFFF")
            }
        } else if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            window.statusBarColor = Color.BLACK
        }

        requestManager = Glide.with(this)

        // 위젯에 대한 참조
        decideTv = findViewById(R.id.room_inform_decide_tv) as TextView
        memberTv = findViewById(R.id.room_inform_member_tv) as TextView
        recomPlaceTv = findViewById(R.id.room_inform_recomplace_tv) as TextView
        myInformTv = findViewById(R.id.room_inform_myinform_tv) as TextView

        // 탭 버튼에 대한 리스너 연결

        roomID = intent.getIntExtra("roomID", 0)
        getRoomDetail()
        getLocation()
        getParticipMemberList()
        subwayCategorySearch()
        // 임의로 액티비티 호출 시점에 어느 프레그먼트를 프레임레이아웃에 띄울 것인지를 정함
        //callFragment(FRAGMENT1)

        room_inform_decide_tv.setSelected(true)

        // '약속 정하기' 탭 버튼 클릭 이벤트
        room_inform_decide_tv.setOnClickListener {
            room_inform_decide_tv.isSelected = true
            room_inform_decide_tv.setTextColor(Color.parseColor("#764dd1"))

            room_inform_member_tv.isSelected = false
            room_inform_member_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_recomplace_tv.isSelected = false
            room_inform_recomplace_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_myinform_tv.isSelected = false
            room_inform_myinform_tv.setTextColor(Color.parseColor("#2b2b2b"))

            callFragment(FRAGMENT1)
        }

        // '멤버' 탭 버튼 클릭 이벤트
        room_inform_member_tv.setOnClickListener {

            room_inform_member_tv.isSelected = true
            room_inform_member_tv.setTextColor(Color.parseColor("#764dd1"))

            room_inform_decide_tv.isSelected = false
            room_inform_decide_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_recomplace_tv.isSelected = false
            room_inform_recomplace_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_myinform_tv.isSelected = false
            room_inform_myinform_tv.setTextColor(Color.parseColor("#2b2b2b"))
            callFragment(FRAGMENT2)
        }

        // '추천장소' 탭 버튼 클릭 이벤트
        room_inform_recomplace_tv.setOnClickListener {
            room_inform_recomplace_tv.isSelected = true
            room_inform_recomplace_tv.setTextColor(Color.parseColor("#764dd1"))

            room_inform_decide_tv.isSelected = false
            room_inform_decide_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_member_tv.isSelected = false
            room_inform_member_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_myinform_tv.isSelected = false
            room_inform_myinform_tv.setTextColor(Color.parseColor("#2b2b2b"))
            callFragment(FRAGMENT3)
        }

        // '내 정보' 탭 버튼 클릭 이벤트
        room_inform_myinform_tv.setOnClickListener {
            room_inform_myinform_tv.isSelected = true
            room_inform_myinform_tv.setTextColor(Color.parseColor("#764dd1"))

            room_inform_decide_tv.isSelected = false
            room_inform_decide_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_recomplace_tv.isSelected = false
            room_inform_recomplace_tv.setTextColor(Color.parseColor("#2b2b2b"))
            room_inform_member_tv.isSelected = false
            room_inform_member_tv.setTextColor(Color.parseColor("#2b2b2b"))
            // '버튼4' 클릭 시 '프래그먼트2' 호출
            callFragment(FRAGMENT4)
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivity(intent)
            finish()
        }

        //마시멜로 이상이면 권한 요청하기
        if (Build.VERSION.SDK_INT >= 23) {
            //권한이 없는 경우
            if (ContextCompat.checkSelfPermission(this@RoomInformActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this@RoomInformActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@RoomInformActivity, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                requestMyLocation()
            }//권한이 있는 경우
        } else {
            requestMyLocation()
        }//마시멜로 아래

    }

    //나의 위치 요청
    fun requestMyLocation() {
        if (ContextCompat.checkSelfPermission(this@RoomInformActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this@RoomInformActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    private fun callFragment(frament_no: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        when (frament_no) {
            1 -> {
                // '약속정하기 탭' 호출
                val roomDecideTab = RoomDecideTab()
                val bundle = Bundle()
                bundle.putInt("roomID", roomID)
                bundle.putInt("roomMemberCount",roomMemberCount)
                Log.v("TAG","상세정보에서 보내는 방 번호 = "+ roomID)
                Log.v("TAG","상세정보에서 보내는 멤버 크기 = "+ roomMemberCount)
                roomDecideTab.setArguments(bundle)

                transaction.replace(R.id.room_inform_frame_layout, roomDecideTab)
                transaction.commit()
            }

            2 -> {
                // '멤버 탭' 호출
                val roomMemberTab = RoomMemberTab()
                val bundle = Bundle()
                bundle.putInt("roomID", roomID)
                bundle.putString("roomName", roomName)
                bundle.putInt("roomStatus", roomStatus)
                Log.v("TAG","상세정보에서 보내는 방 번호 = "+ roomID)
                Log.v("TAG","상세정보에서 보내는 방 이름 = "+ roomName)
                roomMemberTab.setArguments(bundle)

                transaction.replace(R.id.room_inform_frame_layout, roomMemberTab)
                transaction.commit()
            }


            3 -> {
                // '추천장소 탭' 호출
                val roomRecomPlaceTab = RoomRecomPlaceTab()
                val bundle = Bundle()
                bundle.putString("x", x)
                bundle.putString("y", y)
                bundle.putString("confirmedName", confirmedName)
                bundle.putDouble("confirmedLat", confirmedLat)
                bundle.putDouble("confirmedLon", confirmedLon)
                bundle.putString("typeName", typeName)
                bundle.putInt("roomStatus", roomStatus)

                Log.v("TAG","상세정보에서 보내는 x = "+ x)
                Log.v("TAG","상세정보에서 보내는 y = "+ y)
                Log.v("TAG","상세정보에서 보내는 확정 Lat = "+ confirmedLat)
                Log.v("TAG","상세정보에서 보내는 확정 Lon = "+ confirmedLon)
                Log.v("TAG","상세정보에서 보내는 타입 = "+ typeName)
                roomRecomPlaceTab.setArguments(bundle)

                transaction.replace(R.id.room_inform_frame_layout, roomRecomPlaceTab)
                transaction.commit()
            }

            4 -> {
                // '내 정보 탭' 호출
                val roomMyInformTab = RoomMyInformTab()
                val bundle = Bundle()
                bundle.putInt("roomID", roomID)
                bundle.putInt("roomStatus", roomStatus)
                bundle.putString("roomStartDate", roomStartDate)
                bundle.putString("roomEndDate", roomEndDate)
                Log.v("TAG","상세정보에서 보내는 방번호 = "+ roomID)
                Log.v("TAG","상세정보에서 보내는 시작 날짜 = "+ roomStartDate)
                Log.v("TAG","상세정보에서 보내는 끝 날짜 = "+ roomEndDate)
                roomMyInformTab.setArguments(bundle)

                transaction.replace(R.id.room_inform_frame_layout, roomMyInformTab)
                transaction.commit()
            }
        }
        room_inform_out_btn.setOnClickListener {
            AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("해당 약속 방을 나가시겠습니까?")
                    .setPositiveButton("예") { dialog, which ->
                        deletePromise()
                        finish()
                    }
                    .setNegativeButton("아니요", null)
                    .show()
        }
        room_inform_delete_btn.setOnClickListener {
            AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("해당 약속 방을 삭제하시겠습니까?")
                    .setPositiveButton("예") { dialog, which ->
                        deleteRoom()
                        finish()

                    }
                    .setNegativeButton("아니요", null)
                    .show()
        }

    }

    // 해당 방의 상세 정보 데이터 가져오기
    fun getRoomDetail(){
        try {
            networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
            var getRoomDetailRespnose = networkService.getRoomDetail(roomID) // 네트워크 서비스의 getContent 함수를 받아옴
            val pref = applicationContext.getSharedPreferences("auto", Activity.MODE_PRIVATE)
            var userID : Int = 0
            userID = pref.getInt("userID",0)
            getRoomDetailRespnose.enqueue(object : Callback<GetRoomDetailRespnose> {
                override fun onResponse(call: Call<GetRoomDetailRespnose>?, response: Response<GetRoomDetailRespnose>?) {
                    if(response!!.isSuccessful)
                    {
                        roomDetailData = response.body()!!.result

                        roomName = roomDetailData[0].roomName
                        roomStartDate = roomDetailData[0].roomStartDate
                        roomEndDate = roomDetailData[0].roomEndDate
                        typeName = roomDetailData[0].typeName
                        roomCreaterID = roomDetailData[0].roomCreaterID
                        requestManager.load(roomDetailData[0].img_url).into(room_inform_bg_img)
                        roomStatus = roomDetailData[0].roomStatus
                        confirmedName = roomDetailData[0].confirmedName!!
                        confirmedLat = roomDetailData[0].confirmedLat!!
                        confirmedLon = roomDetailData[0].confirmedLon!!

                        room_inform_title_tv.setText(roomName)
                        room_inform_type_tv.setText(typeName)

                        if(roomCreaterID == userID){
                            room_inform_delete_btn.visibility = View.VISIBLE
                            room_inform_out_btn.visibility = View.INVISIBLE
                        }
                        else{
                            if(roomStatus == 1){
                                room_inform_out_btn.visibility = View.INVISIBLE
                                room_inform_delete_btn.visibility = View.INVISIBLE
                            }
                            else{
                                room_inform_out_btn.visibility = View.VISIBLE
                                room_inform_delete_btn.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<GetRoomDetailRespnose>?, t: Throwable?) {
                }
            })
        } catch (e: Exception) {
        }

    }

    // 해당 방의 참여 멤버 리스트 가져오기
    private fun getParticipMemberList() {
        roomMemberItems = ArrayList()
        try {
            networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
            var getParticipMemberResponse = networkService.getParticipMemberList(roomID) // 네트워크 서비스의 getContent 함수를 받아옴

            getParticipMemberResponse.enqueue(object : Callback<GetParticipMemberResponse> {
                override fun onResponse(call: Call<GetParticipMemberResponse>?, response: Response<GetParticipMemberResponse>?) {
                    if(response!!.isSuccessful)
                    {
                        memberlistData = response.body()!!.result

                        for(i in 0..memberlistData.size-1) {
                            if(memberlistData[i].userImageUrl == ""){
                                memberlistData[i].userImageUrl = "http://18.188.54.59:8080/resources/upload/bg_sample.png"
                            }

                            if(i == 0){
                                requestManager.load(memberlistData[0].userImageUrl).into(room_inform_profile1_img)
                                count = 0
                            }
                            else if(i == 1){
                                requestManager.load(memberlistData[1].userImageUrl).into(room_inform_profile2_img)
                                count = 0
                            }
                            else if(i == 2){
                                requestManager.load(memberlistData[2].userImageUrl).into(room_inform_profile3_img)
                                count = 0
                            }

                            else{
                                count += 1

                            }

                        }
                        countVaule = "+" + (count).toString()
                        room_inform_plus_member_number_tv.setText(countVaule)
                        roomMemberCount = memberlistData.size
                        callFragment(FRAGMENT1)
                    }
                }

                override fun onFailure(call: Call<GetParticipMemberResponse>?, t: Throwable?) {
                }
            })
        } catch (e: Exception) {
        }

    }

    // 해당 방의 모든 출발 위치 데이터 가져오기
    private fun getLocation() {
        try {
            networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
            var getLocationResponse = networkService.getLocation(roomID) // 네트워크 서비스의 getContent 함수를 받아옴

            getLocationResponse.enqueue(object : Callback<GetLocationResponse> {
                override fun onResponse(call: Call<GetLocationResponse>?, response: Response<GetLocationResponse>?) {
                    if(response!!.isSuccessful)
                    {
                        locationData = response.body()!!.result

                        for(i in 0..locationData.size-1) {
                            promiseLatSum += locationData[i].promiseLat!!
                            promiseLonSum += locationData[i].promiseLon!!
                            count += 1
                        }
                        recomPromiseLat = promiseLatSum/count
                        recomPromiseLon = promiseLonSum/count

                        x = recomPromiseLon.toString()
                        y = recomPromiseLat.toString()

                    }
                }
                override fun onFailure(call: Call<GetLocationResponse>?, t: Throwable?) {
                }
            })
        } catch (e: Exception) {
        }
    }

    // 근처 지하철 역 데이터 API
    fun subwayCategorySearch()
    {
        restNetworkService = RestApplicationController.getRetrofit().create(RestNetworkService::class.java)

        var subway_group_code : String = ""
        var radius : Int = 0

        subway_group_code = "SW8"
        radius = 10000

        var getSearchCategory = restNetworkService.getCategorySearch("KakaoAK 3897b8b78021e2b29c516d6276ce0b08", subway_group_code, x, y, radius)
        getSearchCategory.enqueue(object : Callback<GetCategoryResponse> {

            override fun onResponse(call: Call<GetCategoryResponse>?, response: Response<GetCategoryResponse>?) {
                if(response!!.isSuccessful)
                {
                    if(response!!.body()!!.documents.size == 0)
                    {
                    }
                    else
                    {
                        val splitResult1 = response!!.body()!!.documents[0]!!.place_name!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        var count : Int = 0

                        for (sp in splitResult1) {
                            splitResult1[count] = sp
                            count += 1
                        }

                        val splitResult2 = response!!.body()!!.documents[1]!!.place_name!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        var count2 : Int = 0

                        for (sp in splitResult2) {
                            splitResult2[count2] = sp
                            count2 += 1
                        }

                        val splitResult3 = response!!.body()!!.documents[2]!!.place_name!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        var count3 : Int = 0

                        for (sp in splitResult3) {
                            splitResult3[count3] = sp
                            count3 += 1
                        }

                        recom_second_name = splitResult2[0]

                        // 첫번째 지하철역 정보
                        recom_first_x = response!!.body()!!.documents[0]!!.x!!
                        recom_first_y = response!!.body()!!.documents[0]!!.y!!
                        recom_first_name = splitResult1[0]

                        // 두번째 지하철역 정보
                        recom_second_x = response!!.body()!!.documents[1]!!.x!!
                        recom_second_y = response!!.body()!!.documents[1]!!.y!!
                        recom_second_name = splitResult2[0]

                        // 세번째 지하철역 정보
                        recom_third_x = response!!.body()!!.documents[2]!!.x!!
                        recom_third_y = response!!.body()!!.documents[2]!!.y!!
                        recom_third_name = splitResult3[0]
                    }

                }
                else
                {
                }
            }

            override fun onFailure(call: Call<GetCategoryResponse>?, t: Throwable?) {
            }
        })
    }

    // 해당 약속 나가기(방장 아닌 인원만 가능)
    fun deletePromise()
    {
        val pref = applicationContext.getSharedPreferences("auto", Activity.MODE_PRIVATE)
        var userID : Int = 0
        userID = pref.getInt("userID",0)
        networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
        var deletePromise = DeletePromise(roomID, userID)
        var deletePromiseResponse = networkService.deletePromise(deletePromise)
        deletePromiseResponse.enqueue(object : retrofit2.Callback<DeletePromiseResponse>{

            override fun onResponse(call: Call<DeletePromiseResponse>, response: Response<DeletePromiseResponse>) {
                if(response.isSuccessful){
                    deleteDate()
                }
            }

            override fun onFailure(call: Call<DeletePromiseResponse>, t: Throwable?) {
            }

        })

    }

    // 해당 방 삭제(방장만 가능)
    fun deleteRoom()
    {
        val pref = applicationContext.getSharedPreferences("auto", Activity.MODE_PRIVATE)
        var userID : Int = 0
        userID = pref.getInt("userID",0)
        networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
        var deleteRoom = DeleteRoom(roomCreaterID, roomID)
        var deleteRoomResponse = networkService.deleteRoom(deleteRoom)
        deleteRoomResponse.enqueue(object : retrofit2.Callback<DeleteRoomResponse>{

            override fun onResponse(call: Call<DeleteRoomResponse>, response: Response<DeleteRoomResponse>) {
                if(response.isSuccessful){
                    var intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            override fun onFailure(call: Call<DeleteRoomResponse>, t: Throwable?) {
            }
        })

    }

    // 자신의 선호 날짜 지우기
    fun deleteDate()
    {
        val pref = applicationContext!!.getSharedPreferences("auto", Activity.MODE_PRIVATE)
        var userID : Int = 0
        userID = pref.getInt("userID",0)

        networkService = ApiClient.getRetrofit().create(NetworkService::class.java)
        var deleteDate = DeleteDate(roomID, userID)
        var deleteDateResponse = networkService.deleteDate(deleteDate)
        deleteDateResponse.enqueue(object : retrofit2.Callback<DeleteDateResponse>{

            override fun onResponse(call: Call<DeleteDateResponse>, response: Response<DeleteDateResponse>) {
                if(response.isSuccessful){
                    var intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            override fun onFailure(call: Call<DeleteDateResponse>, t: Throwable?) {
            }

        })

    }

}