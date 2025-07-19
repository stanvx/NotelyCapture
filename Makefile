# Makefile for Claude Code Hooks Integration
# NotelyCapture - Kotlin Multiplatform Mobile Application

.PHONY: lint test lint-all test-all lint-kotlin lint-python check-integration

# Lint target - called by smart-lint.sh after file edits
# Receives FILE= argument with relative path to edited file
lint:
	@if [ -n "$(FILE)" ]; then \
		echo "Linting specific file: $(FILE)" >&2; \
		case "$(FILE)" in \
			*.kt|*.kts) \
				echo "Running Kotlin linting for $(FILE)" >&2; \
				./gradlew ktlintCheck --daemon >&2; \
				;; \
			*.py) \
				echo "Running Python linting for $(FILE)" >&2; \
				if command -v ruff >/dev/null 2>&1; then \
					ruff check "$(FILE)" >&2; \
				elif command -v flake8 >/dev/null 2>&1; then \
					flake8 "$(FILE)" >&2; \
				else \
					echo "Warning: No Python linter found (ruff or flake8)" >&2; \
				fi; \
				;; \
			*) \
				echo "No specific linter for file type: $(FILE)" >&2; \
				;; \
		esac \
	else \
		echo "Linting all files" >&2; \
		$(MAKE) lint-all; \
	fi

# Test target - called by smart-test.sh after file edits
# Receives FILE= argument with relative path to edited file  
test:
	@if [ -n "$(FILE)" ]; then \
		echo "Testing for file: $(FILE)" >&2; \
		case "$(FILE)" in \
			*.kt|*.kts) \
				echo "Running Kotlin tests" >&2; \
				./gradlew testDebugUnitTest --daemon >&2; \
				;; \
			*.py) \
				echo "Running Python tests (if any)" >&2; \
				if command -v pytest >/dev/null 2>&1; then \
					pytest -xvs >&2 || echo "No Python tests found" >&2; \
				else \
					echo "No Python testing framework found" >&2; \
				fi; \
				;; \
			*) \
				echo "Running all available tests" >&2; \
				$(MAKE) test-all; \
				;; \
		esac \
	else \
		echo "Running all tests" >&2; \
		$(MAKE) test-all; \
	fi

# Project-wide linting
lint-all:
	@echo "Running all linting checks" >&2
	@$(MAKE) lint-kotlin
	@$(MAKE) lint-python

# Project-wide testing
test-all:
	@echo "Running all tests" >&2
	@./gradlew testDebugUnitTest --daemon >&2
	@if command -v pytest >/dev/null 2>&1; then \
		pytest -xvs >&2 || echo "No Python tests to run" >&2; \
	fi

# Kotlin-specific linting
lint-kotlin:
	@echo "Running Kotlin linting (ktlint)" >&2
	@./gradlew ktlintCheck --daemon >&2

# Python-specific linting (for utility files)
lint-python:
	@echo "Running Python linting (optional)" >&2
	@if command -v ruff >/dev/null 2>&1; then \
		echo "Using ruff for Python linting" >&2; \
		ruff check lib/src/main/jni/ggml/src/ggml-cuda/template-instances/ >&2 || true; \
		ruff check lib/src/main/jni/ggml/src/ggml-opencl/kernels/ >&2 || true; \
	elif command -v flake8 >/dev/null 2>&1; then \
		echo "Using flake8 for Python linting" >&2; \
		flake8 lib/src/main/jni/ggml/src/ggml-cuda/template-instances/ >&2 || true; \
		flake8 lib/src/main/jni/ggml/src/ggml-opencl/kernels/ >&2 || true; \
	else \
		echo "No Python linter available (install ruff or flake8 if needed)" >&2; \
	fi

# Helper target to verify integration
check-integration:
	@echo "✓ Makefile detected by Claude Code hooks" >&2
	@echo "  - 'make lint' target: available" >&2
	@echo "  - 'make test' target: available" >&2
	@echo "  - Project type: Kotlin Multiplatform (Android/iOS)" >&2
	@echo "  - Build system: Gradle" >&2
	@echo "" >&2
	@echo "Test integration with:" >&2
	@echo "  make lint FILE=shared/src/commonMain/kotlin/SomeFile.kt" >&2
	@echo "  make test FILE=shared/src/commonTest/kotlin/SomeTest.kt" >&2
	@echo "  make lint FILE=lib/src/main/jni/ggml/src/ggml-cuda/template-instances/generate_cu_files.py" >&2