//package com.code4rox.compassx.activities
//
////import io.github.inflationx.viewpump.ViewPumpContextWrapper
//import android.Manifest
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.res.Resources
//import android.graphics.Color
//import android.location.Location
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.SystemClock
//import android.text.Spannable
//import android.text.SpannableString
//import android.text.style.RelativeSizeSpan
//import android.util.Log
//import android.view.View
//import android.view.ViewGroup
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.core.os.postDelayed
//import androidx.core.view.GravityCompat
//import androidx.core.view.marginBottom
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.recyclerview.widget.StaggeredGridLayoutManager
//import com.anjlab.android.iab.v3.BillingProcessor
//import com.anjlab.android.iab.v3.TransactionDetails
//import com.code4rox.adsmanager.AdmobUtils
//import com.code4rox.adsmanager.NativeAdsIdType
//import com.code4rox.compassx.R
//import com.code4rox.compassx.Utils.*
//import com.code4rox.compassx.adapter.BaseRecyclerAdapter
//import com.code4rox.compassx.holder.DrawerMainScreenItemHolder
//import com.code4rox.compassx.model.DrawerItemModel
//import com.code4rox.weathermanager.WeatherManager
//import com.code4rox.weathermanager.WeatherUtility
//import com.code4rox.weathermanager.model.WeatherData
//import com.code4rox.weatherx.activities.MainWeatherActivity
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.material.animation.ArgbEvaluatorCompat
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import com.nabinbhandari.android.permissions.PermissionHandler
//import com.nabinbhandari.android.permissions.Permissions
//import io.github.inflationx.viewpump.ViewPumpContextWrapper
//import kotlinx.android.synthetic.main.activity_main_display.*
//import kotlinx.android.synthetic.main.bottom_sheet.*
//import kotlinx.android.synthetic.main.main_layout.*
//import org.greenrobot.eventbus.EventBus
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//import java.text.SimpleDateFormat
//import java.util.*
//import kotlin.math.roundToInt
//
//
//class MainDisplayActivity : AppCompatActivity() {
//    private var DATE_FORMAT = SimpleDateFormat("hh:mm", Locale.ENGLISH)
//
//    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>
//
//    private val startColor = Color.parseColor("#00FFFFFF")
//    private val endColor = Color.parseColor("#FFFFFFFF")
//    private val textColor = Color.parseColor("#FF000000")
//    var h = 0
//    var btm_margin = 0
//    private var defaultBrightness: Float = 0F
//    private lateinit var compassSensor: CompassSensor
//    private var currentLocation: CurrentLocation? = null
//    private lateinit var weatherManager: WeatherManager
//    private lateinit var mDrawerLayout: DrawerLayout
//    private var drawerRecyclerAdapter: BaseRecyclerAdapter<DrawerItemModel, DrawerMainScreenItemHolder>? = null
//    private var bp: BillingProcessor? = null
//    private var mLastClickTime: Long = 0
//    private var defaultPeekHeight: Int = 0
//    private var isHalfExpanded: Boolean = false
//    private var lat: Double = 0.0
//    private var lng: Double = 0.0
//    private var admobUtils: AdmobUtils? = null
//    private var weatherAdmobUtils: AdmobUtils? = null
//    private var mCameraPreview: CapturePreview? = null
//    private var modalDismissWithAnimation = false
//    private var mBottomMap: GoogleMap? = null
//    private var cityName: String?= null
//
//    val delayInMillis: Long = 500
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main_display)
//        setupStandardBottomSheet()
//        //animateStandardBottomSheetStates()
//        h = map_container.height
//        btm_margin = map_container.marginBottom
//        defaultInit()
//        recyclerViewInit()
//        clickListeners()
//        loadAds()
//
//    }
//
//    private fun loadAds() {
//        //ads
//        admobUtils = AdmobUtils(this)
//        admobUtils?.loadNativeAd(fl_adplaceholder, R.layout.ad_unified, NativeAdsIdType.MM_NATIVE_AM)
//        admobUtils?.setNativeAdListener(object : AdmobUtils.NativeAdListener {
//            override fun onNativeAdLoaded() {
//                ad_container.visibility = View.VISIBLE
//            }
//
//            override fun onNativeAdError() {
//            }
//        })
//
//        weatherAdmobUtils = AdmobUtils(this)
//        weatherAdmobUtils?.loadNativeAd(fl_adplaceholder_weather, R.layout.ad_unified_banner, NativeAdsIdType.MM_NATIVE_AM)
//        weatherAdmobUtils?.setNativeAdListener(object : AdmobUtils.NativeAdListener {
//            override fun onNativeAdLoaded() {
//                weather_ad_container.visibility = View.VISIBLE
//            }
//
//            override fun onNativeAdError() {
//            }
//        })
//
//
//    }
//
//    private fun statusBarSet() {
//        //make translucent statusBar on kitkat devices
//        if (Build.VERSION.SDK_INT in 19..20) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        }
//        //make fully Android Transparent Status bar
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
//            window.statusBarColor = ContextCompat.getColor(this, R.color.status_bar_color)
//        }
//    }
//
//    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
//        val win = activity.window
//        val winParams = win.attributes
//        if (on) {
//            winParams.flags = winParams.flags or bits
//        } else {
//            winParams.flags = winParams.flags and bits.inv()
//        }
//        win.attributes = winParams
//    }
//
//    private fun recyclerViewInit() {
//        drawerRecyclerAdapter = BaseRecyclerAdapter(R.layout.drawer_items_layout, DrawerMainScreenItemHolder::class.java)
//        drawer_rv.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
//        drawer_rv.adapter = drawerRecyclerAdapter
//        drawerRecyclerAdapter!!.setData(getDrawerItemsList())
//
//    }
//
//    override fun onBillingInitialized() {
//    }
//
//    override fun onPurchaseHistoryRestored() {
//    }
//
//    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
//
//        TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//
//    }
//
//    override fun onBillingError(errorCode: Int, error: Throwable?) {
//    }
//
//
//    private fun defaultInit() {
//
//        statusBarSet()
//        // init compass
//        compassSensor = CompassSensor(this)
//        compassSensor.setListener(this)
//        compassSensor.start()
//
//        //current location
//        currentLocation = CurrentLocation(this)
//
//        // default brightness
//        defaultBrightness = window.attributes.screenBrightness
//
//        // weather manager
//        weatherManager = WeatherManager(this)
//
//        // permission request
//        val permissions: Array<String> = arrayOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//        Permissions.check(this, permissions, null, null, object : PermissionHandler() {
//            override fun onGranted() {
//                currentLocation!!.getLocation(this@MainDisplayActivity)
//            }
//        })
//
//        mDrawerLayout = findViewById(R.id.drawer_layout)
//        // init inApp purchase
//        bp = BillingProcessor(this, Constants.ADS_REMOVE_KEY, this)
//        bp?.initialize()
//
//        mCameraPreview = findViewById(R.id.cameraPreview)
//        mCameraPreview?.visibility = View.GONE
//
//
//        defaultPeekHeight = standardBottomSheetBehavior.peekHeight
//
//        //load bottom map
//        loadBottomMap()
//
//        address_txt.isSelected = true
//
//    }
//
//    private fun clickListeners() {
//
//        drawer_img.setOnClickListener {
//
//            mDrawerLayout.openDrawer(GravityCompat.START)
//
//        }
//
//        loading_layout.setOnClickListener { }
//
//        camera_compass_layout.setOnClickListener {
//
//            // permission request
//            val permissions: Array<String> = arrayOf(
//                Manifest.permission.CAMERA
//            )
//            Permissions.check(this, permissions, null, null, object : PermissionHandler() {
//                override fun onGranted() {
//                    loading_layout.visibility = View.VISIBLE
//                    Handler().postDelayed(delayInMillis) {
//                        loading_layout.visibility = View.GONE
//                        mCameraPreview?.visibility = View.VISIBLE
//                        telescope_view.visibility = View.GONE
//                        google_map_main_layout.visibility = View.GONE
//                        top_shadow_view.visibility = View.GONE
//
//                        UtilsMethods.dimLight(this@MainDisplayActivity, defaultBrightness)
//                        compass_img_main.setImageDrawable(
//                            ContextCompat.getDrawable(
//                                this@MainDisplayActivity,
//                                R.drawable.ic_main_compass_standard_mode
//                            )
//                        )
//                        setDefaultBottomSheet()
//                    }
//                }
//            })
//
//
//        }
//        tele_compass_layout.setOnClickListener {
//            loading_layout.visibility = View.VISIBLE
//            Handler().postDelayed(delayInMillis) {
//                loading_layout.visibility = View.GONE
//                mCameraPreview?.visibility = View.VISIBLE
//                telescope_view.visibility = View.VISIBLE
//                google_map_main_layout.visibility = View.GONE
//                top_shadow_view.visibility = View.VISIBLE
//
//                UtilsMethods.dimLight(this, defaultBrightness)
//                compass_img_main.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_main_compass_telescope_mode))
//                setDefaultBottomSheet()
//
//            }
//        }
//
//        night_compass_layout.setOnClickListener {
//            loading_layout.visibility = View.VISIBLE
//            Handler().postDelayed(delayInMillis) {
//                loading_layout.visibility = View.GONE
//                mCameraPreview?.visibility = View.VISIBLE
//                telescope_view.visibility = View.GONE
//                google_map_main_layout.visibility = View.GONE
//                UtilsMethods.dimLight(this, 30F)
//                top_shadow_view.visibility = View.GONE
//
//                compass_img_main.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_main_compass_night_mode))
//                setDefaultBottomSheet()
//            }
//
//        }
//        digi_compass_layout.setOnClickListener {
//            loading_layout.visibility = View.VISIBLE
//            Handler().postDelayed(delayInMillis) {
//                loading_layout.visibility = View.GONE
//                mCameraPreview?.visibility = View.VISIBLE
//                telescope_view.visibility = View.GONE
//                google_map_main_layout.visibility = View.GONE
//                UtilsMethods.dimLight(this, defaultBrightness)
//                top_shadow_view.visibility = View.GONE
//
//                compass_img_main.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_compass_pin))
//                setDefaultBottomSheet()
//            }
//
//        }
//        google_compass_layout.setOnClickListener {
//
//
//            loading_layout.visibility = View.VISIBLE
//            Handler().postDelayed(delayInMillis) {
//                loading_layout.visibility = View.GONE
//
//                mCameraPreview?.visibility = View.GONE
//                telescope_view.visibility = View.GONE
//                google_map_main_layout.visibility = View.VISIBLE
//                top_shadow_view.visibility = View.GONE
//
//                loadMap(false)
//                UtilsMethods.dimLight(this, defaultBrightness)
//                compass_img_main.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_main_compass_google_maps))
//                setDefaultBottomSheet()
//            }
//
//        }
//        sat_compass_layout.setOnClickListener {
//
//            loading_layout.visibility = View.VISIBLE
//            Handler().postDelayed(delayInMillis) {
//                loading_layout.visibility = View.GONE
//
//                mCameraPreview?.visibility = View.GONE
//                telescope_view.visibility = View.GONE
//                google_map_main_layout.visibility = View.VISIBLE
//                top_shadow_view.visibility = View.GONE
//
//                loadMap(true)
//                UtilsMethods.dimLight(this, defaultBrightness)
//                compass_img_main.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_main_compass_google_maps_satellites))
//                setDefaultBottomSheet()
//            }
//
//
//        }
//
//        stand_compass_layout.setOnClickListener {
//
//            loading_layout.visibility = View.VISIBLE
//            Handler().postDelayed(delayInMillis) {
//                loading_layout.visibility = View.GONE
//
//                mCameraPreview?.visibility = View.GONE
//                telescope_view.visibility = View.GONE
//                google_map_main_layout.visibility = View.GONE
//                top_shadow_view.visibility = View.GONE
//
//                UtilsMethods.dimLight(this, defaultBrightness)
//                compass_img_main.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_main_compass_standard_mode))
//                setDefaultBottomSheet()
//
//            }
//
//
//        }
//
//        weather_main_layout.setOnClickListener {
//            cityName.let {
//                val intent = Intent(this@MainDisplayActivity,MainWeatherActivity::class.java)
//                intent.putExtra(Constants.CITY_NAME,it)
//                startActivity(intent)
//            }
//        }
//
//    }
//
//    private fun setDefaultBottomSheet() {
//        if (!isHalfExpanded) {
//            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED;
//        }
//    }
//
//    private fun getDrawerItemsList(): ArrayList<DrawerItemModel> {
//        val itemsArrayList = ArrayList<DrawerItemModel>()
//        itemsArrayList.add(DrawerItemModel(3, R.drawable.ic_share, "Share with friends"))
//        itemsArrayList.add(DrawerItemModel(2, R.drawable.ic_like, "Rate Us"))
//        itemsArrayList.add(DrawerItemModel(1, R.drawable.ic_ad_remove_dark, "Remove Ads"))
//        itemsArrayList.add(DrawerItemModel(4, R.drawable.ic_privacy_policy, "Privacy Policy"))
//        return itemsArrayList
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onMessageEvent(event: DrawerItemModel) {
//        if (isDoubleClick()) {
//            when (event.id) {
//                1 -> {
//                    if (bp?.isInitialized!! && bp?.isOneTimePurchaseSupported!!) {
//                        bp?.purchase(this, Constants.IN_APP_NAME_REMOVE_ADS)
//                    } else {
//                        Toast.makeText(this, "Service initialization failed.. Please try again!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                2 -> CommonUtils.openUrl(this, "https://play.google.com/store/apps/details?id=$packageName")
//                3 -> CommonUtils.shareText(
//                    this,
//                    "Try GPS Live Street Map and Travel Navigation https://play.google.com/store/apps/details?id=$packageName"
//                );
//                4 -> CommonUtils.openPrivacyPolicyView(this, Constants.PRIVACY_POLICY_LINK)
//
//            }
//        }
//
//    }
//
//    public override fun onStart() {
//        super.onStart()
//        EventBus.getDefault().register(this)
//    }
//
//    public override fun onStop() {
//        super.onStop()
//        EventBus.getDefault().unregister(this)
//    }
//
//    private fun isDoubleClick(): Boolean {
//        // mis-clicking prevention, using threshold of 1000 ms
//        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//            return false
//        }
//        mLastClickTime = SystemClock.elapsedRealtime()
//        return true
//    }
//
//    private fun loadMap(isSat: Boolean) {
//        // init map
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.google_map_main) as SupportMapFragment?
//        mapFragment?.getMapAsync {
//
//            /* try {
//                 it.isMyLocationEnabled = true
//             } catch (e: SecurityException) {
//             }*/
//
//            it.setPadding(0, 0, 0, 70)
////            it.uiSettings.isMyLocationButtonEnabled = false;
//            if (isSat) {
//                it?.mapType = GoogleMap.MAP_TYPE_SATELLITE
//            } else {
//                it?.mapType = GoogleMap.MAP_TYPE_NORMAL
//            }
//            val currentLatLng = LatLng(lat, lng)
//            it?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f))
//        }
//    }
//
//
//    private fun loadBottomMap() {
//        // init map
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.google_map_btm) as SupportMapFragment?
//        mapFragment?.getMapAsync {
//            mBottomMap = it
//        }
//    }
//
//
//    private fun setupStandardBottomSheet() {
//        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
//        standardBottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_PEEK_HEIGHT
//        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
//
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//                        Log.e(localClassName, "Expanded")
//                        isHalfExpanded = false
//                        standardBottomSheet.setBackgroundColor(ContextCompat.getColor(this@MainDisplayActivity, R.color.white))
//                        constraintLayout.setBackgroundColor(ContextCompat.getColor(this@MainDisplayActivity, R.color.white))
//                        slideView.visibility = View.INVISIBLE
//                    }
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        isHalfExpanded = false
//
//                        Log.e(localClassName, "collapsed")
//
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                        "STATE_DRAGGING" + bottomSheet.layoutParams.height
//                        standardBottomSheet.setBackgroundColor(ContextCompat.getColor(this@MainDisplayActivity, android.R.color.transparent))
//                        constraintLayout.background = ContextCompat.getDrawable(this@MainDisplayActivity, R.drawable.btm_bac)
//                        slideView.visibility = View.VISIBLE
//                    }
//                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
//                        Log.e(localClassName, "half")
//
//                        isHalfExpanded = false
//                    }
//                    BottomSheetBehavior.STATE_HIDDEN -> Log.e(localClassName, "Hidden")
//
//                    BottomSheetBehavior.STATE_SETTLING -> {
//                        standardBottomSheet.setBackgroundColor(ContextCompat.getColor(this@MainDisplayActivity, android.R.color.transparent))
//                        constraintLayout.background = ContextCompat.getDrawable(this@MainDisplayActivity, R.drawable.btm_bac)
//                        slideView.visibility = View.VISIBLE
//                        Log.e(localClassName, "Setting")
//                    }
//
//
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                val h = bottomSheet.height
//                val off = h * slideOffset
//                val finalVal = (off * 0.85F).roundToInt()
//                Log.e(
//                    localClassName, "$slideOffset $off $finalVal $h ${standardBottomSheetBehavior.peekHeight}"
//                )
//                /*  map_container.updateLayoutParams {
//                      height = h - finalVal - standardBottomSheetBehavior.peekHeight
//                  }
//  */
//                accelerometer_view.alpha = 1 - slideOffset
////                map_container.translationY =-off
//                setMargins(map_container, 0, 0, 0, finalVal + pxToDp(standardBottomSheetBehavior.peekHeight + btm_margin))
//
//
//                // map_container.setPadding(0,0,0, pxToDp(off.toInt()))
//
///*                when (standardBottomSheetBehavior.state) {
//                    BottomSheetBehavior.STATE_EXPANDED -> "STATE_EXPANDED" + bottomSheet.layoutParams.height
//                    BottomSheetBehavior.STATE_COLLAPSED -> "STATE_COLLAPSED" + bottomSheet.layoutParams.height
//                    BottomSheetBehavior.STATE_DRAGGING -> "STATE_DRAGGING" + bottomSheet.layoutParams.height
//                    BottomSheetBehavior.STATE_HALF_EXPANDED -> "STATE_HALF_EXPANDED" + bottomSheet.layoutParams.height
//                    BottomSheetBehavior.STATE_HIDDEN -> "STATE_HIDDEN" + bottomSheet.layoutParams.height
//                    BottomSheetBehavior.STATE_SETTLING -> "STATE_SETTLING" + bottomSheet.layoutParams.height}*/
//
//                val fraction = (slideOffset + 1f) / 2f
//                val color = ArgbEvaluatorCompat.getInstance().evaluate(fraction, startColor, endColor)
//                slideView.setBackgroundColor(color)
//            }
//        }
//        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
//        standardBottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
////        textView.setTextColor(textColor)
//    }
//
//
//    // compass listener
//    override fun onNewAzimuth(azimuth: Float, pitch: Float, oldDegree: Float) {
//        compassSensor.setCompassAnimation(azimuth, compass_img_main)
//        accelerometer_view.sensorValue.setRotation(azimuth, oldDegree, pitch)
//    }
//
//    override fun magneticField(magneticField: Float) {
//        val finalValue = magneticField.toInt()
//
//        val yourString = "$finalValue μT"
//
//        // val spanString = SpannableString(yourString)
//        // spanString.setSpan(RelativeSizeSpan(0.8f), yourString.length - 2, yourString.length, 0); // set size
//        /* spanString.setSpan(
//             ForegroundColorSpan(ContextCompat.getColor(this,R.color.colorAccent)),yourString.length - 2, yourString.length,
//             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//         )*/
//        txt_magnetic_main.text = increaseFontSizeForPath(SpannableString(yourString), " μT", 0.7f);
//
//
////        txt_magnetic_main.text = finalValue.toString() + increaseFontSizeForPath(SpannableString("μT"),"μT",-1f)
//    }
//
//    private fun increaseFontSizeForPath(spannable: Spannable, path: String, increaseTime: Float): Spannable {
//        val startIndexOfPath = spannable.toString().indexOf(path)
//        spannable.setSpan(
//            RelativeSizeSpan(increaseTime), startIndexOfPath,
//            startIndexOfPath + path.length, 0
//        )
//
//        return spannable
//    }
//
//    // weather listener
//    override fun onWeatherSuccess(weatherData: WeatherData?) {
//        runOnUiThread {
//            Log.d(localClassName, "weather ${weatherData?.address}")
//            mini_temp_txt.text = WeatherUtility.formatTemperature(WeatherUtility.toKelvinToFahrenheit(weatherData?.main?.tempMin).toDouble())
//            max_temp_txt.text = WeatherUtility.formatTemperature(WeatherUtility.toKelvinToFahrenheit(weatherData?.main?.tempMax).toDouble())
//            wind_txt.text = weatherData?.wind?.speed.toString() + " MHP"
//            pressure_txt.text = String.format(Locale.US, "%s hPa", weatherData?.main?.pressure)
//            humidity_txt.text = String.format(Locale.US, "%s %%", weatherData?.main?.humidity)
//            val visibilityKm = weatherData?.visibility?.div(1000)
//            visibility_txt.text = weatherData?.visibility.toString() + " ($visibilityKm km)"
//            val sunRise = weatherData?.sys?.sunrise
//            val sunSet = weatherData?.sys?.sunset
//            val sunR = DATE_FORMAT.format(sunRise?.times(1000)?.toLong()?.let { Date(it) })
//            val sunS = DATE_FORMAT.format(sunSet?.times(1000)?.toLong()?.let { Date(it) })
//            sunrise_txt.text = sunR
//            sunset_txt.text = sunS
//
//            currentLocation?.removeFusedLocationClient()
//        }
//
//    }
//
//    override fun onWeatherFailed(error: String?) {
//    }
//
//
//    // current location listener
//
//    override fun gotLocation(location: Location?) {
//
//        Log.d(localClassName, "location ${location?.latitude}")
//
//        lat = location!!.latitude
//        lng = location.longitude
//
//
//        val lonStr = WeatherUtility.formatDms(lng.toFloat()) + " " + WeatherUtility.formatDms(lng.toFloat())
//
//        val latStr = WeatherUtility.formatDms(lat.toFloat()) + " " + WeatherUtility.formatDms(lat.toFloat())
//
////        txt_lon_lat.text = String.format("%s / %s", latStr, lonStr)
//
//        btm_lat_txt.text = latStr
//        btm_long_txt.text = lonStr
//
//        val fString = String.format(Locale.US, "%d m", location.altitude.toLong())
//
//        txt_altitude_main.text = increaseFontSizeForPath(SpannableString(fString), " m", 0.8f);
//
//        btm_address_txt.text = UtilsMethods.getCompleteAddressString(this, lat, lng)
//
//        address_txt.text = UtilsMethods.getCompleteAddressString(this, lat, lng)
//        cityName = UtilsMethods.getLocalCity(this,lat,lng)
//
////
//        weatherManager.getWeatherData(location, getString(R.string.weather_api_key))
//
//        val currentLatLng = LatLng(lat, lng)
//
//        mBottomMap?.addMarker(
//            MarkerOptions()
//                .position(currentLatLng)
//                .title("Current Location")
//        )
//        mBottomMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f))
//
//    }
//
//
//    fun pxToDp(px: Int): Int {
//        return (px / Resources.getSystem().displayMetrics.density).toInt()
//    }
//
//    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
//        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
//            val p = view.layoutParams as ViewGroup.MarginLayoutParams
//            p.setMargins(left, top, right, bottom)
//            view.requestLayout()
//        }
//    }
//
//    fun dpToPx(dp: Int): Int {
//        return (dp * Resources.getSystem().displayMetrics.density).toInt()
//    }
//
//
//    override fun onBackPressed() {
//
//        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
//            mDrawerLayout.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (!bp?.handleActivityResult(requestCode, resultCode, data)!!) {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//        when (requestCode) {
//            CurrentLocation.REQUEST_LOCATION -> when (resultCode) {
//                Activity.RESULT_OK -> {
//                    currentLocation?.getLocation(this)
//                }
//                Activity.RESULT_CANCELED -> Toast.makeText(
//                    this, "Please enable Gps for get current location.", Toast.LENGTH_SHORT
//                ).show()
//                else -> {
//                }
//            }
//        }
//
//
//    }
//
//    override fun onDestroy() {
//        bp?.release()
//        super.onDestroy()
//
//        admobUtils?.destroyNativeAd()
//        weatherAdmobUtils?.destroyNativeAd()
//
//    }
//
//
//    override fun attachBaseContext(newBase: Context) {
//        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
//    }
//}
