package com.alialtinok.wisly.ui.screens.my

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.CustomWord
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.my.components.EmptyState
import com.alialtinok.wisly.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWordsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val settings = container.userSettingsRepository

    val myWords by repo.myWords.collectAsState(initial = emptyList())
    val nativeLanguage by settings.nativeLanguage.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    SubScreenScaffold(
        title = strings.myWordsScreenTitle,
        onBack = onBack,
        actions = {
            IconButton(onClick = { showAddSheet = true }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = strings.myWordsAdd,
                    tint = WislyColors.AccentGreen,
                )
            }
        },
    ) {
        if (myWords.isEmpty()) {
            EmptyState(
                title = strings.myWordsEmptyTitle,
                message = strings.myWordsEmptyDesc,
                icon = Icons.Filled.NoteAdd,
                iconTint = WislyColors.AccentGreen,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(myWords, key = { it.id }) { word ->
                    MyWordRow(
                        word = word,
                        onDelete = { scope.launch { repo.removeMyWord(word) } },
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = WislyColors.Background,
        ) {
            AddWordForm(
                onSave = { wordText, translation, example ->
                    scope.launch {
                        repo.addMyWord(
                            CustomWord(
                                word = wordText.trim(),
                                translation = translation.trim(),
                                languageID = nativeLanguage?.id.orEmpty(),
                                example = example.trim(),
                            ),
                        )
                    }
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false },
            )
        }
    }
}

@Composable
private fun MyWordRow(word: CustomWord, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = word.word,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = word.translation.ifEmpty { "—" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (word.translation.isEmpty()) WislyColors.OnSurfaceMuted else WislyColors.Primary,
            )
            if (word.example.isNotEmpty()) {
                Text(
                    text = word.example,
                    fontSize = 12.sp,
                    color = WislyColors.OnSurfaceMuted,
                )
            }
        }
        Text(
            text = word.languageID.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = WislyColors.AccentGreen,
            modifier = Modifier
                .background(WislyColors.AccentGreen.copy(alpha = 0.1f), CircleShape)
                .padding(horizontal = 6.dp, vertical = 3.dp),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = LocalAppStrings.current.myWordsDelete,
                tint = WislyColors.AccentRed,
            )
        }
    }
}

@Composable
private fun AddWordForm(
    onSave: (word: String, translation: String, example: String) -> Unit,
    onCancel: () -> Unit,
) {
    var wordText by remember { mutableStateOf("") }
    var translationText by remember { mutableStateOf("") }
    var exampleText by remember { mutableStateOf("") }

    val canSave = wordText.trim().isNotEmpty() && translationText.trim().isNotEmpty()
    val s = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = s.myWordsAddDialogTitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )

        FieldLabel(s.myWordsFieldEnglish)
        AppTextField(
            value = wordText,
            onValueChange = { wordText = it },
            placeholder = s.myWordsEnglishPlaceholder,
        )

        FieldLabel(s.myWordsFieldTranslation)
        AppTextField(
            value = translationText,
            onValueChange = { translationText = it },
            placeholder = s.myWordsTranslationPlaceholder,
            capitalization = KeyboardCapitalization.Sentences,
        )

        FieldLabel(s.myWordsFieldExample)
        AppTextField(
            value = exampleText,
            onValueChange = { exampleText = it },
            placeholder = s.myWordsExamplePlaceholder,
            capitalization = KeyboardCapitalization.Sentences,
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onSave(wordText, translationText, exampleText) },
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WislyColors.AccentGreen,
                disabledContainerColor = WislyColors.SurfaceBorder,
                contentColor = Color.White,
                disabledContentColor = WislyColors.OnSurfaceMuted,
            ),
        ) {
            Text(
                text = s.myWordsAdd,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = WislyColors.OnSurfaceMuted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = WislyColors.OnSurfaceMuted.copy(alpha = 0.6f)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(capitalization = capitalization),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WislyColors.Primary,
            unfocusedBorderColor = WislyColors.SurfaceBorder,
            focusedContainerColor = WislyColors.Surface,
            unfocusedContainerColor = WislyColors.Surface,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = WislyColors.Primary,
        ),
    )
}
