import os

directories_to_scan = [
    r"c:\Users\Weimar Garcia\OneDrive\Desktop\ObraTech_Pro Aula\Codigo\ObraTech-Vinculando-Proyectos-y-Personas-main\src\main\java\com\obratech\controllers",
    r"c:\Users\Weimar Garcia\OneDrive\Desktop\ObraTech_Pro Aula\Codigo\ObraTech-Vinculando-Proyectos-y-Personas-main\src\main\java\com\obratech\service"
]

for d in directories_to_scan:
    for root, dirs, files in os.walk(d):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Replace Long id with String id in typical method parameters
                content = content.replace("Long id", "String id")
                content = content.replace("Long proyectoId", "String proyectoId")
                content = content.replace("Long contratistaId", "String contratistaId")
                content = content.replace("Long trabajadorId", "String trabajadorId")
                content = content.replace("Long personaId", "String personaId")
                content = content.replace("Long postId", "String postId")
                
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(content)
                
print("Refactoring de Long -> String completado en controllers y services.")
