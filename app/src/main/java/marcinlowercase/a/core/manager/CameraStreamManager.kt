package marcinlowercase.a.core.manager

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

class CameraStreamManager(private val context: Context) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val barcodeScanner by lazy {
        try {
            com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
        } catch (e: Exception) {
            Log.e("CameraStreamManager", "Failed to init BarcodeScanner", e)
            null
        }
    }
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    @Volatile
    var isRunning: Boolean = false
        private set

    @Volatile
    var isTorchOn: Boolean = false
        private set

    // Active client listeners waiting for frames from /camera/stream
    val frameListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    var onBarcodeScanned: ((String) -> Unit)? = null

    private var lastScannedCode = ""
    private var lastScanTime = 0L

    fun start(lifecycleOwner: LifecycleOwner, torch: Boolean = false, onReady: (() -> Unit)? = null) {
        Log.i("CameraStreamManager", "start() called. Current isRunning=$isRunning")
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("CameraStreamManager", "ERROR: android.permission.CAMERA is NOT granted at OS level!")
            return
        }

        if (isRunning) {
            setTorch(torch)
            onReady?.invoke()
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    Log.v("CameraStreamManager", "Analyzer received frame: ${imageProxy.width}x${imageProxy.height}")
                    processFrame(imageProxy)
                }

                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
                isRunning = true
                Log.i("CameraStreamManager", "CameraX bound to lifecycle successfully!")

                Handler(Looper.getMainLooper()).post {
                    camera?.cameraInfo?.cameraState?.observe(lifecycleOwner) { state ->
                        Log.i("CameraStreamManager", "CameraState: type=${state.type}, error=${state.error?.code}")
                    }
                }

                if (torch) {
                    setTorch(true)
                }

                Handler(Looper.getMainLooper()).post { onReady?.invoke() }
            } catch (e: Exception) {
                Log.e("CameraStreamManager", "Failed to start CameraX", e)
                isRunning = false
            }
        }, ContextCompat.getMainExecutor(context))
    }
    fun stop() {
        if (!isRunning) return
        isRunning = false
        isTorchOn = false
        try {
            cameraProvider?.unbindAll()
            camera = null
        } catch (_: Exception) {}
    }

    fun setTorch(enable: Boolean): Boolean {
        isTorchOn = enable
        val cam = camera ?: return false
        return try {
            cam.cameraControl.enableTorch(enable)
            true
        } catch (e: Exception) {
            Log.e("CameraStreamManager", "Failed to set torch in CameraX", e)
            false
        }
    }

    fun toggleTorch(): Boolean {
        return setTorch(!isTorchOn)
    }

    private fun processFrame(imageProxy: ImageProxy) {
        try {
            val rawBitmap = imageProxy.toBitmap()
            if (rawBitmap != null) {
                // Rotate sensor frame to upright portrait orientation
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val bitmap = if (rotationDegrees != 0) {
                    val matrix = android.graphics.Matrix().apply {
                        postRotate(rotationDegrees.toFloat())
                    }
                    val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                    if (rotated != rawBitmap) rawBitmap.recycle()
                    rotated
                } else {
                    rawBitmap
                }

                // Compress upright bitmap to JPEG for MJPEG stream
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 65, stream)
                val jpegBytes = stream.toByteArray()

                if (frameListeners.isEmpty()) {
                    Log.v("CameraStreamManager", "Frame generated (${jpegBytes.size} bytes), but 0 active listeners")
                }

                for (listener in frameListeners) {
                    listener(jpegBytes)
                }

                val now = System.currentTimeMillis()
                if (now - lastScanTime > 500) {
                    val scanner = barcodeScanner
                    if (scanner != null) {
                        val inputImage = InputImage.fromBitmap(bitmap, 0)
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                val code = barcodes.firstOrNull()?.rawValue
                                if (!code.isNullOrBlank() && (code != lastScannedCode || now - lastScanTime > 2000)) {
                                    lastScannedCode = code
                                    lastScanTime = now
                                    Log.i("CameraStreamManager", "Barcode detected: $code")
                                    onBarcodeScanned?.invoke(code)
                                }
                            }
                    }
                }
            } else {
                Log.w("CameraStreamManager", "imageProxy.toBitmap() returned null")
            }
        } catch (e: Exception) {
            Log.e("CameraStreamManager", "processFrame exception", e)
        } finally {
            imageProxy.close()
        }
    }
}