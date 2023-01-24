package com.example.ptpn.ui.deliveryorder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ptpn.R
import com.example.ptpn.databinding.FragmentDeliveryOrderBinding
import com.example.ptpn.model.DeliveryOrder
import com.example.ptpn.utils.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DeliveryOrderFragment : Fragment() {

    private lateinit var binding: FragmentDeliveryOrderBinding
    private val args: DeliveryOrderFragmentArgs by navArgs()
    private val navController by lazy { findNavController() }
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var deliveryOrderAdapter: DeliveryOrderAdapter
    lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeliveryOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind()
    }

    private fun FragmentDeliveryOrderBinding.bind(){
        shimmer = shimmerViewContainer
        db = Firebase.firestore
        svDeliveryOrder.onActionViewExpanded()
        svDeliveryOrder.clearFocus()

        args.spta?.apply {
            id?.let { readDataSPTA(it) }
        }

        action()
    }

    private fun FragmentDeliveryOrderBinding.action(){
        icBack.setOnClickListener {
            navController.navigateUp()
        }

        svDeliveryOrder.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isEmpty()){
                    args.spta?.id?.let {
                        readDataSPTA(it)
                    }
                }
                return false
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                args.spta?.id?.let{
                    readDataSPTA(it, query)
                }
                return false
            }
        })

        btnAdd.setOnClickListener {
            val bundle = bundleOf("deliveryOrder" to null, "spta" to args.spta, "dynamicLink" to false)
            navController.navigate(R.id.action_deliveryOrderFragment_to_setDeliveryOrderFragment, bundle)
        }

        rvDeliveryOrder.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        deliveryOrderAdapter = DeliveryOrderAdapter { deliveryOrder ->
            val bundle = bundleOf("deliveryOrder" to deliveryOrder, "spta" to args.spta, "dynamicLink" to false)
            navController.navigate(R.id.action_deliveryOrderFragment_to_setDeliveryOrderFragment, bundle)
        }
        rvDeliveryOrder.adapter = deliveryOrderAdapter
    }

    private fun readDataSPTA(id: String, search: String = ""){
        shimmer.visible()
        shimmer.startShimmer()
        binding.emptyPage.root.gone()
        db.collection(COLLECTIONS.SPTA.name).document(id).collection(COLLECTIONS.DELIVERY_ORDER.name)
            //for better search and index use elastic search or algolia
            .orderBy(SEARCH.sopir.name)
            .startAt(search)
            .endAt(search +'\uf8ff')
            .get()
            .addOnSuccessListener { result ->
                val listDeliveryOrder = mutableListOf<DeliveryOrder>()
                if(result.size() == 0) {
                   binding.apply{
                       emptyPage.root.visible()
                       rvDeliveryOrder.gone()
                   }
                } else {
                    binding.apply {
                        emptyPage.root.gone()
                        rvDeliveryOrder.visible()
                    }
                }
                result.forEach { deliveryOrder ->
                    listDeliveryOrder.add(deliveryOrder.toObject(DeliveryOrder::class.java).withId(deliveryOrder.id))
                }
                deliveryOrderAdapter.setDeliveryOrderMethod(listDeliveryOrder)
                shimmer.apply {
                    stopShimmer()
                    gone()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ReadDO", "Error getting documents.", exception)
            }
    }
}