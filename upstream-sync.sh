#!/bin/bash
# upstream-sync.sh - Script to help sync with upstream Notely Voice repository

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Notely Capture - Upstream Sync Helper${NC}"
echo "============================================="

# Function to manage upstream remote
manage_upstream_remote() {
    if git remote get-url upstream &> /dev/null; then
        echo -e "${GREEN}Upstream remote exists${NC}"
        return 0
    else
        echo -e "${YELLOW}No upstream remote found${NC}"
        return 1
    fi
}

# Function to add upstream remote
add_upstream_remote() {
    echo -e "${YELLOW}Adding upstream remote...${NC}"
    git remote add upstream https://github.com/tosinonikute/NotelyVoice.git
    echo -e "${GREEN}Upstream remote added${NC}"
}

# Function to remove upstream remote
remove_upstream_remote() {
    if git remote get-url upstream &> /dev/null; then
        echo -e "${YELLOW}Removing upstream remote...${NC}"
        git remote remove upstream
        echo -e "${GREEN}Upstream remote removed${NC}"
    else
        echo -e "${YELLOW}No upstream remote to remove${NC}"
    fi
}

# Check upstream status
if ! manage_upstream_remote; then
    echo ""
    echo "Upstream remote management:"
    echo "1) Add upstream remote and continue sync"
    echo "2) Exit (no sync needed)"
    
    read -p "Enter your choice (1-2): " upstream_choice
    
    case $upstream_choice in
        1)
            add_upstream_remote
            ;;
        2)
            echo -e "${GREEN}Exiting...${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}Invalid choice. Exiting...${NC}"
            exit 1
            ;;
    esac
fi

# Fetch latest upstream changes
echo -e "${YELLOW}Fetching upstream changes...${NC}"
if ! git fetch upstream; then
    echo -e "${RED}Failed to fetch upstream. Check your internet connection or upstream repository access.${NC}"
    exit 1
fi

# Show current branch
current_branch=$(git branch --show-current)
echo -e "${GREEN}Current branch: ${current_branch}${NC}"

# Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
    echo -e "${RED}Warning: You have uncommitted changes. Please commit or stash them first.${NC}"
    exit 1
fi

# Option to merge or just show differences
echo ""
echo "Choose an option:"
echo "1) Show differences with upstream/main"
echo "2) Merge upstream/main (will preserve your personalizations)"
echo "3) Create a new branch for testing upstream changes"
echo "4) Remove upstream remote (disconnect from upstream)"
echo "5) Exit"

read -p "Enter your choice (1-5): " choice

case $choice in
    1)
        echo -e "${YELLOW}Showing differences with upstream/main...${NC}"
        git diff HEAD upstream/main --name-only
        ;;
    2)
        echo -e "${YELLOW}Merging upstream/main...${NC}"
        git merge upstream/main
        echo -e "${GREEN}Merge completed. Please review the changes and commit if needed.${NC}"
        ;;
    3)
        read -p "Enter branch name for testing (e.g., test-upstream): " branch_name
        echo -e "${YELLOW}Creating branch ${branch_name}...${NC}"
        git checkout -b "$branch_name"
        git merge upstream/main
        echo -e "${GREEN}Branch ${branch_name} created and merged. Switch back with: git checkout ${current_branch}${NC}"
        ;;
    4)
        echo -e "${YELLOW}Disconnecting from upstream...${NC}"
        remove_upstream_remote
        echo -e "${GREEN}Upstream remote removed. This fork is now independent.${NC}"
        echo -e "${YELLOW}Note: You can re-add upstream later by running this script again.${NC}"
        ;;
    5)
        echo -e "${GREEN}Exiting...${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice. Exiting...${NC}"
        exit 1
        ;;
esac
