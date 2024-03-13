package pt.ipp.estg.fitcheck.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import pt.ipp.estg.fitcheck.DataBases.DOHistoryDB;
import pt.ipp.estg.fitcheck.DataBases.DailyObjectiveDB;
import pt.ipp.estg.fitcheck.DataBases.TrainingDB;
import pt.ipp.estg.fitcheck.FragmentChange;
import pt.ipp.estg.fitcheck.Models.Training;
import pt.ipp.estg.fitcheck.R;
import pt.ipp.estg.fitcheck.Services.CountStepsForegroundService;


public class HomeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FragmentChange context;

    private TextView welcomeText;
    private TextView distWalked;
    private TextView posRankText;

    private PieChart pieChart;
    private BarChart barChart;

    private DailyObjectiveDB dailyObjectiveDB;
    private DOHistoryDB doHistoryDB;
    private TrainingDB trainingHistoryDB;
    private List<Training> historyList;
    private Long currentSteps;
    private Long currentGoalDay;

    ArrayList<String> dates = new ArrayList();

    CountStepsForegroundService services;
    Handler handler;

    private Runnable runnableCode = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {

            getDailyObjectiveAndCurrentSteps();

            handler = new Handler();

            handler.postDelayed(this, 500);

        }
    };

    public HomeFragment() {
        this.context = (FragmentChange) getContext();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dailyObjectiveDB = DailyObjectiveDB.getInstance(getActivity().getApplicationContext());
        doHistoryDB = DOHistoryDB.getInstance(getActivity().getApplicationContext());
        trainingHistoryDB = TrainingDB.getInstance(getActivity().getApplicationContext());


        if (getArguments() != null) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View contextView = inflater.inflate(R.layout.fragment_home, container, false);


        pieChart = contextView.findViewById(R.id.chart);
        barChart = contextView.findViewById(R.id.barChart);
        welcomeText = contextView.findViewById(R.id.welcomeText);
        distWalked = contextView.findViewById(R.id.distWalked);
        posRankText = contextView.findViewById(R.id.posRankText);

        welcomeText.setText("Bem vindo " + currentUser.getDisplayName());


        getDailyObjectiveAndCurrentSteps();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            MapsFragment mapsFragment = new MapsFragment();
            FragmentTransaction tr = getChildFragmentManager().beginTransaction();
            tr.replace(R.id.containerMap, mapsFragment);
            tr.commit();
        }

        return contextView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        getHistoricDist();

        runnableCode.run();

    }

    public void setPieChart() {
        Long goal;


        int walked;

        if (this.currentSteps == null) {
            walked = 0;
        } else {
            walked = this.currentSteps.intValue();
        }

        ArrayList<PieEntry> dataEntries = new ArrayList<>();

        if (currentGoalDay != null) {
            goal = currentGoalDay;//Goal is in meters converted on steps
            if (walked < goal) {
                Long missing = goal - walked;
                distWalked.setText("Você já deu " + walked + " passos\nFaltam " + missing + " passos");
                dataEntries.add(new PieEntry(walked, "Walked"));
                dataEntries.add(new PieEntry(missing, "Missing"));
            } else if (walked >= goal) {
                dataEntries.add(new PieEntry(goal, "Goal"));
                distWalked.setText("Parabéns!\nVocê concluiu o seu objetivo diário de " + goal + " passos!");
            }
        } else {
            if (distWalked != null) {
                distWalked.setText("Para ativar esta funcionalidade tem de definir um objetivo Diário\n" +
                        "Passos Dados: " + walked);
            }
        }

        PieDataSet dataSet = new PieDataSet(dataEntries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(15f);

        pieChart.setData(data);

        pieChart.setRenderer(new RoundedSlicesPieChartRenderer(pieChart, pieChart.getAnimator(), pieChart.getViewPortHandler()));

        pieChart.setExtraOffsets(0f, 5f, 0f, 0f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setDrawMarkers(false);
        pieChart.setTouchEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(false);
        pieChart.setDrawEntryLabels(false);

        //create hole in center
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleColor(Color.TRANSPARENT);

        //add text in center
        pieChart.setDrawCenterText(false);

        Description description = pieChart.getDescription();
        description.setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);


        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                ArrayList<Integer> colorsDark = new ArrayList<>();
                colorsDark.add(Color.LTGRAY);
                colorsDark.add(Color.TRANSPARENT);
                dataSet.setColors(colorsDark);
                data.setValueTextColor(Color.WHITE);
                legend.setTextColor(Color.WHITE);

                break;
            case Configuration.UI_MODE_NIGHT_NO:
                ArrayList<Integer> colorsLight = new ArrayList<>();
                colorsLight.add(Color.GRAY);
                colorsLight.add(Color.TRANSPARENT);
                dataSet.setColors(colorsLight);
                data.setValueTextColor(Color.LTGRAY);
                legend.setTextColor(Color.LTGRAY);

                break;
        }

        //pieChart.animateY(1000, Easing.EaseInOutQuad);

        pieChart.invalidate();
    }

    public void setBarChart() {

        ArrayList<String> xAxisvalue;
        xAxisvalue = this.getLast3Days();

        ArrayList<BarEntry> entries = new ArrayList<>();

        Iterator i = xAxisvalue.iterator();
        Iterator x = dates.iterator();
        float count = 0;
        while (i.hasNext()) {
            String temp = (String) i.next();

            Double d = this.getDistTrainingDay((String) x.next());


            entries.add(new BarEntry(count, d.floatValue()));
            count++;
        }

        BarDataSet barDataSet = new BarDataSet(entries, "");
        barDataSet.setDrawValues(false);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);


        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisvalue));


        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setDrawGridLines(true);
        yAxis.setGranularity(100f);

        YAxis axisRight = barChart.getAxisRight();
        axisRight.setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        Description description = barChart.getDescription();
        description.setEnabled(false);


        //Set Colors Depending on theme
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                barDataSet.setColors(Color.LTGRAY);
                xAxis.setTextColor(Color.WHITE);
                yAxis.setTextColor(Color.WHITE);
                description.setTextColor(Color.WHITE);

                break;
            case Configuration.UI_MODE_NIGHT_NO:
                barDataSet.setColors(Color.GRAY);
                xAxis.setTextColor(Color.BLACK);
                yAxis.setTextColor(Color.BLACK);
                description.setTextColor(Color.BLACK);

                break;
        }

        barChart.setTouchEnabled(false);

        //add animation
        barChart.animateY(1500);

        //draw chart
        barChart.invalidate();
    }

    private String getMonthName(int month) {
        month += 1;
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Fev";
            case 3:
                return "Mar";
            case 4:
                return "Abr";
            case 5:
                return "Mai";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Ago";
            case 9:
                return "Set";
            case 10:
                return "Out";
            case 11:
                return "Nov";
            case 12:
                return "Dez";

        }
        return null;
    }

    private String getMonthNumber(int month) {
        month += 1;
        switch (month) {
            case 1:
                return "01";
            case 2:
                return "02";
            case 3:
                return "03";
            case 4:
                return "04";
            case 5:
                return "05";
            case 6:
                return "06";
            case 7:
                return "07";
            case 8:
                return "08";
            case 9:
                return "09";
            case 10:
                return "10";
            case 11:
                return "11";
            case 12:
                return "12";

        }
        return null;
    }

    private String getDayNumber(int day) {
        switch (day) {
            case 1:
                return "01";
            case 2:
                return "02";
            case 3:
                return "03";
            case 4:
                return "04";
            case 5:
                return "05";
            case 6:
                return "06";
            case 7:
                return "07";
            case 8:
                return "08";
            case 9:
                return "09";
            case 10:
                return "10";
            default:
                return String.valueOf(day);

        }
    }

    private ArrayList<String> getLast3Days() {
        ArrayList<String> result = new ArrayList();
        Calendar c = Calendar.getInstance();
        Date d = new Date();

        c.setTime(d);

        c.add(Calendar.DATE, -2);
        String data1 = c.get(Calendar.DATE) + "/" + getMonthName(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);
        result.add(data1);
        dates.add(getDayNumber(c.get(Calendar.DATE)) + "/" + getMonthNumber(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR));

        c.add(Calendar.DATE, +1);
        String data2 = c.get(Calendar.DATE) + "/" + getMonthName(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);
        result.add(data2);
        dates.add(getDayNumber(c.get(Calendar.DATE)) + "/" + getMonthNumber(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR));

        c.add(Calendar.DATE, +1);
        String data3 = c.get(Calendar.DATE) + "/" + getMonthName(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR);
        result.add(data3);
        dates.add(getDayNumber(c.get(Calendar.DATE)) + "/" + getMonthNumber(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR));

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getDailyObjectiveAndCurrentSteps() {
        if (getView() != null) {

            //Get GoalDay
            dailyObjectiveDB.daoAccess().findDailyObjectiveByUser(currentUser.getUid()).observe(getViewLifecycleOwner(), dailyObjective -> {
                if (dailyObjective != null) {
                    this.currentGoalDay = dailyObjective.objective;
                } else {
                    this.currentGoalDay = null;
                }

                //Get CurrentSteps
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime now = LocalDateTime.now();
                doHistoryDB.daoAccess().findDOHistoryByUserByDate(currentUser.getUid(), dtf.format(now)).observe(getViewLifecycleOwner(), doHistory -> {

                    if (doHistory != null) {
                        this.currentSteps = doHistory.achieved;
                    }

                    this.setPieChart();

                });
            });
        }
    }

    private void getHistoricDist() {
        trainingHistoryDB.daoAccess().findTreinoByUser(currentUser.getUid()).observe(getViewLifecycleOwner(), treinos -> {
            historyList = treinos;

            this.setBarChart();
        });
    }

    public Double getDistTrainingDay(String date) {
        Double som = 0d;
        Iterator i = historyList.iterator();

        while (i.hasNext()) {

            Training temp = (Training) i.next();

            if (date.equals(temp.data)) {

                som += temp.distancia;
            }
        }

        return som;
    }

}

