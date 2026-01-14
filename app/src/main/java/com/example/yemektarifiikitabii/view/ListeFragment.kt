package com.example.yemektarifiikitabii.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.yemektarifiikitabii.ListeFragmentDirections
import com.example.yemektarifiikitabii.databinding.FragmentListeBinding
import com.example.yemektarifiikitabii.roomdb.TarifDAO
import com.example.yemektarifiikitabii.roomdb.TarifDatabase
import kotlinx.serialization.encoding.CompositeDecoder

class ListeFragment : Fragment() {
    private var _binding: FragmentListeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDAO
    private val mDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
        binding.floatingActionButton.setOnClickListener { yeniEkle(it) }
        binding.yemekRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        verileriAl()
    }
    private fun verileriAl() {
        mDisposable.add(tarifDao.getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleResponse))
    }
    fun yeniEkle (view: View){
        val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "yeni", id = -1)
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}