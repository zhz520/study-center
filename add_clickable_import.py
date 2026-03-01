import os
filepath = r"d:\Android\application\app\src\main\java\cn\zhzgo\study\ui\screens\BaseConverterScreen.kt"
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

import_line = "import androidx.compose.foundation.clickable\n"
if "import androidx.compose.foundation.clickable" not in content:
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if line.startswith('import '):
            last_import = i
    lines.insert(last_import + 1, import_line.strip())
    content = '\n'.join(lines)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
print("Import added")
