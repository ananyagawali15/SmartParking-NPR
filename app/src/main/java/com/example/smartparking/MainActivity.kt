package com.example.smartparking


import android.os.Bundle
import kotlinx.coroutines.delay
import android.app.Activity
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import android.graphics.Bitmap
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

import java.io.File
import java.io.InputStream

// 🔥 Coroutines
import kotlinx.coroutines.launch

// 🔥 Retrofit / OkHttp
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

// 🔥 Activity Result (Camera + Gallery)
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// 🔥 Compose Core
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.*

// 🔥 UI + Layout
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

// 🔥 Material 3
import androidx.compose.material3.*

// 🔥 Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// 🔥 Graphics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// 🔥 Text + Units
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🔥 Image loading (VERY IMPORTANT)
import coil.compose.rememberAsyncImagePainter

class MainActivity : ComponentActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }
}
val DarkBlue = Color(0xFF0A1628)
val CardBlue = Color(0xFF122B4A)
val ElectricBlue = Color(0xFF2979FF)

//////////////////////////////////////////////////////
// APP CONTROLLER
//////////////////////////////////////////////////////

@Composable
fun App() {

    var screen by remember { mutableStateOf("login") }
    var selectedVehicle by remember { mutableStateOf("Car") }
    var slot by remember { mutableStateOf("None") }
    var detectedPlate by remember { mutableStateOf("") }

    // ✅ ADD THESE
    var activeSlot by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<Long?>(null) }

    val context = LocalContext.current

    when (screen) {

        "login" -> LoginScreen(
            onLogin = { screen = "home" },
            onSignup = { screen = "signup" }
        )

        "signup" -> SignupScreen {
            screen = "login"
        }

        // ✅ PASS DATA HERE
        "home" -> HomeScreen(
            onScan = { screen = "scan" },
            onBook = { screen = "booking" },
            onPay = { screen = "payment" },
            activeSlot = activeSlot,
            startTime = startTime
        )

        "scan" -> ScanScreen(
            onBack = { screen = "home" },
            onPlateDetected = { detectedPlate = it }
        )

        "booking" -> {
            BookingScreen(
                selectedVehicle = selectedVehicle,
                onVehicleChange = { selectedVehicle = it },
                plateNumber = detectedPlate,
                onBack = { screen = "home" },

                onConfirm = { selectedSlot ->

                    if (selectedSlot.isNotEmpty()) {

                        slot = selectedSlot

                        // ✅ SET TIMER DATA HERE
                        activeSlot = selectedSlot
                        startTime = System.currentTimeMillis()

                        // 🚀 MAP NAVIGATION
                        val uri = Uri.parse(
                            "google.navigation:q=18.5204,73.8567(Parking $selectedSlot)"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")

                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        screen = "payment"
                    }
                }
            )
        }

        "payment" -> PaymentScreen(
            slot = slot,
            plateNumber = detectedPlate,
            onDone = { screen = "home" }
        )
    }
}

//////////////////////////////////////////////////////
// LOGIN
//////////////////////////////////////////////////////

@Composable
fun LoginScreen(onLogin: () -> Unit, onSignup: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("ParkEase", fontSize = 30.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Text(
            "Create Account",
            color = ElectricBlue,
            modifier = Modifier
                .padding(top = 10.dp)
                .clickable { onSignup() }
        )
    }
}

//////////////////////////////////////////////////////
// SIGNUP
//////////////////////////////////////////////////////

@Composable
fun SignupScreen(onDone: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Signup", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField("", {}, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField("", {}, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField("", {}, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(20.dp))

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Register")
        }
    }
}

//////////////////////////////////////////////////////
// HOME
//////////////////////////////////////////////////////

