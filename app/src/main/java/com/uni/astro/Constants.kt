package com.uni.astro

object Constants {
    const val BASE_URL = "http://197.52.142.136:80/new_astro/"
    const val API_KEY = "156c4675-9608-4591-1111-00000"

    //for referral deep linking
    const val REFERRAL_LINK = BASE_URL + "ref/?code="

    //here we place dummy key please replace it with your original one
    const val API_URL = "https://apis.argear.io/"
    const val API_KEY_ARGEAR = "ffc5e797a396f89d8812c831"
    const val SECRET_KEY = "12bc915afc8f6e10449552f8fd4108f6a7e5f9e3d1107550ef37419e7057f221"
    const val AUTH_KEY =
        "U2FsdGVkX182aPzEQtG/vJizeGcmtIai1rf91IvhZ9EwxjGCbNU6unNgAVE163XsD6ylhgK8zFKP9xBK8rBdeA=="

    // banuba token
    const val banubaToken =
        "E2JR9K8rYMQf8b/HLYcWOjRKfl6KoEI4M7W1GH3HmBnIrkvZ5UFkfyXBArfdDPJ+ruILLhDjOrIbQji4RQLoFqZ6zIvTZOOVAcdrM/qGgzdNiv1jLHq12mexlUOOm7mxDBeuccYFsN5AggiYDzhEQAD42AxMTvFOvMP+3tmO8h9yOzUbFjK4AlOFL0jWE703NrxoOfEs7NWKfdi9gS/ONAAocVD8ErO+WUOCmnHSqhzoGvtviNv/PZUBtoxZp2Pp2Hs/r+Id2/7WwqUx4N3+g75l5B1UwBsQv73upPFHmlNIbz7z/KJuSpNu5T+g26UlChfzQLzs3XEC96zTNXD7I/tIkZSHnBBWdnBLjTP8kDB+f1kG0WmkhUoP1opxJ0okBsAUqEf3y2XcnZvaMTQcK7co6qLwR46GBBFWieIVMfo/jBV/iUaAyLDFbAs6B8+ed0LO+7FCEeHhM5omyQ1/xZTzqmRg4curzydcK+Ig4B30vXFrWGOWJXx6yrJR6z5GED1CG9ckvwfxQrPExACNavOCNEowI10acdRGMTmyDF7wTJCV5lq173bc6ELl0kFIGeUj4iTGpRabOsD6JDtGlzNTP9xrPI62exJNVeJ4y/GgSg=="
    const val APP_AD_COLONY_ID = "appc2e347b482d54cab8b"
    const val AD_COLONY_BANNER_ID = "vzdb67721f381948c292"
    const val AD_COLONY_INTERSTITIAL_ID = "vza31e4740f6e94f5d9b"
    val AD_COLONY_UNIT_Zone_Ids = arrayOf(AD_COLONY_BANNER_ID, AD_COLONY_INTERSTITIAL_ID)
    const val privacy_policy = "encept.co"
    const val terms_conditions = "\"encept.co"

    // if you want a user can't share a video from your app then you have to set this value to true
    const val IS_SECURE_INFO = false

    // if you show the ad on after every specific video count
    const val SHOW_AD_ON_EVERY = 8

    // if you want a video thumbnail image show rather then a video gif then set the below value to false.
    const val IS_SHOW_GIF = true

    // if you want to disable all the toasts in the app
    const val IS_TOAST_ENABLE = true

    // if you want to add a limit that a user can watch only 6 video then change the below value to true
    // if you want to change the demo videos limit count then set the count as you want
    const val IS_DEMO_APP = false
    const val DEMO_APP_VIDEOS_COUNT = 6

    // maximum time to record the video for now it is 30 sec
    @JvmField
    var MAX_RECORDING_DURATION = 600000
    @JvmField
    var RECORDING_DURATION = 30000

    // minimum time of recode a video.
    @JvmField
    var MIN_TIME_RECORDING = 3000

    // max photo allowed for photo video upload
    @JvmField
    var MAX_PICS_ALLOWED_FOR_VIDEO = 5

    //max time for photo videos
    @JvmField
    var MAX_TIME_FOR_VIDEO_PICS = 10

    // minimum trim chunk time span of a video.
    @JvmField
    var MIN_TRIM_TIME = 5

    // maximum trim chunk time span of a video for now it is 30 sec
    @JvmField
    var MAX_TRIM_TIME = 30

    //video description char limit during posting the video
    const val VIDEO_DESCRIPTION_CHAR_LIMIT = 250

    // Username char limit during signup and edit the account
    const val USERNAME_CHAR_LIMIT = 30

    // user profile bio char limit during edit the profile
    const val BIO_CHAR_LIMIT = 150

    // set the profile image max size for now it is 400 * 400
    const val PROFILE_IMAGE_SQUARE_SIZE = 300
    const val CURRENCY = "$"

    // Make product ids of different prices on google play console and place that ids in it.
    const val COINS0 = "100"
    const val PRICE0 = CURRENCY + "1"
    const val Product_ID0 = "android.test.purchased"
    const val COINS1 = "500"
    const val PRICE1 = CURRENCY + "5"
    const val Product_ID1 = "com.uni.astro.coin2"
    const val COINS2 = "2000"
    const val PRICE2 = CURRENCY + "20"
    const val Product_ID2 = "com.uni.astro.coin3"
    const val COINS3 = "5000"
    const val PRICE3 = CURRENCY + "50"
    const val Product_ID3 = "com.uni.astro.coin4"
    const val COINS4 = "10000"
    const val PRICE4 = CURRENCY + "100"
    const val Product_ID4 = "com.uni.astro.coin5"


    @JvmField
    var tag = "astro_"
    const val ALL_IMAGE_DEFAULT_SIZE = 500
}