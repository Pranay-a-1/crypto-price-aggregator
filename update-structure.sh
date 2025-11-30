#!/bin/bash

# ==========================================
# Project Structure Generator
# - Writes a markdown tree of the repo
# - Respects .gitignore and internal ignore list
# - Outputs to PROJECT_STRUCTURE.md
# ==========================================

OUTPUT_FILE="PROJECT_STRUCTURE.md"

# Terminal colors (for script output only)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Icons for different file types (used in markdown output)
DIR_ICON="🔵"      # Directories
JAVA_ICON="☕"     # Java files
CONFIG_ICON="⚙️"   # Config files
SCRIPT_ICON="📜"   # Scripts
DOC_ICON="📄"      # Docs
OTHER_ICON="📦"    # Other

# List of expressions to prune from `find`
PRUNE_ARGS=()

# ------------------------------------------
# Helper: add ignore pattern to PRUNE_ARGS
# ------------------------------------------
add_ignore() {
    local pattern="$1"

    # Add "-o" between prune terms
    if [ ${#PRUNE_ARGS[@]} -gt 0 ]; then
        PRUNE_ARGS+=("-o")
    fi

    if [[ "$pattern" == */* ]]; then
        # Treat as path pattern
        if [[ "$pattern" != ./* ]]; then
            if [[ "$pattern" == /* ]]; then
                pattern=".$pattern"
            else
                pattern="./$pattern"
            fi
        fi
        PRUNE_ARGS+=("-path" "$pattern")
    else
        # Treat as name pattern
        PRUNE_ARGS+=("-name" "$pattern")
    fi
}

# ------------------------------------------
# Base ignores (internal)
# ------------------------------------------
add_ignore ".git"
add_ignore ".gemini"
add_ignore "update-structure.sh"
add_ignore "$OUTPUT_FILE"

# ------------------------------------------
# Read .gitignore and add patterns
# ------------------------------------------
if [ -f ".gitignore" ]; then
    while IFS= read -r line || [ -n "$line" ]; do
        # Strip CR and surrounding whitespace
        line=$(echo "$line" | tr -d '\r' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

        # Skip empty, comments, and negation patterns
        if [[ -z "$line" || "$line" == \#* || "$line" == \!* ]]; then
            continue
        fi

        # Remove trailing slash to normalize
        clean_line=${line%/}
        add_ignore "$clean_line"
    done < ".gitignore"
fi

echo -e "${CYAN}🔍 Analyzing project structure...${NC}"

# ------------------------------------------
# Generate markdown file
# ------------------------------------------
{
    echo "# Project Structure"
    echo
    echo "**Generated on:** $(date '+%B %d, %Y at %I:%M %p')"
    echo
    echo '```text'

    # We use `find` + `sort` + `awk` to build a simple tree
    # No HTML, only plain text + icons
    find . -maxdepth 15 \( "${PRUNE_ARGS[@]}" \) -prune -o -print | \
        sort | \
        awk -v dir_icon="$DIR_ICON" \
            -v java_icon="$JAVA_ICON" \
            -v config_icon="$CONFIG_ICON" \
            -v script_icon="$SCRIPT_ICON" \
            -v doc_icon="$DOC_ICON" \
            -v other_icon="$OTHER_ICON" '
        BEGIN {
            FS="/"
        }
        NR == 1 {
            # Root line
            print "."
            next
        }
        {
            path = $0

            # Strip leading "./"
            sub(/^\.\//, "", path)

            if (path == "") next

            depth = split(path, parts, "/")
            filename = parts[depth]

            # Build prefix based on depth
            prefix = ""
            for (i = 1; i < depth; i++) {
                prefix = prefix "│   "
            }
            prefix = prefix "├── "

            # Determine if path is directory
            cmd = "test -d \"" path "\""
            is_dir = (system(cmd) == 0)

            # Choose icon
            icon = other_icon
            if (is_dir) {
                icon = dir_icon
            } else if (filename ~ /\.java$/) {
                icon = java_icon
            } else if (filename ~ /\.(properties|xml|yml|yaml|json|conf)$/) {
                icon = config_icon
            } else if (filename ~ /\.(sh|bat|cmd)$/) {
                icon = script_icon
            } else if (filename ~ /\.(md|txt|rst)$/) {
                icon = doc_icon
            }

            print prefix icon " " filename
        }'

    echo '```'
    echo
    echo "---"
    echo
    echo "### Legend"
    echo
    echo "- $DIR_ICON Directories"
    echo "- $JAVA_ICON Java source files"
    echo "- $CONFIG_ICON Configuration files"
    echo "- $SCRIPT_ICON Scripts"
    echo "- $DOC_ICON Documentation"
    echo "- $OTHER_ICON Other files"
    echo
    echo "---"
    echo
    echo "**Total Files:** $(find . -maxdepth 15 \( "${PRUNE_ARGS[@]}" \) -prune -o -type f -print | wc -l)"
    echo
    echo "**Total Directories:** $(find . -maxdepth 15 \( "${PRUNE_ARGS[@]}" \) -prune -o -type d -print | wc -l)"
} > "$OUTPUT_FILE"

echo -e "${GREEN}✅ $OUTPUT_FILE has been updated successfully!${NC}"
echo -e "${BLUE}📄 Location: $(pwd)/$OUTPUT_FILE${NC}"
echo -e "${YELLOW}📅 Date: $(date +%Y-%m-%d)${NC}"
