import os


class Settings:
    def __init__(self) -> None:
        self.ai_provider = os.getenv("AI_PROVIDER", "ollama").strip().lower()
        self.ollama_base_url = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434").strip().rstrip("/")
        self.ollama_model = os.getenv("OLLAMA_MODEL", "gemma3:latest").strip()
        self.openai_api_key = os.getenv("OPENAI_API_KEY", "").strip()
        self.openai_model = os.getenv("OPENAI_MODEL", "gpt-4.1-mini").strip()
        self.worker_internal_token = os.getenv("PYTHON_WORKER_INTERNAL_TOKEN", "dev-worker-token").strip()

    @property
    def openai_enabled(self) -> bool:
        return bool(self.openai_api_key)


settings = Settings()
