package com.aslilokal.mitra.ui.analitik.pemasukan

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.mitra.databinding.FragmentPemasukanBinding
import com.aslilokal.mitra.model.data.api.ApiHelper
import com.aslilokal.mitra.model.data.api.RetrofitInstance
import com.aslilokal.mitra.model.remote.response.ResultOrder
import com.aslilokal.mitra.ui.adapter.LatestOrderAdapter
import com.aslilokal.mitra.ui.analitik.AnalitikViewModel
import com.aslilokal.mitra.utils.CustomFunction
import com.aslilokal.mitra.utils.KodelapoDataStore
import com.aslilokal.mitra.utils.ResourcePagination
import com.aslilokal.mitra.utils.Status
import com.aslilokal.mitra.viewmodel.KodelapoViewModelProviderFactory
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.util.ChartUtils
import java.util.*
import kotlin.collections.ArrayList


class PemasukanFragment : Fragment() {
    private var _binding: FragmentPemasukanBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AnalitikViewModel
    private lateinit var datastore: KodelapoDataStore
    private lateinit var latestOrderAdapter: LatestOrderAdapter

    private lateinit var username: String
    private lateinit var token: String
    private lateinit var currentYear: String
    private lateinit var currentMonth: String


    // Chart
    private lateinit var data: LineChartData
    private var maxValueOfChart = 0
    private var maxTopValueOfChart = 0f
    private var shape = ValueShape.CIRCLE

    private val mPointValues: ArrayList<PointValue> = ArrayList()
    private val mAxisXValues: ArrayList<AxisValue> = ArrayList()

    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPemasukanBinding.inflate(inflater, container, false)
        datastore = KodelapoDataStore(binding.root.context)

        showProgressBar()

        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()

            currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
            currentMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString()
        }

        setupViewModel()
        setupRecycler()


        showSkeleton()
        getData()
        setupGetTotalRevenue()

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            getData()
            setupGetTotalRevenue()
        }


        var finalDate =
            CustomFunction().formateMonthAndYear("$currentMonth/$currentYear")
        binding.txtMonthYear.text = finalDate

        binding.birthDate.setOnClickListener {
            getDate()
        }


        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            KodelapoViewModelProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AnalitikViewModel::class.java)
    }

    private fun setupRecycler() {
        latestOrderAdapter = LatestOrderAdapter()
        binding.rvLatestTransaction.apply {
            adapter = latestOrderAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun getDate() {
        val today = Calendar.getInstance()
        val builder =
            MonthPickerDialog.Builder(
                binding.root.context, { selectedMonth, selectedYear ->
                    var finalMonth = selectedMonth + 1
                    var finalDate =
                        CustomFunction().formateMonthAndYear("$finalMonth/$selectedYear")
                    binding.txtMonthYear.text = finalDate
                    // do call api
                    currentMonth = finalMonth.toString()
                    currentYear = selectedYear.toString()

                    getData()
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH)
            )
        builder.setMinYear(2020)
            .setActivatedYear(currentYear.toInt())
            .setMaxYear(currentYear.toInt())
            .build()
            .show()
    }

    private fun generateData(listData: ArrayList<ResultOrder>) {
        // test data
        getAxisXLables(listData)
        getAxisPoints(listData)
        //Toast.makeText(activity, maxTopValueOfChart.toString(), Toast.LENGTH_SHORT).show()

        val lines: MutableList<Line> = ArrayList()

        val line = Line(mPointValues)

        line.color = ChartUtils.COLORS[0]
        line.shape = shape
        line.isCubic = true
        line.isFilled = true
        line.setHasLabels(false)
        line.setHasLabelsOnlyForSelected(true)
        line.setHasLines(true)
        line.setHasPoints(true)
        line.pointColor = ChartUtils.COLORS[(0 + 1) % ChartUtils.COLORS.size]
        lines.add(line)

        data = LineChartData(lines)
//        data.axisXBottom = null
//        data.axisYLeft = null
        data.lines = lines

        //Setup data Axis
        val axisX = Axis()
        axisX.setHasTiltedLabels(true)
        axisX.textColor = Color.parseColor("#D6D6D9")
        axisX.textSize = 10
        axisX.values = mAxisXValues
        data.axisXBottom = axisX
        axisX.setHasLines(true)

        val axisY = Axis().setHasLines(true)
        axisY.name = "Rupiah"
        axisY.textSize = 8
        data.axisYLeft = axisY
        data.baseValue = Float.NEGATIVE_INFINITY;
        binding.chartStatistikHistory.lineChartData = data
        resetViewport()
        binding.chartStatistikHistory.isViewportCalculationEnabled = false
    }

    private fun resetViewport() {
        binding.chartStatistikHistory.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL
        binding.chartStatistikHistory.visibility = View.VISIBLE

        val v = Viewport(binding.chartStatistikHistory.maximumViewport)
        v.bottom = 0f
        v.top = maxTopValueOfChart
        v.left = 0f
        v.right = maxValueOfChart.toFloat()
//        binding.chartStatistikHistory.maximumViewport = v
        binding.chartStatistikHistory.currentViewport = v
    }

    private fun getData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllLatestTransaction(
                token,
                username,
                currentMonth.toInt(),
                currentYear.toInt()
            )
            setupObserver()
        }
    }

    private fun setupObserver() {
        viewModel.latestOrderFinish.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ResourcePagination.Success -> {
                    hideProgressBar()
                    response.data?.result.let { orderResponse ->
                        latestOrderAdapter.differ.submitList(orderResponse?.toList())
                        if (orderResponse != null) {
                            if (orderResponse.toList().isEmpty()) {
                                Toast.makeText(
                                    activity,
                                    "Sepertinya datanya masih kosong...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.d("GENERATEDATA", "Counting")
                                mPointValues.clear()
                                mAxisXValues.clear()
                                generateData(orderResponse)
                                maxValueOfChart = orderResponse.size
                            }
                        }
                    }
                }

                is ResourcePagination.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "Sepertinya jaringanmu lemah, coba refresh...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is ResourcePagination.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun setupGetTotalRevenue() {
        viewModel.getTotalRevenue(token, username).observe(viewLifecycleOwner, {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        hideSkeleton()
                        resource.data?.body().let { response ->
                            if (response?.result == null) {
                                binding.txtTotalRevenue.text = "Rp 0"
                            } else {
                                binding.txtTotalRevenue.text =
                                    CustomFunction().formatRupiah(response.sumSaldo.toDouble())
                            }
                        }
                    }

                    Status.LOADING -> {
                        showSkeleton()
                    }

                    Status.ERROR -> {
                        binding.txtTotalRevenue.text = "Rp 0"
                    }
                }
            }
        })
    }

    private fun showSkeleton() {
        binding.skeletonLayout.showShimmer(true)
        binding.skeletonLayout.startShimmer()
    }

    private fun hideSkeleton() {
        binding.skeletonLayout.stopShimmer()
        binding.skeletonLayout.hideShimmer()
    }

    private fun showProgressBar() {
        binding.swipeRefresh.isRefreshing = true
        binding.rvSkeletonLayout.showShimmerAdapter()
        binding.rvLatestTransaction.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.swipeRefresh.isRefreshing = false
        binding.rvSkeletonLayout.hideShimmerAdapter()
        binding.rvLatestTransaction.visibility = View.VISIBLE
    }

    private fun getAxisXLables(listData: ArrayList<ResultOrder>) {
        for (i in listData.indices) {
            //change orderAt to finishAt
            var finalDate = CustomFunction().isoTimeToDate(listData[i].orderAt)
            mAxisXValues.add(AxisValue(i.toFloat()).setLabel(finalDate))
        }
    }

    private fun getAxisPoints(listData: ArrayList<ResultOrder>) {
        var listAllPayment = arrayListOf<Float>()
        var sumTotalRevenue = 0
        for (i in listData.indices) {
            sumTotalRevenue += listData[i].totalPayment
            listAllPayment.add(listData[i].totalPayment.toFloat())
            mPointValues.add(PointValue(i.toFloat(), listData[i].totalPayment.toFloat()))
        }
        maxTopValueOfChart = listAllPayment.maxOf { it }
        binding.txtCurrentSumValueMonth.text =
            CustomFunction().formatRupiah(sumTotalRevenue.toDouble())
    }
}