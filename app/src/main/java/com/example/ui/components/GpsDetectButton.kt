package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.LocationDetectResult
import com.example.data.LocationHelper
import com.example.ui.theme.LocalResponsiveSpec
import com.example.ui.theme.iCaseBlue
import com.example.ui.theme.iCaseGreen
import com.example.ui.theme.iCaseLightBlue
import com.example.ui.theme.iCaseRed
import kotlinx.coroutines.launch

@Composable
fun GpsDetectLocationButton(
    onAddressDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
    buttonLabel: String = "Use GPS / Detect Current Location",
    testTag: String = "use_current_location_button"
) {
    val spec = LocalResponsiveSpec.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isDetecting by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun fetchLiveLocation() {
        if (isDetecting) return
        scope.launch {
            isDetecting = true
            isError = false
            statusText = "Fetching live GPS coordinates..."
            when (val result = LocationHelper.detectCurrentLocation(context)) {
                is LocationDetectResult.Success -> {
                    onAddressDetected(result.location.formattedAddress)
                    isError = false
                    statusText = if (result.location.isGeocoded) {
                        "Current location detected (${result.location.locality.ifBlank { "Live GPS" }})."
                    } else {
                        "Live GPS coordinates captured. Add house/street details if needed."
                    }
                }
                is LocationDetectResult.Error -> {
                    isError = true
                    statusText = result.message
                }
            }
            isDetecting = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
            (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (granted) {
            fetchLiveLocation()
        } else {
            isError = true
            statusText = "Location permission denied. Please allow location access or enter your address manually."
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = {
                if (LocationHelper.hasLocationPermission(context)) {
                    fetchLiveLocation()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            enabled = !isDetecting,
            colors = ButtonDefaults.buttonColors(
                containerColor = iCaseLightBlue,
                disabledContainerColor = iCaseLightBlue.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag(testTag)
        ) {
            if (isDetecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = iCaseBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detecting Current Location...",
                    color = iCaseBlue,
                    fontSize = spec.sp(13),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Detect Current Location",
                    tint = iCaseBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonLabel,
                    color = iCaseBlue,
                    fontSize = spec.sp(13),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (statusText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText!!,
                fontSize = spec.sp(11),
                color = if (isError) iCaseRed else iCaseGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
