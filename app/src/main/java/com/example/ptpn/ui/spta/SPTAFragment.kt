package com.example.ptpn.ui.spta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ptpn.R
import com.example.ptpn.databinding.FragmentSptaBinding
import com.example.ptpn.model.DeliveryOrder
import com.example.ptpn.model.SPTA
import com.example.ptpn.utils.COLLECTIONS
import com.example.ptpn.utils.DialogUtils
import com.example.ptpn.utils.gone
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SPTAFragment : Fragment(), DialogUtils {

    private lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var binding: FragmentSptaBinding
    private val navController by lazy { findNavController() }
    private lateinit var sptaAdapter: SPTAAdapter
    private lateinit var shimmer: ShimmerFrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSptaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind()
    }

    private fun FragmentSptaBinding.bind(){
        auth = Firebase.auth
        db = Firebase.firestore
        shimmer = shimmerViewContainer

        imgLogout.setOnClickListener {
            requireContext().regularDialog(
                image = R.drawable.img_logout,
                title = getString(R.string.title_dialog_exit),
                desc = getString(R.string.desc_dialog_exit),
                buttonNo = Pair(getString(R.string.lbl_btn_no)){},
                buttonYes = Pair(getString(R.string.lbl_btn_yes)){
                    auth.signOut()
                    navController.navigate(R.id.action_sptaFragment_to_loginFragment)
                }
            )
        }

        action()
    }

    private fun FragmentSptaBinding.action(){
        rvSpta.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        sptaAdapter = SPTAAdapter { SPTA ->
            val bundle = bundleOf("spta" to SPTA)
            navController.navigate(R.id.action_sptaFragment_to_deliveryOrderFragment, bundle)
        }
        rvSpta.adapter = sptaAdapter
        readDataSPTA()
        //writeData()
    }

    fun readDataSPTA(){
        shimmer.startShimmer()
        db.collection(COLLECTIONS.SPTA.name)
            .get()
            .addOnSuccessListener { result ->
                val listSpta = mutableListOf<SPTA>()
                result.forEach { spta ->
                    listSpta.add(spta.toObject(SPTA::class.java).withId(spta.id))
                }
                sptaAdapter.setSptaMethod(listSpta)
                shimmer.apply {
                    stopShimmer()
                    gone()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ReadDO", "Error getting documents.", exception)
            }
    }

    fun readData(){
        db.collection(COLLECTIONS.SPTA.name).document("LA").collection(COLLECTIONS.DELIVERY_ORDER.name)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("ReadDO", "${document.id} => ${document.data}")
                }
                sptaAdapter.setSptaMethod(result.toObjects(SPTA::class.java))
            }
            .addOnFailureListener { exception ->
                Log.w("ReadDO", "Error getting documents.", exception)
            }
    }

    fun searchData(){
        db.collection(COLLECTIONS.SPTA.name)
            .whereEqualTo("name", "los angeles")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("ReadDO", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ReadDO", "Error getting documents.", exception)
            }
    }

    fun writeData(){
        val spta = SPTA(
            tanggal = "07 OCT 2022",
            no_petak = "7TK1221310890",
            afdeling = "AFD14",
            kategori = "TZ-KS",
            deskripsi = "MATAHARI WAL TZ AFD14 AZAM PC 10B 1310890",
            pta = "PTA TZ",
            expired = "2022-10-08 05:59:59"
        )

        // Add a new document with a generated ID
        db.collection(COLLECTIONS.SPTA.name)
            .add(spta)
            .addOnSuccessListener { documentReference ->
                //Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                //Log.w(TAG, "Error adding document", e)
            }
    }
}