package com.uni.astro.activitesfragments.walletandwithdraw;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.uni.astro.Constants;
import com.uni.astro.R;
import com.uni.astro.activitesfragments.profile.analytics.DateOperations;
import com.uni.astro.apiclasses.ApiLinks;
import com.uni.astro.databinding.ActivityEarnCoinsBinding;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EarnCoinsA extends AppCompatActivity {
    ActivityEarnCoinsBinding binding;


    int todayCoins = 0;
    private String numberOfCoins;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEarnCoinsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.in_from_bottom);
        Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.out_to_top);

        binding.viewflipper.setInAnimation(inAnimation);
        binding.viewflipper.setOutAnimation(outAnimation);
        binding.viewflipper.setFlipInterval(2000);
        binding.viewflipper.startFlipping();


        numberOfCoins = Functions.getSharedPreference(this).getString(Variables.U_WALLET, "0");
        getCoinWorth(numberOfCoins);




        callApiShowCheckIn();
        ClickActions();


    }
    private void getCoinWorth(String coins){
        if (coins == "0"){
            binding.coinWorthTxt.setText(Constants.CURRENCY +" 0");
        }
        Double numberOfCoinsInOneDollar = Double.parseDouble(coins);
        double total_amount = (1 / numberOfCoinsInOneDollar);
        DecimalFormat decimalFormat = new DecimalFormat("#0.0000");
        String formattedAmount = decimalFormat.format(total_amount);
        binding.coinWorthTxt.setText(Constants.CURRENCY + formattedAmount);

        binding.totalCoins.setText(getString(R.string.coins)+" "+coins);
    }


    public void ClickActions() {

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (todayCoins > 0){
                    callApiAddCheck();
                    todayCoins += Integer.parseInt(numberOfCoins);
                    Functions.getSharedPreference(getApplicationContext()).edit().putString(Variables.U_WALLET, ""+todayCoins).commit();
                    getCoinWorth(String.valueOf(todayCoins));

                }



            }
        });

    }

    private void setDisableButton(){
        binding.collectBtn.setEnabled(false);
        binding.collectBtn.setBackground(getDrawable(R.drawable.disable_round_btn));
    }

    public void setChecked(CardView cardView, ImageView icon, View dayLine, TextView dayText) {
        cardView.setActivated(true);
        cardView.setBackground(getDrawable(R.drawable.round_card_select));

        icon.setBackgroundResource(R.drawable.ic_check);
        dayLine.setBackgroundColor(getResources().getColor(R.color.selected_card));
        dayText.setTextColor(getResources().getColor(R.color.white));

        cardView.setClickable(false);
    }

    public void setUnChecked(CardView cardView, ImageView icon, View dayLine) {
        cardView.setActivated(false);
        cardView.setCardBackgroundColor(getResources().getColor(R.color.semi_transparent2));

        icon.setBackgroundResource(R.drawable.ic_coin);
        dayLine.setBackgroundColor(getResources().getColor(R.color.semi_transparent2));
    }


    public void disableCard(CardView cardView, ImageView icon, View dayLine,TextView dayText) {
        cardView.setActivated(false);
        cardView.setBackground(getDrawable(R.drawable.round_card_disable));
        dayText.setTextColor(getResources().getColor(R.color.white));

        icon.setBackgroundResource(R.drawable.ic_coin);
        dayLine.setBackgroundColor(getResources().getColor(R.color.graycolor));
    }


    private void callApiShowCheckIn() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID, "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(this,false,false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.showDailyCheckins, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                parseData(resp);
            }
        });

    }

    long count = 0;
    long days;
    public void parseData(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");
                String startDate = msg.optString("starting_date");
                String current_date = msg.optString("server_datetime");
                String currDate = DateOperations.INSTANCE.changeDateFormat("yyyy-MM-dd hh:mm:ss","yyyy-MM-dd",current_date);
                count = getDays(startDate, currDate);
                count += 1;
                binding.daysCountTxt.setText("" + count);


                disablecards();

                JSONArray checkins = msg.optJSONArray("checkins");
                for (int i = 0; i < checkins.length(); i++) {
                    JSONObject jsonObject1 = checkins.getJSONObject(i);
                    JSONObject dailyCheckin = jsonObject1.getJSONObject("DailyCheckin");
                    days = getDays(startDate,dailyCheckin.optString("created"));
                  ++days;
//                    if (days > 0){
//                        days -= 1;
//                    }

                    if (days == 1) {
                        if (count == days){
                            setChecked(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
                            checkCoinCollet();
                        }

                    } else if (days == 2) {
                        if (count == days){
                            setChecked(binding.day2card, binding.day2icon, binding.day2line,binding.day2txt);
                            checkCoinCollet();
                        }

                    } else if (days == 3) {
                        if (count == days){
                            setChecked(binding.day3card, binding.day3icon, binding.day3line,binding.day3txt);
                            checkCoinCollet();
                        }

                    } else if (days == 4) {
                        if (count == days){
                            setChecked(binding.day4card, binding.day4icon, binding.day4line,binding.day4txt);
                            checkCoinCollet();
                        }

                    } else if (days == 5) {
                        if(count == days){
                            setChecked(binding.day5card, binding.day5icon, binding.day5line,binding.day5txt);
                            checkCoinCollet();
                        }

                    } else if (days == 6) {
                        if(count == days){
                            setChecked(binding.day6card, binding.day6icon, binding.day6line,binding.day6txt);
                            checkCoinCollet();
                        }
                    } else if (days == 7) {
                        if (count == days){
                            setChecked(binding.day7card, binding.day7icon, binding.day7line,binding.day7txt);
                            checkCoinCollet();
                        }

                    }

                    if (days == count) {
                        binding.calendericon.setImageDrawable(getResources().getDrawable(R.drawable.ic_calendar_active));
                    }


                }

            }

        } catch (JSONException e) {
            
        }
    }


    public long getTotalDays(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date1 = dateFormat.parse(date);

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);

            Calendar cal2 = Calendar.getInstance();

            long diffInMillis = cal2.getTimeInMillis() - cal1.getTimeInMillis();
            long daysBetween = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
            return daysBetween;
        } catch (Exception e) {

            return 0;
        }

    }


    public long getDays(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date1 = dateFormat.parse(startDate);
            Date date2 = dateFormat.parse(endDate);

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);

            long diffInMillis = cal2.getTimeInMillis() - cal1.getTimeInMillis();
            long daysBetween = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

            return daysBetween;
        } catch (Exception e) {

            return 0;
        }

    }


    public void disablecards() {

        binding.day1card.setClickable(false);
        binding.day2card.setClickable(false);
        binding.day3card.setClickable(false);
        binding.day4card.setClickable(false);
        binding.day5card.setClickable(false);
        binding.day6card.setClickable(false);
        binding.day7card.setClickable(false);

        binding.day1icon.setBackgroundResource(R.drawable.ic_coin);
        binding.day2icon.setBackgroundResource(R.drawable.ic_coin2);
        binding.day3icon.setBackgroundResource(R.drawable.ic_coin3);
        binding.day4icon.setBackgroundResource(R.drawable.ic_coin4);
        binding.day5icon.setBackgroundResource(R.drawable.ic_coin5);
        binding.day6icon.setBackgroundResource(R.drawable.ic_coin5);
        binding.day7icon.setBackgroundResource(R.drawable.ic_coin5);

        if (count == 0) {
            binding.day1card.setClickable(true);

        } else if (count == 1) {
            todayCoins = 20;
            days = 1;
            binding.day1card.setClickable(true);
            binding.day1card.setBackground(getDrawable(R.drawable.round_card));
        } else if (count == 2) {
            todayCoins = 30;
            days = 2;
            disableCard(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
            binding.day2card.setClickable(true);
            binding.day2card.setBackground(getDrawable(R.drawable.round_card));
        } else if (count == 3) {
            todayCoins = 40;
            days = 3;
            disableCard(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
            disableCard(binding.day2card, binding.day2icon, binding.day2line,binding.day2txt);
            binding.day3card.setClickable(true);
            binding.day3card.setBackground(getDrawable(R.drawable.round_card));
        } else if (count == 4) {
            todayCoins = 50;
            days = 4;
            disableCard(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
            disableCard(binding.day2card, binding.day2icon, binding.day2line,binding.day2txt);
            disableCard(binding.day3card, binding.day3icon, binding.day3line,binding.day3txt);
            binding.day4card.setClickable(true);
            binding.day4card.setBackground(getDrawable(R.drawable.round_card));
        } else if (count == 5) {
            todayCoins = 70;
            days = 5;
            disableCard(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
            disableCard(binding.day2card, binding.day2icon, binding.day2line,binding.day2txt);
            disableCard(binding.day3card, binding.day3icon, binding.day3line,binding.day3txt);
            disableCard(binding.day4card, binding.day4icon, binding.day4line,binding.day4txt);
            binding.day5card.setClickable(true);
            binding.day5card.setBackground(getDrawable(R.drawable.round_card));
        } else if (count == 6) {
            todayCoins = 70;
            days = 6;
            disableCard(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
            disableCard(binding.day2card, binding.day2icon, binding.day2line,binding.day2txt);
            disableCard(binding.day3card, binding.day3icon, binding.day3line,binding.day3txt);
            disableCard(binding.day4card, binding.day4icon, binding.day4line,binding.day4txt);
            disableCard(binding.day5card, binding.day5icon, binding.day5line,binding.day5txt);
            binding.day6card.setClickable(true);
            binding.day6card.setBackground(getDrawable(R.drawable.round_card));
        } else if (count == 7) {
            todayCoins = 70;
            days = 7;
            disableCard(binding.day1card, binding.day1icon, binding.day1line,binding.day1txt);
            disableCard(binding.day2card, binding.day2icon, binding.day2line,binding.day2txt);
            disableCard(binding.day3card, binding.day3icon, binding.day3line,binding.day3txt);
            disableCard(binding.day4card, binding.day4icon, binding.day4line,binding.day4txt);
            disableCard(binding.day5card, binding.day5icon, binding.day5line,binding.day5txt);
            disableCard(binding.day6card, binding.day6icon, binding.day6line,binding.day6txt);
            binding.day7card.setClickable(true);
            binding.day7card.setBackground(getDrawable(R.drawable.round_card));
        }

    }


    private void callApiAddCheck() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID, "0"));
            parameters.put("coin", "" + todayCoins);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(this,false,false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.addDailyCheckin, parameters, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");

                    if (code.equals("200")) {
                        callApiShowCheckIn();
                    } else {
                        Toast.makeText(EarnCoinsA.this, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
                        setDisableButton();
                    }
                } catch (JSONException e) {

                }
            }
        });

    }
    private void checkCoinCollet() {
        if (binding.day1card.isActivated())
            setDisableButton();
        if (binding.day2card.isActivated())
            setDisableButton();
        if (binding.day3card.isActivated())
            setDisableButton();
        if (binding.day4card.isActivated())
            setDisableButton();
        if (binding.day5card.isActivated())
            setDisableButton();
        if (binding.day6card.isActivated())
            setDisableButton();
        if (binding.day7card.isActivated())
            setDisableButton();


    }


}