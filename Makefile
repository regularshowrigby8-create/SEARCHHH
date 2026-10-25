# SEARCHHH - one-word commands for every routine task.
.DEFAULT_GOAL := help
SHELL := /bin/bash
PY := .venv/bin/python
PIP := .venv/bin/pip

.PHONY: help venv install dev test cov lint kcheck fmt typecheck audit precommit stack licenses apk apk-debug clean

help:  ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-14s\033[0m %s\n", $$1, $$2}'

venv:  ## Create the virtualenv
	python3 -m venv .venv

install: venv  ## Install python deps (runtime + dev)
	$(PIP) install -q --upgrade pip wheel setuptools
	$(PIP) install -q -r backend/requirements-dev.txt
	@echo "Python toolchain ready."

dev:  ## Run the API with autoreload on :8000
	cd backend && ../$(PY) -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

test:  ## Run the test suite
	$(PY) -m pytest backend/tests -q

cov:  ## Run tests with coverage (fails under 80%)
	$(PY) -m pytest backend/tests -q --cov=backend/app --cov-report=term-missing --cov-fail-under=80

lint:  ## Ruff lint (backend)
	$(PY) -m ruff check backend

kcheck:  ## Static Kotlin sanity checks (no JVM needed)
	$(PY) scripts/check_kotlin.py

fmt:  ## Ruff format
	$(PY) -m ruff format backend

typecheck:  ## mypy
	$(PY) -m mypy backend/app

audit:  ## pip-audit + bandit
	$(PY) -m pip_audit -r backend/requirements.txt || true
	$(PY) -m bandit -q -c pyproject.toml -r backend/app || true

precommit:  ## Install git hooks
	.venv/bin/pre-commit install

stack:  ## Regenerate STACK.md from the real installed environment
	$(PY) scripts/scan_stack.py

licenses:  ## Authoritative third-party license report (Python side)
	$(PY) -m pip_licenses --format=markdown --with-urls --output-file licenses-report/THIRD_PARTY.md
	@echo "wrote licenses-report/THIRD_PARTY.md"

apk:  ## Build a release APK via Gradle (needs the Android SDK)
	cd android && ./gradlew assembleRelease

apk-debug:  ## Build a debug APK
	cd android && ./gradlew assembleDebug

clean:  ## Remove caches and build output
	rm -rf .pytest_cache .ruff_cache .mypy_cache htmlcov .coverage
	find backend -name __pycache__ -type d -prune -exec rm -rf {} +
