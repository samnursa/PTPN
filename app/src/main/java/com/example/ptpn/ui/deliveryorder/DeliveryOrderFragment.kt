package com.example.ptpn.ui.deliveryorder

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ptpn.R
import com.example.ptpn.databinding.FragmentDeliveryOrderBinding
import com.example.ptpn.model.DeliveryOrder
import com.example.ptpn.utils.COLLECTIONS
import com.example.ptpn.utils.gone
import com.example.ptpn.utils.visible
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

        args.spta?.apply {
            id?.let { readDataSPTA(it) }
        }

        action()
    }

    private fun FragmentDeliveryOrderBinding.action(){
        icBack.setOnClickListener {
            navController.navigateUp()
        }

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

    private fun readDataSPTA(id: String){
        shimmer.visible()
        shimmer.startShimmer()
        db.collection(COLLECTIONS.SPTA.name).document(id).collection(COLLECTIONS.DELIVERY_ORDER.name)
            .get()
            .addOnSuccessListener { result ->
                val listDeliveryOrder = mutableListOf<DeliveryOrder>()
                if(result.size() == 0) binding.emptyPage.root.visible() else binding.emptyPage.root.gone()
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