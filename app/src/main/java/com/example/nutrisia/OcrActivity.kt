package com.example.nutrisia

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Size

class OcrActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        RetrofitClient.instance
    }

    private lateinit var previewView: PreviewView
    private lateinit var tvScanResult: TextView
    private lateinit var tvCalorieResult: TextView
    private lateinit var btnScan: Button
    private lateinit var btnReset: Button
    private lateinit var btnFlashlight: ImageView
    private lateinit var imageViewCaptured: ImageView
    private lateinit var btnSaveCalorie: Button
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var isFlashlightOn = false
    private var currentCalorieInfo: CalorieInfo? = null

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    companion object {
        private const val CALORIE_THRESHOLD = 500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ocr)

        initializeViews()
        initializeCamera()

        btnScan.setOnClickListener { captureImage() }
        btnReset.setOnClickListener { resetResults() }
        btnFlashlight.setOnClickListener { toggleFlashlight(!isFlashlightOn) }
        btnSaveCalorie.setOnClickListener { saveCalorieData() }
    }

    private fun initializeViews() {
        previewView = findViewById(R.id.previewView)
        tvScanResult = findViewById(R.id.tv_scan_result)
        tvCalorieResult = findViewById(R.id.tv_calorie_result)
        btnScan = findViewById(R.id.btn_scan)
        btnReset = findViewById(R.id.btn_reset)
        btnFlashlight = findViewById(R.id.btn_flashlight_icon)
        imageViewCaptured = findViewById(R.id.imageViewCaptured)
        btnSaveCalorie = findViewById(R.id.btnSaveCalorie)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initializeCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionsLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun resetResults() {
        tvScanResult.text = "Hasil Scan"
        tvCalorieResult.text = "Hasil Kalori"
        imageViewCaptured.setImageBitmap(null)
        previewView.visibility = ImageView.VISIBLE
        imageViewCaptured.visibility = ImageView.GONE
        btnSaveCalorie.visibility = Button.GONE
        startCamera()
        toggleFlashlight(false)
        Toast.makeText(this, "Hasil telah direset.", Toast.LENGTH_SHORT).show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Failed to bind camera use cases", exc)
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    processImageForOCR(imageProxy)
                    imageProxy.toBitmap()?.let {
                        imageViewCaptured.setImageBitmap(it)
                        previewView.visibility = ImageView.GONE
                        imageViewCaptured.visibility = ImageView.VISIBLE
                    }
                    toggleFlashlight(false)
                    imageProxy.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Capture", "Image capture failed", exception)
                    Toast.makeText(this@OcrActivity, "Gagal menangkap gambar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun processImageForOCR(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        val processedImage = preprocessImage(image)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(processedImage)
            .addOnSuccessListener { visionText ->
                Log.d("OCR Raw Text", visionText.text) // Log teks mentah
                val filteredText = filterRelevantInfo(visionText.text)
                Log.d("OCR Filtered Text", filteredText) // Log teks setelah pemfilteran
                val calorieInfo = extractCalorieInfo(filteredText)
                updateResultViews(filteredText, calorieInfo)
            }
            .addOnFailureListener {
                Log.e("OCR", "OCR failure", it)
                Toast.makeText(this, "Gagal mengenali teks", Toast.LENGTH_SHORT).show()
            }
    }

    private fun preprocessImage(image: InputImage): InputImage {
        val bitmap = image.bitmapInternal ?: return image
        val enhancedBitmap = enhanceBitmap(bitmap)
        return InputImage.fromBitmap(enhancedBitmap, image.rotationDegrees)
    }

    private fun enhanceBitmap(bitmap: Bitmap): Bitmap {
        val grayscaleBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return grayscaleBitmap
    }

    private fun updateResultViews(filteredText: String, calorieInfo: CalorieInfo) {
        tvScanResult.text = filteredText

        if (calorieInfo.first > 0) {
            tvCalorieResult.text = """
                Energi Total: ${calorieInfo.first} kkal
                Kalori Per Sajian: ${calorieInfo.second} kkal
                Energi Lemak: ${calorieInfo.third} kkal
            """.trimIndent()
        } else {
            tvCalorieResult.text = "Tidak ditemukan informasi kalori"
        }

        currentCalorieInfo = calorieInfo
        btnSaveCalorie.visibility = Button.VISIBLE
    }

    private fun saveCalorieData() {
        val calorieInfo = currentCalorieInfo ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val userId = getUserId()

        if (userId != null) {
            val scanCalorieRequest = ScanCalorieRequest(
                user_id = userId,
                total_calories = calorieInfo.first.toDouble(),
                scan_date = currentDate
            )

            CoroutineScope(Dispatchers.IO).launch {
                sendDataToServer(scanCalorieRequest)
            }
        } else {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun sendDataToServer(scanCalorieRequest: ScanCalorieRequest) {
        try {
            val response: Response<ApiResponse> = apiService.postScanCalorie(scanCalorieRequest)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                apiResponse?.let {
                    if (it.status) {
                        Log.d("OcrActivity", "Data kalori berhasil disimpan")
                        runOnUiThread {
                            Toast.makeText(this, "Data kalori berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("OcrActivity", "Gagal menyimpan data kalori: ${it.message}")
                        runOnUiThread {
                            Toast.makeText(this, "Gagal menyimpan data kalori", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Log.e("OcrActivity", "Gagal menyimpan data kalori: ${response.message()}")
                runOnUiThread {
                    Toast.makeText(this, "Gagal menyimpan data kalori", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("OcrActivity", "Error saat mengirim data: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, "Terjadi kesalahan saat mengirim data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFlashlight(on: Boolean) {
        camera?.cameraControl?.enableTorch(on)?.also {
            isFlashlightOn = on
            btnFlashlight.setImageResource(
                if (on) R.drawable.ic_flashlight_on else R.drawable.ic_flashlight_off
            )
        } ?: Toast.makeText(this, "Flash tidak tersedia.", Toast.LENGTH_SHORT).show()
    }

    private fun filterRelevantInfo(text: String): String {
        val keywords = listOf(
            "kalori", "calorie", "energy", "energi", "lemak", "fat",
            "sugar", "gula", "karbohidrat", "carbohydrate", "total energi",
            "energi total", "energi lemak", "takaran saji", "serving size"
        )
        val relevantLines = text.lines().filter { line ->
            keywords.any { keyword -> line.contains(keyword, ignoreCase = true) }
        }

        return relevantLines.joinToString("\n")
    }

    private fun extractCalorieInfo(text: String): CalorieInfo {
        val totalCalories = Regex(
            "(total energi|energi total|total calories).*?:?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(kkal|cal)?",
            RegexOption.IGNORE_CASE
        ).find(text)?.groupValues?.get(2)?.replace(",", "")?.toIntOrNull() ?: 0

        val perServing = Regex(
            "(per sajian|per serving).*?:?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(kcal|kkal)?",
            RegexOption.IGNORE_CASE
        ).find(text)?.groupValues?.get(2)?.replace(",", "")?.toIntOrNull() ?: 0

        val fatCalories = Regex(
            "(energi lemak|lemak|fat).*?:?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(g|kkal|cal)?",
            RegexOption.IGNORE_CASE
        ).find(text)?.groupValues?.get(2)?.replace(",", "")?.toIntOrNull() ?: 0

        return CalorieInfo(totalCalories, perServing, fatCalories)
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun getUserId(): Int? {
        return 43 // Ganti dengan logika nyata untuk mengambil user_id
    }
}
