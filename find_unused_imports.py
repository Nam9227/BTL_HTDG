import os
import re

def find_unused_imports(directory):
    unused_imports = {}
    
    # Regex to match basic imports, ignoring static imports and wildcards for simplicity
    # e.g., import java.util.List;
    import_pattern = re.compile(r'^import\s+(?!static\s)([\w\.]+)\.([\w]+);')
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    
                lines = content.split('\n')
                imports = []
                for line in lines:
                    line = line.strip()
                    match = import_pattern.match(line)
                    if match:
                        full_import = line
                        class_name = match.group(2)
                        imports.append((full_import, class_name))
                
                # Check usage
                for full_import, class_name in imports:
                    # Remove the import line from content to avoid self-match
                    # Find all occurrences of the class name as a whole word
                    # using regex \bClassName\b
                    usage_pattern = re.compile(r'\b' + class_name + r'\b')
                    matches = usage_pattern.findall(content)
                    # The import statement itself contains one occurrence of the class name
                    # If there's only 1 occurrence, it's just the import statement!
                    # Wait, what if the import is like import com.uet.model.Role; 
                    # and the usage is Role? Then there are 2 matches.
                    # Sometimes the package name contains the class name? Rarely.
                    if len(matches) <= 1:
                        if file_path not in unused_imports:
                            unused_imports[file_path] = []
                        unused_imports[file_path].append(full_import)

    return unused_imports

if __name__ == "__main__":
    dirs_to_check = ["bidding-client/src", "bidding-common/src", "bidding-server/src"]
    for d in dirs_to_check:
        if os.path.exists(d):
            unused = find_unused_imports(d)
            for file, imports in unused.items():
                print(f"File: {file}")
                for imp in imports:
                    print(f"  - {imp}")
