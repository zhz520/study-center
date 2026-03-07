package cn.zhzgo.study.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.image.ImagesPlugin
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Base64
import cn.zhzgo.study.data.Question

// Minimalist Theme Colors
val ChatGPTBlack = Color(0xFF202123)
val ChatGPTGray = Color(0xFF343541)
val ChatGPTLightGray = Color(0xFF444654)
val ChatGPTText = Color.White
val ChatGPTTextSecondary = Color(0xFFACACBE)

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onBackground, fontSize: TextUnit = 16.sp) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val markwon = remember(context) { 
        Markwon.builder(context)
            .usePlugin(io.noties.markwon.html.HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(io.noties.markwon.ext.tables.TablePlugin.create(context))
            .usePlugin(io.noties.markwon.ext.strikethrough.StrikethroughPlugin.create())
            .usePlugin(io.noties.markwon.linkify.LinkifyPlugin.create())
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(48f)) // Use consistent font size
            .build() 
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(android.graphics.Color.argb(
                    (color.alpha * 255).toInt(),
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                ))
                textSize = fontSize.value
            }
        },
        update = { textView ->
            var processedText = text.replace("\\n", "\n")
                .replace("](/uploads/", "](https://studyapi.zhzgo.cn/uploads/")
                .replace("src=\"/uploads/", "src=\"https://studyapi.zhzgo.cn/uploads/")
            
            // 1. Normalize block delimiters \[ ... \] -> $$ ... $$
            processedText = processedText.replace(Regex("""\\\[(.*?)\\\]""", RegexOption.DOT_MATCHES_ALL)) {
                "\n$$\n" + it.groupValues[1].trim() + "\n$$\n"
            }

            // 2. Normalize inline delimiters \( ... \) -> $ ... $
            processedText = processedText.replace(Regex("""\\\((.*?)\\\)""", RegexOption.DOT_MATCHES_ALL)) {
                "$" + it.groupValues[1].trim() + "$"
            }

            // 3. Fix \begin{env} ... \end{env} blocks
            val envs = "align|equation|matrix|pmatrix|bmatrix|Bmatrix|vmatrix|Vmatrix|cases|gather|flalign|alignat|multline"
            val envRegex = Regex("""\\begin\{($envs)\*?\}(.*?)\\end\{\1\*?\}""", RegexOption.DOT_MATCHES_ALL)
            processedText = processedText.replace(envRegex) { 
                "\n$$\n" + it.value.trim() + "\n$$\n"
            }
            
            // 4. Force single $ ... $ to be treated as math blocks
            val inlineFormulaRegex = Regex("""(?<!\$)\$(?!\s)([^$\n]+?)(?<!\s)\$(?!\$)""")
            processedText = processedText.replace(inlineFormulaRegex) {
                "$$" + it.groupValues[1].trim() + "$$"
            }

            markwon.setMarkdown(textView, processedText)
        }
    )
}