class RoundedSlicesPieChartRenderer extends PieChartRenderer {
    public RoundedSlicesPieChartRenderer(PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);

        chart.setDrawRoundedSlices(true);
    }

    protected void drawDataSet(Canvas c, IPieDataSet dataSet) {

        float angle = 0;
        float rotationAngle = mChart.getRotationAngle();

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        final RectF circleBox = mChart.getCircleBox();

        final int entryCount = dataSet.getEntryCount();
        final float[] drawAngles = mChart.getDrawAngles();
        final MPPointF center = mChart.getCenterCircleBox();
        final float radius = mChart.getRadius();
        final boolean drawInnerArc = mChart.isDrawHoleEnabled() && !mChart.isDrawSlicesUnderHoleEnabled();
        final float userInnerRadius = drawInnerArc
                ? radius * (mChart.getHoleRadius() / 100.f)
                : 0.f;
        final float roundedRadius = (radius - (radius * mChart.getHoleRadius() / 100f)) / 2f;
        final RectF roundedCircleBox = new RectF();
        final boolean drawRoundedSlices = drawInnerArc && mChart.isDrawRoundedSlicesEnabled();

        int visibleAngleCount = 0;
        for (int j = 0; j < entryCount; j++) {
            // draw only if the value is greater than zero
            if ((Math.abs(dataSet.getEntryForIndex(j).getY()) > Utils.FLOAT_EPSILON)) {
                visibleAngleCount++;
            }
        }

        final float sliceSpace = visibleAngleCount <= 1 ? 0.f : getSliceSpace(dataSet);
        final Path mPathBuffer = new Path();
        final RectF mInnerRectBuffer = new RectF();
        for (int j = 0; j < entryCount; j++) {

            float sliceAngle = drawAngles[j];
            float innerRadius = userInnerRadius;

            Entry e = dataSet.getEntryForIndex(j);

            // draw only if the value is greater than zero
            if (!(Math.abs(e.getY()) > Utils.FLOAT_EPSILON)) {
                angle += sliceAngle * phaseX;
                continue;
            }

            // Don't draw if it's highlighted, unless the chart uses rounded slices
            if (dataSet.isHighlightEnabled() && mChart.needsHighlight(j) && !drawRoundedSlices) {
                angle += sliceAngle * phaseX;
                continue;
            }

            final boolean accountForSliceSpacing = sliceSpace > 0.f && sliceAngle <= 180.f;

            mRenderPaint.setColor(dataSet.getColor(j));

            final float sliceSpaceAngleOuter = visibleAngleCount == 1 ?
                    0.f :
                    sliceSpace / (Utils.FDEG2RAD * radius);
            final float startAngleOuter = rotationAngle + (angle + sliceSpaceAngleOuter / 2.f) * phaseY;
            float sweepAngleOuter = (sliceAngle - sliceSpaceAngleOuter) * phaseY;
            if (sweepAngleOuter < 0.f) {
                sweepAngleOuter = 0.f;
            }

            mPathBuffer.reset();

            if (drawRoundedSlices) {
                float x = center.x + (radius - roundedRadius) * (float) Math.cos(startAngleOuter * Utils.FDEG2RAD);
                float y = center.y + (radius - roundedRadius) * (float) Math.sin(startAngleOuter * Utils.FDEG2RAD);
                roundedCircleBox.set(x - roundedRadius, y - roundedRadius, x + roundedRadius, y + roundedRadius);
            }

            float arcStartPointX = center.x + radius * (float) Math.cos(startAngleOuter * Utils.FDEG2RAD);
            float arcStartPointY = center.y + radius * (float) Math.sin(startAngleOuter * Utils.FDEG2RAD);

            if (sweepAngleOuter >= 360.f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                // Android is doing "mod 360"
                mPathBuffer.addCircle(center.x, center.y, radius, Path.Direction.CW);
            } else {

                if (drawRoundedSlices) {
                    mPathBuffer.arcTo(roundedCircleBox, startAngleOuter + 180, -180);
                }

                mPathBuffer.arcTo(
                        circleBox,
                        startAngleOuter,
                        sweepAngleOuter
                );
            }

            // API < 21 does not receive floats in addArc, but a RectF
            mInnerRectBuffer.set(
                    center.x - innerRadius,
                    center.y - innerRadius,
                    center.x + innerRadius,
                    center.y + innerRadius);

            if (drawInnerArc && (innerRadius > 0.f || accountForSliceSpacing)) {

                if (accountForSliceSpacing) {
                    float minSpacedRadius =
                            calculateMinimumRadiusForSpacedSlice(
                                    center, radius,
                                    sliceAngle * phaseY,
                                    arcStartPointX, arcStartPointY,
                                    startAngleOuter,
                                    sweepAngleOuter);

                    if (minSpacedRadius < 0.f)
                        minSpacedRadius = -minSpacedRadius;

                    innerRadius = Math.max(innerRadius, minSpacedRadius);
                }

                final float sliceSpaceAngleInner = visibleAngleCount == 1 || innerRadius == 0.f ?
                        0.f :
                        sliceSpace / (Utils.FDEG2RAD * innerRadius);
                final float startAngleInner = rotationAngle + (angle + sliceSpaceAngleInner / 2.f) * phaseY;
                float sweepAngleInner = (sliceAngle - sliceSpaceAngleInner) * phaseY;
                if (sweepAngleInner < 0.f) {
                    sweepAngleInner = 0.f;
                }
                final float endAngleInner = startAngleInner + sweepAngleInner;

                if (sweepAngleOuter >= 360.f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                    // Android is doing "mod 360"
                    mPathBuffer.addCircle(center.x, center.y, innerRadius, Path.Direction.CCW);
                } else {

                    if (drawRoundedSlices) {
                        float x = center.x + (radius - roundedRadius) * (float) Math.cos(endAngleInner * Utils.FDEG2RAD);
                        float y = center.y + (radius - roundedRadius) * (float) Math.sin(endAngleInner * Utils.FDEG2RAD);
                        roundedCircleBox.set(x - roundedRadius, y - roundedRadius, x + roundedRadius, y + roundedRadius);
                        mPathBuffer.arcTo(roundedCircleBox, endAngleInner, 180);
                    } else
                        mPathBuffer.lineTo(
                                center.x + innerRadius * (float) Math.cos(endAngleInner * Utils.FDEG2RAD),
                                center.y + innerRadius * (float) Math.sin(endAngleInner * Utils.FDEG2RAD));

                    mPathBuffer.arcTo(
                            mInnerRectBuffer,
                            endAngleInner,
                            -sweepAngleInner
                    );
                }
            } else {

                if (sweepAngleOuter % 360f > Utils.FLOAT_EPSILON) {
                    if (accountForSliceSpacing) {

                        float angleMiddle = startAngleOuter + sweepAngleOuter / 2.f;

                        float sliceSpaceOffset =
                                calculateMinimumRadiusForSpacedSlice(
                                        center,
                                        radius,
                                        sliceAngle * phaseY,
                                        arcStartPointX,
                                        arcStartPointY,
                                        startAngleOuter,
                                        sweepAngleOuter);

                        float arcEndPointX = center.x +
                                sliceSpaceOffset * (float) Math.cos(angleMiddle * Utils.FDEG2RAD);
                        float arcEndPointY = center.y +
                                sliceSpaceOffset * (float) Math.sin(angleMiddle * Utils.FDEG2RAD);

                        mPathBuffer.lineTo(
                                arcEndPointX,
                                arcEndPointY);

                    } else {
                        mPathBuffer.lineTo(
                                center.x,
                                center.y);
                    }
                }

            }

            mPathBuffer.close();

            mBitmapCanvas.drawPath(mPathBuffer, mRenderPaint);

            Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(0);
            strokePaint.setColor(Color.WHITE);
            mBitmapCanvas.drawPath(mPathBuffer, strokePaint);

            angle += sliceAngle * phaseX;
        }

        MPPointF.recycleInstance(center);
    }

}