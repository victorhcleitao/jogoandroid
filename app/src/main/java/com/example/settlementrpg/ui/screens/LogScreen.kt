package com.example.settlementrpg.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settlementrpg.data.model.LogMessage
import com.example.settlementrpg.data.model.LogType
import com.example.settlementrpg.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogScreen(
    logs: List<LogMessage>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Color(0xFF2C2C35))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = "Diário do Assentamento",
                color = GoldPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nenhum evento registrado ainda.", color = TextGray, fontSize = 14.sp)
                }
            } else {
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        val (bgColor, textColor, borderVal) = when (log.type) {
                            LogType.COMBAT -> Triple(Color(0xFF261214), Color(0xFFFF8A80), Color(0xFF4C1E20))
                            LogType.GUILD -> Triple(Color(0xFF2B200E), Color(0xFFFFD54F), Color(0xFF5D4037))
                            LogType.UPGRADE -> Triple(Color(0xFF132A13), Color(0xFF81C784), Color(0xFF2E7D32))
                            LogType.SYSTEM -> Triple(Color(0xFF1E1E24), TextWhite, Color(0xFF37474F))
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, borderVal),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "[${dateFormat.format(Date(log.timestamp))}]",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = log.text,
                                    color = textColor,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
