package com.example.ptpn.ui

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.example.ptpn.R
import com.example.ptpn.Utils.decodeBitmap
import com.example.ptpn.databinding.ActivityMainBinding
import com.example.ptpn.model.DeliveryOrder
import com.example.ptpn.model.SPTA
import com.example.ptpn.utils.COLLECTIONS
import com.example.ptpn.utils.DYNAMIC_LINKS
import com.example.ptpn.utils.gone
import com.example.ptpn.utils.visible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var binding: ActivityMainBinding
    val bundle = Bundle()
//
//    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            encodeQr(binding.etQr.text.toString())
//        }else{
//            Toast.makeText(this, "denied bluetooth permission", Toast.LENGTH_LONG).show()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_PTPN)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        binding.btnPrint.setOnClickListener {
//            requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
//        }
        auth = Firebase.auth
        db = Firebase.firestore

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    deepLink?.let { uri ->
                        val path = uri.path.toString()
                        if(path.contains(DYNAMIC_LINKS.DeliveryOrder.name)){
                            val parameter = pendingDynamicLinkData.link?.getQueryParameter("PARAMETER").orEmpty().split("/").toTypedArray()

                            db.collection(COLLECTIONS.SPTA.name).document(parameter[0])
                                .get()
                                .addOnSuccessListener { spta ->
                                    val dataSpta = spta.toObject(SPTA::class.java)?.withId<SPTA>(spta.id)
                                    db.collection(COLLECTIONS.SPTA.name).document(parameter[0]).collection(COLLECTIONS.DELIVERY_ORDER.name).document(parameter[1])
                                        .get()
                                        .addOnSuccessListener { deliveryOrder ->
                                            val currentUser = auth.currentUser
                                            if(currentUser != null && currentUser.isEmailVerified) {
                                                val dataDeliveryOrder = deliveryOrder.toObject(DeliveryOrder::class.java)?.withId<DeliveryOrder>(deliveryOrder.id)
                                                bundle.putParcelable("spta", dataSpta)
                                                bundle.putParcelable("deliveryOrder", dataDeliveryOrder)
                                                bundle.putBoolean("dynamicLink", true)
                                                val navHostFragment = Navigation.findNavController(this, R.id.nav_host_fragment)
                                                val inflater = navHostFragment.navInflater
                                                val graph = inflater.inflate(R.navigation.nav_main)
                                                graph.setStartDestination(R.id.setDeliveryOrderFragment)
                                                navHostFragment.setGraph(graph, bundle)
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.w("ReadDO", "Error getting delivery order.", exception)
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    Log.w("ReadDO", "Error getting spta.", exception)
                                }

                        }
                    }
                }
            }
            .addOnFailureListener(this) { e -> Log.w("MainActivity", "getDynamicLink:onFailure", e) }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if(currentUser != null && currentUser.isEmailVerified){
            val navHostFragment = Navigation.findNavController(this, R.id.nav_host_fragment)
            val inflater = navHostFragment.navInflater
            val graph = inflater.inflate(R.navigation.nav_main)
            graph.setStartDestination(R.id.sptaFragment)
            navHostFragment.setGraph(graph, bundle)
        }
    }

//    private fun encodeQr(stringQr: String){
//        try {
//            val bm: Bitmap? = encodeAsBitmap(stringQr)
//            bm?.let {
//                binding.imgQr.setImageBitmap(bm)
//                printData(bm)
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }

    private fun encodeAsBitmap(str: String): Bitmap? {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 2000, 2000)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }

    private fun printData(bitmap: Bitmap) {
        try {
            val connection: BluetoothConnection? = BluetoothPrintersConnections.selectFirstPaired()
            if(connection != null){

                val printer = EscPosPrinter(connection, 180, 58f, 384)
                val command = decodeBitmap(bitmap)
                printer.printFormattedText(command.toString())

                //val text = "[C]<img>"+ PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap) +"</img>\n"
                //printer.printFormattedText(text)
            } else {
                Toast.makeText(this, "No printer was connected!", Toast.LENGTH_SHORT).show();
            }
        } catch (e: Exception) {
            Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}