package com.example.ptpn.ui.setdeliveryorder

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.ptpn.R
import com.example.ptpn.customview.Type
import com.example.ptpn.databinding.FragmentSetDeliveryOrderBinding
import com.example.ptpn.model.DeliveryOrder
import com.example.ptpn.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*


class SetDeliveryOrderFragment : Fragment(), DialogUtils {

    private lateinit var binding: FragmentSetDeliveryOrderBinding
    private val args: SetDeliveryOrderFragmentArgs by navArgs()
    private val navController by lazy { findNavController() }
    lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetDeliveryOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bind()
    }

    private fun FragmentSetDeliveryOrderBinding.bind(){
        db = Firebase.firestore
        args.apply{
            spta?.let { spta ->
                containerSpta.apply {
                    tvNoPetak.text = spta.no_petak
                    tvDate.text = spta.tanggal
                    tvExpired.text = spta.expired
                    tvAfdeling.text = spta.afdeling
                    tvKategori.text = spta.kategori
                    tvPta.text = spta.pta
                    tvDeskripsi.text = spta.deskripsi
                }
            }

            deliveryOrder?.let {
                calendar.editText?.setText(it.tgl_tebang)
                haTertebang.editText?.setText(it.ha_tertebang.toString())
                brix.editText?.setText(it.brix.toString())
                ph.editText?.setText(it.ph.toString())
                name.editText?.setText(it.sopir.toString())
                noLori.editText?.setText(it.no_lori.toString())
                noTruk.editText?.setText(it.no_truk.toString())
                if(it.terbakar == true) rdTerbakar.check(R.id.rdButtonYes) else rdTerbakar.check(R.id.rdButtonNo)
            }

            if(dynamicLink){
                icBack.gone()
                btnCetak.invisible()
                btnSimpan.invisible()
                haTertebang.disable()
                brix.disable()
                ph.disable()
                rdButtonNo.disable()
                rdButtonYes.disable()
                name.disable()
                noLori.disable()
                noTruk.disable()
            }
        }

        action()
    }

    private fun FragmentSetDeliveryOrderBinding.action(){
        icBack.setOnClickListener {
            when (navController.graph.startDestinationId) {
                navController.currentDestination?.id -> requireActivity().finish()
                else -> navController.navigateUp()
            }
        }

        calendar.setStartIconOnClickListener {
            calendar.editText?.let { it1 -> showDateTimePicker(it1) }
        }

        btnSimpan.setOnClickListener {
            val deliveryOrder = DeliveryOrder(
                ha_tertebang = haTertebang.editText?.text.toString().toInt(),
                tgl_tebang = calendar.editText?.text.toString(),
                no_lori = noLori.editText?.text.toString(),
                no_truk = noTruk.editText?.text.toString(),
                sopir = name.editText?.text.toString(),
                brix = brix.editText?.text.toString().toInt(),
                ph = ph.editText?.text.toString().toInt(),
                terbakar = !rdButtonNo.isChecked
            )

            args.spta?.id?.let { idSpta ->
                val idDo = args.deliveryOrder?.id ?: "PTPN${idSpta}DO${Calendar.getInstance().timeInMillis}"
                updateData(idSpta, idDo , deliveryOrder)
            }
        }

        btnCetak.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            }
            else{
                requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }

        }
    }

    private fun showDateTimePicker(calendarView: EditText) {
        val currentDate = Calendar.getInstance()
        val date = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                TimePickerDialog(requireContext(),
                    { _, hourOfDay, minute ->
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        date.set(Calendar.MINUTE, minute)
                        calendarView.setText(SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault()).format(date.timeInMillis))
                    },
                    currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE),
                    true
                ).show()
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DATE)
        ).show()
    }

    private fun updateData(
        idSpta: String,
        idDO: String,
        deliveryOrder: DeliveryOrder
    ){
        db.collection(COLLECTIONS.SPTA.name)
            .document(idSpta)
            .collection(COLLECTIONS.DELIVERY_ORDER.name)
            .document(idDO)
            .set(deliveryOrder)
            .addOnSuccessListener {
                requireContext().toast(getString(R.string.success_update_do))
            }
            .addOnFailureListener { exception ->
                requireContext().toast(getString(R.string.failed_update_do), ToastState.ERROR)
            }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var isPermited = true
            permissions.entries.forEach {
                if(!it.value) isPermited = false
            }

            if(isPermited){
                printData()
            }else{
                requireContext().toast(getString(R.string.access_bluetooth_denied), ToastState.ERROR)
            }
        }

    private val requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //encodeQr(binding.etQr.text.toString())
            printData()
        }else{
            requireContext().toast(getString(R.string.access_bluetooth_denied), ToastState.ERROR)
        }
    }

//    private fun encodeQr(stringQr: String){
//        try {
//            val bm: Bitmap? = encodeAsBitmap(stringQr)
//            bm?.let {
//                //binding.imgQr.setImageBitmap(bm)
//                printData(bm)
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }

//    private fun printData(bitmap: Bitmap) {
    private fun printData() {
        binding.btnCetak.type(Type.LOADING.value)
        try {
            val connection: BluetoothConnection? = BluetoothPrintersConnections.selectFirstPaired()
            if(connection != null){
                val printer = EscPosPrinter(connection, 203, 48f, 32)
                val idDo = args.deliveryOrder?.id ?: "PTPN${args.spta?.id}DO${Calendar.getInstance().timeInMillis}"
                val text = "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer, ContextCompat.getDrawable(requireActivity(), R.drawable.ic_logo_black)) +"</img>\n\n"+
                        "[C]================================\n" +
                        "[C]<font size='big'>${args.spta?.no_petak}</font>\n\n" +
                        "[L]Tanggal : ${args.spta?.tanggal}\n" +
                        "[L]Expired : ${args.spta?.expired}\n" +
                        "[C]================================\n" +
                        "[L]Afdeling : ${args.spta?.afdeling}\n" +
                        "[L]Kategori : ${args.spta?.kategori}\n" +
                        "[L]PTA : ${args.spta?.pta}\n" +
                        "[C]--------------------------------\n" +
                        "[L]Deskripsi :\n${args.spta?.deskripsi}\n" +
                        "[C]================================\n" +
                        "[C]<qrcode size='40'>https://ptpn.page.link/?link=https://ptpn.page.link/DeliveryOrder?PARAMETER=${args.spta?.id}/$idDo&apn=com.example.ptpn&efr=1</qrcode>"
                printer.printFormattedText(text)
                binding.btnCetak.type(Type.ENABLED.value)
            } else {
                requireContext().regularDialog(
                    image = R.drawable.ic_bluetooth,
                    title = getString(R.string.title_dialog_error_connect_print),
                    desc = getString(R.string.desc_dialog_error_connect_print),
                    buttonYes = Pair(getString(R.string.lbl_btn_yes)){}
                )
                binding.btnCetak.type(Type.ENABLED.value)
            }
        } catch (e: Exception) {
            requireContext().regularDialog(
                image = R.drawable.ic_bluetooth,
                title = getString(R.string.title_dialog_error_connect_print),
                desc = getString(R.string.desc_dialog_error_connect_print),
                buttonYes = Pair(getString(R.string.lbl_btn_yes)){}
            )
            binding.btnCetak.type(Type.ENABLED.value)
        }
    }
}