@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = 16.sp,
    onHeightChange: ((Int) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var webViewHeight by remember { mutableStateOf(100) } 
    
    fun Color.toHex(): String = String.format("#%02x%02x%02x", 
        (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
    
    val textColorHex = color.toHex()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val bgColorHex = if (isDark) "#000000" else "#f9f9f9" // Match Theme.kt PureBlack and LightBg
    
    // Process text for initial display (simple replacements)
    val initialText = text.replace("\n", "<br>")
        .replace("](/uploads/", "](https://studyapi.zhzgo.cn/uploads/")
        .replace("src=\"/uploads/", "src=\"https://studyapi.zhzgo.cn/uploads/")

    // Base64 version for full rendering
    val base64Text = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="file:///android_asset/katex/katex.min.css">
            <style>
                body { 
                    font-family: -apple-system, system-ui, Roboto, sans-serif; 
                    font-size: ${fontSize.value}px; 
                    color: $textColorHex;
                    line-height: 1.8;
                    margin: 0;
                    padding: 8px 0;
                    background-color: $bgColorHex;
                    word-wrap: break-word;
                    overflow-x: hidden;
                    text-rendering: optimizeLegibility;
                    -webkit-user-select: none;
                    opacity: 0;
                    transition: opacity 0.2s ease-in;
                }
                body.math-rendered {
                    opacity: 1;
                }
                #container { 
                    width: 100%; 
                    min-height: 20px;
                    display: block;
                }
                .katex-display { 
                    margin: 1.2em 0; 
                    overflow-x: auto; 
                    overflow-y: hidden;
                    padding: 4px 0;
                }
                .katex { font-size: 1.1em; }
                img { max-width: 100%; height: auto; border-radius: 4px; margin: 12px 0; display: block; cursor: pointer; }
                h1, h2, h3, h4 { margin: 20px 0 10px 0; line-height: 1.4; }
                p { margin: 10px 0; }
                ul, ol { padding-left: 20px; margin: 10px 0; }
                li { margin: 6px 0; }
            </style>
        </head>
        <body>
            <div id="container">$initialText</div>
            
            <script src="file:///android_asset/katex/katex.min.js"></script>
            <script src="file:///android_asset/katex/contrib/auto-render.min.js"></script>
            <script src="file:///android_asset/marked/marked.min.js"></script>
            
            <script>
                function updateHeight() {
                    try {
                        const container = document.getElementById('container');
                        const height = Math.max(container.scrollHeight, document.body.scrollHeight, document.documentElement.scrollHeight, 40);
                        Android.onHeightChange(height);
                    } catch(e) {}
                }

                function b64_to_utf8(str) {
                    try {
                        const binaryString = window.atob(str);
                        const bytes = new Uint8Array(binaryString.length);
                        for (let i = 0; i < binaryString.length; i++) {
                            bytes[i] = binaryString.charCodeAt(i);
                        }
                        return new TextDecoder('utf-8').decode(bytes);
                    } catch (e) {
                        return decodeURIComponent(escape(window.atob(str)));
                    }
                }

                function performFullRender() {
                    try {
                        const container = document.getElementById('container');
                        let rawText = b64_to_utf8('$base64Text');
                        
                        // Fix for KaTeX: replace \begin{align} with \begin{aligned} etc
                        rawText = rawText.replace(/\\begin\{align\*?\}/g, "\\begin{aligned}")
                                         .replace(/\\end\{align\*?\}/g, "\\end{aligned}");
                        rawText = rawText.replace(/\\begin\{eqnarray\*?\}/g, "\\begin{aligned}")
                                         .replace(/\\end\{eqnarray\*?\}/g, "\\end{aligned}");
                        rawText = rawText.replace(/\\begin\{gather\*?\}/g, "\\begin{gathered}")
                                         .replace(/\\end\{gather\*?\}/g, "\\end{gathered}");
                        
                        // Mask math blocks to prevent marked from parsing them and eating backslashes
                        let mathBlocks = [];
                        let textToMark = rawText;
                        
                        // Mask $$ ... $$
                        textToMark = textToMark.replace(/\$\$([\s\S]*?)\$\$/g, function(match) {
                            mathBlocks.push(match);
                            return 'MATH_BLK_' + (mathBlocks.length - 1) + '_';
                        });
                        
                        // Mask \[ ... \]
                        textToMark = textToMark.replace(/\\\[([\s\S]*?)\\\]/g, function(match) {
                            mathBlocks.push(match);
                            return 'MATH_BLK_' + (mathBlocks.length - 1) + '_';
                        });

                        // Mask \( ... \)
                        textToMark = textToMark.replace(/\\\(([\s\S]*?)\\\)/g, function(match) {
                            mathBlocks.push(match);
                            return 'MATH_BLK_' + (mathBlocks.length - 1) + '_';
                        });

                        // Mask $ ... $ (inline)
                        textToMark = textToMark.replace(/\$((?:[^$\\]|\\.)+)\$/g, function(match) {
                            mathBlocks.push(match);
                            return 'MATH_BLK_' + (mathBlocks.length - 1) + '_';
                        });

                        if (typeof marked !== 'undefined') {
                            marked.setOptions({
                                breaks: true,
                                gfm: true,
                                headerIds: false,
                                mangle: false
                            });
                            textToMark = marked.parse(textToMark);
                        }
                        
                        // Restore math blocks
                        textToMark = textToMark.replace(/MATH_BLK_(\d+)_/g, function(match, p1) {
                            return mathBlocks[parseInt(p1)];
                        });
                        
                        container.innerHTML = textToMark;
                        
                        if (typeof renderMathInElement !== 'undefined') {
                            renderMathInElement(container, {
                                delimiters: [
                                    {left: "$$", right: "$$", display: true},
                                    {left: "$", right: "$", display: false},
                                    {left: "\\(", right: "\\)", display: false},
                                    {left: "\\[", right: "\\]", display: true},
                                    {left: "\\begin{aligned}", right: "\\end{aligned}", display: true},
                                    {left: "\\begin{gathered}", right: "\\end{gathered}", display: true},
                                    {left: "\\begin{equation}", right: "\\end{equation}", display: true},
                                    {left: "\\begin{matrix}", right: "\\end{matrix}", display: true},
                                    {left: "\\begin{cases}", right: "\\end{cases}", display: true}
                                ],
                                throwOnError: false
                            });
                        }
                        
                        document.body.classList.add('math-rendered');
                        
                        setTimeout(updateHeight, 100);
                        setTimeout(updateHeight, 500);
                        setTimeout(updateHeight, 1000);
                        
                        const imgs = document.getElementsByTagName('img');
                        for(let i=0; i<imgs.length; i++) {
                            imgs[i].addEventListener('load', updateHeight);
                            imgs[i].addEventListener('click', function() {
                                try { Android.onImageClick(this.src); } catch(e) {}
                            });
                        }
                    } catch (e) {
                        console.error("Render error:", e);
                        updateHeight();
                    }
                }

                updateHeight();
                
                if (document.readyState === 'complete') {
                    performFullRender();
                } else {
                    window.addEventListener('load', performFullRender);
                    // Backup trigger for fast-loading assets
                    setTimeout(performFullRender, 100);
                }
                
                if (window.ResizeObserver) {
                    new ResizeObserver(updateHeight).observe(document.getElementById('container'));
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(webViewHeight.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(0)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.textZoom = 100
                isClickable = true
                isFocusable = true
                // Disable built-in long-press image handling that can block click events
                setOnLongClickListener { true }
                
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onHeightChange(height: Float) {
                        // JavaScript JS pixels map 1:1 to Android DP if viewport width=device-width
                        val heightDp = height.toInt() + 4
                        post {
                            if (webViewHeight != heightDp) {
                                webViewHeight = heightDp
                            }
                            onHeightChange?.invoke(heightDp)
                        }
                    }
                    @android.webkit.JavascriptInterface
                    fun onImageClick(url: String) {
                        post { onImageClick?.invoke(url) }
                    }
                }, "Android")
                
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://studyapi.zhzgo.cn/", htmlContent, "text/html", "UTF-8", null)
        }
    )
}

