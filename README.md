# 2018 컴퓨터공학과 졸업작품 - 연결고리

## Summary

![ic_launcher](https://user-images.githubusercontent.com/8520965/46294159-1cb08500-c5d0-11e8-971a-903f2725ad48.png)

직업이나 사는 지역이 모두 다른 사람들이 약속을 정하려고 하면 의견조율이 쉽지 않다. 
의견 충돌이 일어나기도 하고 정하는 데 시간이 오래 걸리면 약속 자체가 흐지부지 되는 경우도 발생한다.


연결고리 애플리케이션은 이러한 고충들을 해결하고자 사용자들의 선호 날짜, 위치를 통해 합리적으로 약속 날짜와 장소를 추천해주며 추천 장소 주변에 자신이 원하는 타입의 가게 리스트
정보도 보여준다.

<br/><br/>
## Dependencies

```
'com.android.support:recyclerview-v7:27.0.2'          // 효과적인 리스트 표현을 위해 recyclerview 이용
'com.squareup.retrofit2:retrofit:2.3.0'               // 서버와의 통신을 위해 retrofit 이용
'com.github.bumptech.glide:glide:3.7.0'               // 이미지url을 통해 효과적으로 이미지를 불러오기 위해 사용
'com.prolificinteractive:material-calendarview:1.4.3' // 달력에서 다중 선택 및 여러 이벤트를 구현하기 위해 사용
'com.android.support:cardview-v7:27.0.2'              // 아이템뷰의 둥근 효과, 그림자 효과를 적용하기 위해 사용
'de.hdodenhof:circleimageview:2.2.0'                  // 이미지를 뷰에 띄울 때 원모양으로 보이도록 구현하기 위해 사용
'com.google.firebase:firebase-core:16.0.0'            // 푸시 알람 기능을 구현하기 위해 사용
'com.google.firebase:firebase-messaging:17.1.0'       // 푸시 알람 기능을 구현하기 위해 사용
'com.kakao.sdk:usermgmt:1.9.0'                        // 카카오 로그인 기능을 구현하기 위해 사용
'com.kakao.sdk:kakaolink:1.9.0'                       // 카카오톡 초대 기능을 구현하기 위해 사용
files('libs/libDaumMapAndroid.jar')                   // 다음 지도를 구현하기 위해 사용
```
<br/><br/>
## API
```
다음카카오
1. Kakao Login API
2. Kakao REST API
3. Kakao Link API
4. Daum Map API
```
## Overall APP VIEW
![1](https://user-images.githubusercontent.com/8520965/46295730-c0e7fb00-c5d3-11e8-9505-ac810f00a7d6.PNG)
![2](https://user-images.githubusercontent.com/8520965/46295749-ca716300-c5d3-11e8-918c-89bec2cf9aeb.PNG)
![3](https://user-images.githubusercontent.com/8520965/46295765-d1987100-c5d3-11e8-92cc-141946843e80.PNG)
![4](https://user-images.githubusercontent.com/8520965/46295775-d826e880-c5d3-11e8-8c88-317af4af29b5.PNG)
![5](https://user-images.githubusercontent.com/8520965/46295787-de1cc980-c5d3-11e8-8d0a-553fab2cec2c.PNG)