@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onBook: () -> Unit,
    onPay: () -> Unit,
    activeSlot: String,
    startTime: Long?
) {

    val context = LocalContext.current

    // ⏱ Timer state
    var timeLeft by remember { mutableStateOf(0L) }

    // 🔥 TIMER LOGIC (1 hour = 3600000 ms)
    LaunchedEffect(startTime) {
        while (startTime != null) {
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = 3600000 - elapsed

            timeLeft = if (remaining > 0) remaining else 0

            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(20.dp)
    ) {

        Text(
            "Welcome 👋",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        // ✅ SHOW TIMER ONLY IF SLOT ACTIVE
        if (activeSlot.isNotEmpty() && timeLeft > 0) {

            val minutes = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElectricBlue)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "🚗 Active Slot: $activeSlot",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "⏱ Time Remaining: ${minutes}m ${seconds}s",
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        HomeCard("Scan Number Plate", Icons.Default.CameraAlt, onScan)
        HomeCard("Slot Booking", Icons.Default.LocalParking, onBook)
        HomeCard("Payment", Icons.Default.Payment, onPay)

        HomeCard(
            title = "Navigate to Parking",
            icon = Icons.Default.LocationOn
        ) {
            val uri = Uri.parse("google.navigation:q=18.5204,73.8567")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
@Composable
fun HomeCard(title: String, icon: ImageVector, onClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBlue),
        shape = RoundedCornerShape(16.dp)
    )

    {

        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(icon, contentDescription = null, tint = ElectricBlue)

            Spacer(Modifier.width(20.dp))

            Text(title, color = Color.White, fontSize = 18.sp)
        }
    }
}

//////////////////////////////////////////////////////
//  NUmber plate SCAN
//////////////////////////////////////////////////////

@Composable
fun ScanScreen(onBack: () -> Unit, onPlateDetected: (String) -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var resultText by remember { mutableStateOf("") }

    // 📸 CAMERA
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->

        bitmap?.let { bmp ->
            val file = File(context.cacheDir, "captured.jpg")

            file.outputStream().use { stream ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }

            imageUri = Uri.fromFile(file)
        }
    }

    // 🖼 GALLERY
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔙 BACK
        Text(
            "← Back",
            color = ElectricBlue,
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { onBack() }
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Scan Number Plate",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(30.dp))

        // 🖼 IMAGE PREVIEW
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .size(220.dp)
                    .padding(10.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // 📸 CAMERA BUTTON
        Button(
            onClick = { cameraLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Open Webcam")
        }

        Spacer(Modifier.height(12.dp))

        // 🖼 UPLOAD BUTTON
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Upload Image")
        }

        Spacer(Modifier.height(30.dp))

        // 🔍 DETECT BUTTON
        Button(
            onClick = {
                imageUri?.let { uri ->

                    scope.launch {
                        try {
                            val file = uriToFile(context, uri)

                            val requestFile =
                                file.asRequestBody("image/*".toMediaTypeOrNull())

                            val body = MultipartBody.Part.createFormData(
                                "image",
                                file.name,
                                requestFile
                            )

                            val response =
                                RetrofitClient.api.detectPlate(body)

                            if (response.isSuccessful) {
                                val plate = response.body()?.plateNumber ?: "No Plate Found"
                                resultText = plate

                                if (plate != "No Plate Found") {
                                    onPlateDetected(plate)   // ✅ SEND TO APP
                                }
                            } else {
                                resultText = "API Error"
                            }

                        } catch (e: Exception) {
                            resultText = "Error: ${e.message}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            Text("Detect Number Plate")
        }

        Spacer(Modifier.height(20.dp))

        // 📄 RESULT
        Text(
            text = resultText,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "upload.jpg")

    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file
}

//////////////////////////////////////////////////////
// BOOKING
//////////////////////////////////////////////////////

@Composable
fun BookingScreen(
    selectedVehicle: String,
    onVehicleChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    plateNumber: String,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vehicles = listOf("Car", "Bike", "Truck")

    // 🎯 Slot mapping
    val slotMap = mapOf(
        "Car" to listOf("A1","A2","A3","A4","C1","C2"),
        "Bike" to listOf("B1","B2","B3","B4","C3"),
        "Truck" to listOf("A1","A4","C2")
    )

    val slots = slotMap[selectedVehicle] ?: emptyList()

    var occupiedSlots by remember { mutableStateOf(listOf<String>()) }
    var selectedSlot by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // 🔄 Reset on vehicle change
    LaunchedEffect(selectedVehicle) {
        selectedSlot = ""
    }

    // 🔥 Auto fetch from backend
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val response = RetrofitClient.api.getSlots()

                occupiedSlots = response.filter {
                    it.value.status == "occupied"
                }.keys.toList()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            kotlinx.coroutines.delay(5000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(20.dp)
    ) {

        // ✅ 🔙 BACK BUTTON (TOP)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = ElectricBlue,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBack() }
            )

            Spacer(Modifier.width(10.dp))

            Text(
                "Back",
                color = ElectricBlue,
                fontSize = 16.sp
            )
        }

        // 🔹 VEHICLE TYPE
        Text("Select Vehicle", fontSize = 20.sp, color = Color.White)

        Row {
            vehicles.forEach {
                FilterChip(
                    selected = selectedVehicle == it,
                    onClick = { onVehicleChange(it) },
                    label = { Text(it) },
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        // 🔹 NUMBER PLATE
        Text("Vehicle Number", color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBlue)
        ) {
            Text(
                text = if (plateNumber.isNotEmpty())
                    plateNumber
                else
                    "⚠ Please scan vehicle",
                modifier = Modifier.padding(16.dp),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Recommended Slots for $selectedVehicle",
            color = ElectricBlue,
            fontSize = 18.sp
        )

        // 🔹 GROUPED SLOTS
        val groupedSlots = slots.groupBy { it.first() }

        groupedSlots.forEach { (row, rowSlots) ->

            Text(
                text = "Row $row",
                color = ElectricBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )

            rowSlots.forEach { slot ->

                val isOccupied = slot in occupiedSlots
                val isSelected = selectedSlot == slot

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .clickable {
                            if (isOccupied) {
                                Toast.makeText(
                                    context,
                                    "Slot already occupied",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@clickable
                            }
                            selectedSlot = slot
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isOccupied -> Color.Red
                            isSelected -> ElectricBlue
                            else -> CardBlue
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isOccupied -> "$slot (Occupied)"
                            isSelected -> "$slot (Selected)"
                            else -> "$slot (Available)"
                        },
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 🔹 CONFIRM BUTTON
        Button(
            onClick = {

                isLoading = true

                scope.launch {
                    try {
                        val response = RetrofitClient.api.bookSlot(
                            BookRequest(
                                slot = selectedSlot,
                                vehicle = plateNumber
                            )
                        )

                        if (response.isSuccessful) {

                            // 🔥 instant UI update
                            occupiedSlots = occupiedSlots + selectedSlot

                            Toast.makeText(
                                context,
                                "✅ Slot Booked Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            onConfirm(selectedSlot)

                        } else {
                            Toast.makeText(
                                context,
                                "❌ Slot already occupied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    isLoading = false
                }
            },
            enabled = !isLoading &&
                    selectedSlot.isNotEmpty() &&
                    selectedSlot !in occupiedSlots &&
                    plateNumber.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Booking..." else "Confirm Booking")
        }
    }
}

//////////////////////////////////////////////////////
// PAYMENT
//////////////////////////////////////////////////////

@Composable
fun PaymentScreen(
    slot: String,
    plateNumber: String,
    onDone: () -> Unit
) {

    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // 🔹 HEADER
        Text(
            "Secure Payment",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        // 🔹 PAYMENT CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBlue)
        ) {

            Column(modifier = Modifier.padding(20.dp)) {

                Text("Vehicle", color = Color.Gray)

                Text(
                    plateNumber.ifEmpty { "Not Detected" },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Text("Parking Slot", color = Color.Gray)

                Text(
                    slot,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                Divider(color = Color.Gray)

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Amount", color = Color.Gray)

                    Text(
                        "₹30",
                        color = ElectricBlue,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    "Includes GST",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 🔹 PAY BUTTON
        Button(
            onClick = {

                val checkout = Checkout()
                checkout.setKeyID("rzp_test_SkTHMkeHykHZFI")

                try {
                    val options = JSONObject()
                    options.put("name", "Smart Parking")
                    options.put("description", "Slot Booking Payment")
                    options.put("currency", "INR")
                    options.put("amount", 3000)

                    val prefill = JSONObject()
                    prefill.put("email", "test@gmail.com")
                    prefill.put("contact", "9999999999")

                    options.put("prefill", prefill)

                    checkout.open(activity, options)

                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {

            Icon(Icons.Default.Payment, contentDescription = null)
            Spacer(Modifier.width(10.dp))

            Text(
                "Pay Securely",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        // 🔹 EXIT BUTTON (RELEASE SLOT)
        OutlinedButton(
            onClick = {

                scope.launch {
                    try {
                        val response = RetrofitClient.api.releaseSlot(
                            ReleaseRequest(slot)
                        )

                        if (response.isSuccessful) {
                            Toast.makeText(
                                context,
                                "🚗 Slot Released",
                                Toast.LENGTH_SHORT
                            ).show()

                            onDone()

                        } else {
                            Toast.makeText(
                                context,
                                "❌ Release failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exit Parking")
        }
    }
}