@Composable
fun RenderText(
    text: String?, 
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = 16.sp,
    onImageClick: ((String) -> Unit)? = null
) {
    if (text == null) return
    
    // Check if it contains math markers or images
    val hasMath = text.contains("$") || text.contains("\\") || text.contains("{")
    val hasImages = text.contains("<img") || text.contains("![") || text.contains("/uploads/")
    
    if (hasMath || hasImages) {
        MathText(text = text, modifier = modifier, color = color, fontSize = fontSize, onImageClick = onImageClick)
    } else {
        MarkdownText(text = text, modifier = modifier, color = color, fontSize = fontSize)
    }
}

@Composable
fun QuestionContent(question: Question, onImageClick: ((String) -> Unit)? = null) {
    RenderText(
        text = question.content,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 24.dp),
        onImageClick = onImageClick
    )
}

@Composable
fun SingleChoiceQuestion(
    options: Map<String, String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    showAnswer: Boolean,
    correctAnswer: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { (key, value) ->
            val isSelected = selectedOption == key
            val parsedCorrect = correctAnswer?.trim()?.uppercase()
            val isCorrect = parsedCorrect == key.uppercase()
            
            // Minimalist Logic: 
            // - Normal: Gray Border
            // - Selected: Black Border + Black Dot
            // - ShowAnswer: Green/Red Border + White BG
            
            val borderColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981) // Green
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444) // Red
                isSelected -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            }
            
            val containerColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981).copy(alpha = 0.1f)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            
            val checkColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444)
                isSelected -> MaterialTheme.colorScheme.onSurface
                else -> Color.Transparent
            }

            val checkBorderColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444)
                isSelected -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .background(containerColor, RoundedCornerShape(8.dp))
                    .clickable { if (!showAnswer) onOptionSelected(key) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.dp, checkBorderColor, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(16.dp).background(checkColor, CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Option Text with specific styling
                Row(modifier = Modifier.fillMaxWidth()) {
                     Text(
                        text = "$key.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    RenderText(
                        text = value,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MultiChoiceQuestion(
    options: Map<String, String>,
    selectedOptions: Set<String>,
    onOptionToggle: (String) -> Unit,
    showAnswer: Boolean,
    correctAnswer: String? // e.g. "AB"
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { (key, value) ->
            val isSelected = selectedOptions.contains(key)
            val parsedCorrect = correctAnswer?.trim()?.uppercase()
            val isCorrect = parsedCorrect?.contains(key.uppercase()) == true
            
            val borderColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444)
                isSelected -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            }

            val containerColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981).copy(alpha = 0.1f)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            
            val checkColor = when {
                 showAnswer && isCorrect -> Color(0xFF10B981)
                 showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444)
                 isSelected -> MaterialTheme.colorScheme.onSurface
                 else -> Color.Transparent
            }

            val checkBorderColor = when {
                 showAnswer && isCorrect -> Color(0xFF10B981)
                 showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444)
                 isSelected -> MaterialTheme.colorScheme.onSurface
                 else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .background(containerColor, RoundedCornerShape(8.dp))
                    .clickable { if (!showAnswer) onOptionToggle(key) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.dp, checkBorderColor, RoundedCornerShape(4.dp))
                        .background(checkColor, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected || (showAnswer && isCorrect)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                 Row(modifier = Modifier.fillMaxWidth()) {
                     Text(
                        text = "$key.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    RenderText(text = value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun JudgeQuestion(
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    showAnswer: Boolean,
    correctAnswer: String? // "A" for True, "B" for False
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf("正确" to "A", "错误" to "B").forEach { (label, value) ->
            val isSelected = selectedOption == value
            val parsedCorrect = correctAnswer?.trim()?.uppercase()
            val isCorrect = parsedCorrect == value.uppercase()
            
            val borderColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444)
                isSelected -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            }
            
            val containerColor = when {
                showAnswer && isCorrect -> Color(0xFF10B981).copy(alpha = 0.1f)
                showAnswer && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            
            // Icon handling (Check or Close)
            val icon = if (label == "正确") Icons.Default.Check else Icons.Default.Close

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .background(containerColor, RoundedCornerShape(8.dp))
                    .clickable { if (!showAnswer) onOptionSelected(value) },
                contentAlignment = Alignment.Center
            ) {
               Row(verticalAlignment = Alignment.CenterVertically) {
                   Icon(
                       imageVector = icon,
                       contentDescription = null,
                       tint = if (isSelected || (showAnswer && isCorrect)) borderColor else Color.Gray
                   )
                   Spacer(modifier = Modifier.width(8.dp))
                   Text(
                       text = label, 
                       color = if (isSelected || (showAnswer && isCorrect)) borderColor else Color.Gray, 
                       fontSize = 18.sp, 
                       fontWeight = FontWeight.Bold
                   )
               }
            }
        }
    }
}

@Composable
fun FillBlankQuestion(
    answerText: String,
    onAnswerChange: (String) -> Unit,
    showAnswer: Boolean,
    correctAnswer: String?
) {
    val isCorrect = showAnswer && answerText.trim() == correctAnswer?.trim()
    val isWrong = showAnswer && answerText.trim() != correctAnswer?.trim()

    val borderColor = when {
        isCorrect -> Color(0xFF10B981)
        isWrong -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val unfocusedBorderColor = when {
        isCorrect -> Color(0xFF10B981)
        isWrong -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    }

    Column {
        OutlinedTextField(
            value = answerText,
            onValueChange = onAnswerChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("请输入答案") },
            enabled = !showAnswer,
            trailingIcon = {
                if (showAnswer) {
                    if (isCorrect) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                    } else {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = unfocusedBorderColor,
                disabledBorderColor = unfocusedBorderColor,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onSurface
            )
        )
        if (showAnswer) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "正确答案: $correctAnswer", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnswerSheetDrawer(
    questions: List<Question>,
    currentIndex: Int,
    answers: Map<Int, Any>, // QuestionId -> Answer
    onQuestionClick: (Int) -> Unit
) {
    var sheetPage by remember(currentIndex) { mutableStateOf(currentIndex / 100) }
    val pageSize = 100
    val totalPages = maxOf(1, (questions.size + pageSize - 1) / pageSize)
    if (sheetPage >= totalPages) sheetPage = totalPages - 1
    if (sheetPage < 0) sheetPage = 0

    val startIdx = sheetPage * pageSize
    val endIdx = minOf(startIdx + pageSize, questions.size)
    val pageItemsCount = if (startIdx < endIdx) endIdx - startIdx else 0

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "答题卡", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (sheetPage > 0) sheetPage-- }, enabled = sheetPage > 0) {
                    Text("上一页", color = if (sheetPage > 0) MaterialTheme.colorScheme.onSurface else Color.Gray)
                }
                Text("${sheetPage + 1} / $totalPages", fontSize = 14.sp)
                TextButton(onClick = { if (sheetPage < totalPages - 1) sheetPage++ }, enabled = sheetPage < totalPages - 1) {
                    Text("下一页", color = if (sheetPage < totalPages - 1) MaterialTheme.colorScheme.onSurface else Color.Gray)
                }
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp) // Bound height for bottom sheet
        ) {
            items(pageItemsCount) { localIndex ->
                val globalIndex = startIdx + localIndex
                val question = questions[globalIndex]
                val isAnswered = answers.containsKey(question.id)
                val isCurrent = globalIndex == currentIndex
                
                val bg = when {
                    isCurrent -> MaterialTheme.colorScheme.onSurface
                    isAnswered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) 
                    else -> MaterialTheme.colorScheme.surface
                }
                
                val contentColor = if (isCurrent) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                val border = if (isCurrent || isAnswered) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .run { if (border != null) border(border, RoundedCornerShape(8.dp)) else this }
                        .clickable { onQuestionClick(globalIndex) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${globalIndex + 1}",
                        color = contentColor,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AIAnalysisView(analysis: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💡 解析", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        RenderText(
            text = analysis ?: "暂无解析",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        for (measurable in measurables) {
            val placeable = measurable.measure(constraints)
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            if (currentRow.isNotEmpty()) {
                currentRowWidth += mainAxisSpacingPx
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val width = rowWidths.maxOrNull() ?: 0
        val height = rowHeights.sum() + (if (rows.isNotEmpty()) (rows.size - 1) * crossAxisSpacingPx else 0)

        layout(width, height) {
            var y = 0
            for ((i, row) in rows.withIndex()) {
                var x = 0
                for (placeable in row) {
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacingPx
                }
                y += rowHeights[i] + crossAxisSpacingPx
            }
        }
    }
}
