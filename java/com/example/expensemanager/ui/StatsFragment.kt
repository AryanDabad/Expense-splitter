package com.example.expensemanager.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.expensemanager.Model.Data
import com.example.expensemanager.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DateFormat
import java.text.Format
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class StatsFragment : Fragment() {
    // Firebase
    private var mAuth: FirebaseAuth? = null
    private var mIncomeDatabase: DatabaseReference? = null
    private var mExpenseDatabase: DatabaseReference? = null
    private val type = arrayOf("Income", "Expense")
    private val values = floatArrayOf(0f, 0f)
    private val DateWiseIncome: MutableMap<Date?, Int> = TreeMap()
    private val DateWiseExpense: MutableMap<Date?, Int> = TreeMap()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_stats, container, false)
        mAuth = FirebaseAuth.getInstance()
        val mUser = mAuth!!.currentUser
        val uid = mUser!!.uid
        mIncomeDatabase = FirebaseDatabase.getInstance().reference.child("IncomeData").child(uid)
        mExpenseDatabase = FirebaseDatabase.getInstance().reference.child("ExpenseData").child(uid)
        mIncomeDatabase!!.keepSynced(true)
        mExpenseDatabase!!.keepSynced(true)
        mIncomeDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                values[0] = 0F
                DateWiseIncome.clear()
                for (mysnap in snapshot.children) {
                    val data = mysnap.getValue(
                        Data::class.java
                    )
                    values[0] += data!!.amount.toFloat()
                    val format: DateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                    var date: Date? = null
                    try {
                        date = format.parse(data.date)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    DateWiseIncome[date] = DateWiseIncome.getOrDefault(date, 0) + data.amount
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })


        //Calculate total expense
        mExpenseDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                values[1] = 0F
                for (mysnap in snapshot.children) {
                    val data = mysnap.getValue(
                        Data::class.java
                    )
                    values[1] += data!!.amount.toFloat()
                    val format: DateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
                    var date: Date? = null
                    try {
                        date = format.parse(data.date)
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                    DateWiseExpense[date] = DateWiseExpense.getOrDefault(date, 0) + data.amount
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //Pie Chart
        val pieChart = myView.findViewById<PieChart>(R.id.piechart)
        val data = ArrayList<PieEntry>()
        for (i in values.indices) {
            data.add(PieEntry(values[i], type[i]))
        }
        val colorClassArray = intArrayOf(-0x996700, -0x340000)
        val pieDataSet = PieDataSet(data, "")
        pieDataSet.setColors(*colorClassArray)
        val pieData = PieData(pieDataSet)
        pieData.setValueTextSize(25f)
        pieChart.data = pieData
        pieChart.animateXY(2000, 2000)
        pieChart.isDrawHoleEnabled = false
        val l = pieChart.legend
        l.textSize = 18f
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.textColor = Color.CYAN
        l.isEnabled = true
        pieChart.description.isEnabled = false
        pieChart.invalidate()


        //Line Chart 1
        var dataSets = ArrayList<ILineDataSet?>()
        val xAxisValues = arrayOfNulls<String>(DateWiseIncome.size)
        val incomeEntries = ArrayList<Entry>()
        var i = 0
        for ((key, value) in DateWiseIncome) {
            val formatter: Format = SimpleDateFormat("MMM d, yyyy")
            val s = formatter.format(key)
            xAxisValues[i] = s
            incomeEntries.add(Entry(i.toFloat(), value.toFloat()))
            i++
            Log.i("DATE", xAxisValues[i - 1]!!)
            Log.i("AMOUNT", value.toString())
        }
        dataSets = ArrayList()
        val set1: LineDataSet
        set1 = LineDataSet(incomeEntries, "Income")
        set1.color = -0x996700
        set1.valueTextColor = Color.CYAN
        set1.valueTextSize = 15f
        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets.add(set1)
        val mLineGraph = myView.findViewById<LineChart>(R.id.linechart)
        val rightYAxis = mLineGraph.axisRight
        rightYAxis.textColor = Color.BLUE
        val leftYAxis = mLineGraph.axisLeft
        leftYAxis.isEnabled = true
        leftYAxis.textColor = Color.BLUE
        val topXAxis = mLineGraph.xAxis
        topXAxis.isEnabled = true
        topXAxis.position = XAxis.XAxisPosition.BOTTOM
        set1.lineWidth = 4f
        set1.circleRadius = 3f
        val xAxis = mLineGraph.xAxis
        xAxis.textColor = Color.BLUE
        mLineGraph.xAxis.labelCount = DateWiseIncome.size
        mLineGraph.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues)
        val data2 = LineData(dataSets)
        mLineGraph.data = data2
        mLineGraph.animateX(3000)
        mLineGraph.legend.isEnabled = false
        mLineGraph.invalidate()
        mLineGraph.description.isEnabled = false
        var dataSets1 = ArrayList<ILineDataSet?>()
        val xAxisValues1 = arrayOfNulls<String>(DateWiseExpense.size)
        val expenseEntries = ArrayList<Entry>()
        var j = 0
        for ((key, value) in DateWiseExpense) {
            val formatter: Format = SimpleDateFormat("MMM d, yyyy")
            val s = formatter.format(key)
            xAxisValues1[j] = s
            expenseEntries.add(Entry(j.toFloat(), value.toFloat()))
            j++
            Log.i("DATE", xAxisValues1[j - 1]!!)
            Log.i("AMOUNT", value.toString())
        }
        dataSets1 = ArrayList()
        val set2: LineDataSet
        set2 = LineDataSet(expenseEntries, "Expense")
        set2.color = -0x340000
        set2.valueTextColor = Color.CYAN
        set2.valueTextSize = 15f
        set2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSets1.add(set2)
        val mLineGraph1 = myView.findViewById<LineChart>(R.id.lineChart1)
        val rightYAxis1 = mLineGraph1.axisRight
        rightYAxis1.textColor = Color.BLUE
        val leftYAxis1 = mLineGraph1.axisLeft
        leftYAxis1.isEnabled = true
        leftYAxis1.textColor = Color.BLUE
        val topXAxis1 = mLineGraph1.xAxis
        topXAxis1.isEnabled = true
        topXAxis1.position = XAxis.XAxisPosition.BOTTOM
        set2.lineWidth = 4f
        set2.circleRadius = 3f
        val xAxis1 = mLineGraph1.xAxis
        xAxis1.textColor = Color.BLUE
        mLineGraph1.xAxis.labelCount = DateWiseExpense.size
        mLineGraph1.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValues1)
        val data3 = LineData(dataSets1)
        mLineGraph1.data = data3
        mLineGraph1.animateX(3000)
        mLineGraph1.legend.isEnabled = false
        mLineGraph1.invalidate()
        mLineGraph1.description.isEnabled = false
        return myView
    }

    companion object {
        private val DateWiseIncomeSorter: Set<Pair<Int, Int>> = HashSet()
    }
}